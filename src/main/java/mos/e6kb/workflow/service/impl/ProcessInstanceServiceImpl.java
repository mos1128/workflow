package mos.e6kb.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import mos.e6kb.workflow.constant.ActivitiConstants;
import mos.e6kb.workflow.dao.DeployProcessDao;
import mos.e6kb.workflow.dto.*;
import mos.e6kb.workflow.entity.DeployProcess;
import mos.e6kb.workflow.exception.BusinessException;
import mos.e6kb.workflow.service.ProcessInstanceService;
import mos.e6kb.workflow.utils.UserUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: lilin
 * @CreateTime: 2024-12-06  17:17
 * @Description: 流程实例
 */
@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DeployProcessDao deployProcessDao;

    /**
     * 我发起的任务列表
     *
     * @param dto 参数
     * @return 结果
     */
    @Override
    public PageResult<InstanceDto> page(instanceQueryDto dto) {
        String userId = String.valueOf(UserUtils.getCurrentUserId());
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId)
                .includeProcessVariables()
                .orderByProcessInstanceStartTime()
                .desc();
        if (StringUtils.isNoneEmpty(dto.getBusinessKey())) {
            query.processInstanceBusinessKey(dto.getBusinessKey());
        }
        if (StringUtils.isNoneEmpty(dto.getProcessName())) {
            query.processDefinitionName(dto.getProcessName());
        }
        if (StringUtils.isNoneEmpty(dto.getVariable())) {
            query.variableValueEquals(dto.getVariable());
        }
        if (Objects.nonNull(dto.getIsFinish())) {
            if (Boolean.TRUE.equals(dto.getIsFinish())) {
                query.finished();
            } else {
                query.unfinished();
            }
        }
        List<HistoricProcessInstance> list = query.listPage(dto.getPageNo() - 1, dto.getPageSize());
        long totalCount = query.count();

        List<InstanceDto> resultList = new ArrayList<>();
        for (HistoricProcessInstance instance : list) {
            // 设置流程实例
            InstanceDto instanceDto = new InstanceDto();
            instanceDto.setInstanceId(instance.getId())
                    .setInstanceName(instance.getName())
                    .setBusinessKey(instance.getBusinessKey())
                    .setProcessName(instance.getProcessDefinitionName())
                    .setVariables(instance.getProcessVariables())
                    .setStartTime(instance.getStartTime())
                    .setEndTime(instance.getEndTime());

            // 获取任务处理节点
            Task task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId())
                    .singleResult();
            if (task != null) {
                instanceDto.setTaskId(task.getId())
                        .setTaskName(task.getName());
            }

            // 获取额外配置信息
            Integer cancelTime = (Integer) instanceDto.getVariables().get(ActivitiConstants.CANCEL_TIME);
            if (cancelTime != null) {
                long canCancelTime = cancelTime * 60 * 1000L;
                long currentTime = System.currentTimeMillis();
                long createTime = instance.getStartTime().getTime();
                instanceDto.setCanCancel(currentTime - createTime < canCancelTime);
            }
            resultList.add(instanceDto);
        }
        return new PageResult<>(dto.getPageNo(), dto.getPageSize(), resultList, totalCount);
    }

    /**
     * 启动流程
     *
     * @param dto 启动流程参数
     */
    @Transactional
    @Override
    public String startProcess(StartProcessDto dto) {
        String companyId = String.valueOf(UserUtils.getCompanyId());
        String departmentId = String.valueOf(UserUtils.getDepartmentId());
        String userId = String.valueOf(UserUtils.getCurrentUserId());
        String username = UserUtils.getUsername();

        // 获取相关数据
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(dto.getProcessId())
                .singleResult();
        if (processDefinition.isSuspended()) {
            throw new BusinessException("流程已停用");
        }

        // 获取额外配置信息
        DeployProcess deployProcess = deployProcessDao.selectOne(new LambdaQueryWrapper<DeployProcess>()
                .eq(DeployProcess::getProcessId, dto.getProcessId()));
        if (deployProcess == null) {
            throw new BusinessException("当前流程不存在或已被弃用");
        }

        // 设置流程发起人用户Id
        Authentication.setAuthenticatedUserId(userId);
        Map<String, Object> variables = dto.getVariables();
        // 设置表单信息
        variables.put(ActivitiConstants.NODE_ + "main", JSON.toJSONString(dto.getVariables()));
        // 设置发起人信息
        variables.put(ActivitiConstants.INITIATOR, userId);
        variables.put(ActivitiConstants.COMPANY_ID, companyId);
        variables.put(ActivitiConstants.DEPARTMENT_ID, departmentId);
        // 设置业务流程的类型和数据
        variables.put(ActivitiConstants.BUSINESS_JSON, dto.getBusinessJson());
        // 设置额外配置参数
        variables.put(ActivitiConstants.CC_USERS_SELF, deployProcess.getCcUsersSelf());
        variables.put(ActivitiConstants.CANCEL_TIME, deployProcess.getCancelTime());
        variables.put(ActivitiConstants.SAME_AUTO_APPROVE, deployProcess.getSameAutoApprove());
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(dto.getProcessId())
                .businessKey(dto.getBusinessKey())
                .name(username + "-" + processDefinition.getName())
                .variables(variables)
                .start();
        return processInstance.getId();
    }


    /**
     * 删除流程实例
     *
     * @param instanceId 流程实例id
     */
    @Override
    public void delete(String instanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        if (processInstance == null) {
            throw new BusinessException("流程不存在");
        }

        // 获取撤销时间，如果当前时间距离流程创建时间大于撤销时间，则不能删除
        String processDefinitionId = processInstance.getProcessDefinitionId();
        DeployProcess deployProcess = deployProcessDao.selectOne(new LambdaQueryWrapper<DeployProcess>()
                .eq(DeployProcess::getProcessId, processDefinitionId));
        if (deployProcess.getCancelTime() != null) {
            long cancelTime = deployProcess.getCancelTime() * 60 * 1000;
            long createTime = processInstance.getStartTime().getTime();
            long currentTime = System.currentTimeMillis();
            if (currentTime - createTime > cancelTime) {
                throw new BusinessException("已经超过可撤销时间");
            }
        }

        runtimeService.deleteProcessInstance(instanceId, null);
    }

    /**
     * 流程详情
     *
     * @param instanceId
     * @return
     */
    @Override
    public InstanceDto detail(String instanceId) {
        // 查询历史数据
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .includeProcessVariables()
                .singleResult();

        // 设置流程实例
        InstanceDto instanceDto = new InstanceDto();
        instanceDto.setInstanceId(instance.getId())
                .setInstanceName(instance.getName())
                .setBusinessKey(instance.getBusinessKey())
                .setProcessName(instance.getProcessDefinitionName())
                .setVariables(instance.getProcessVariables())
                .setStartTime(instance.getStartTime())
                .setEndTime(instance.getEndTime());

        // 获取任务处理节点
        Task task = taskService.createTaskQuery()
                .processInstanceId(instance.getId())
                .singleResult();
        if (task != null) {
            instanceDto.setTaskId(task.getId())
                    .setTaskName(task.getName());
        }

        // 获取额外配置信息
        Integer cancelTime = (Integer) instanceDto.getVariables().get(ActivitiConstants.CANCEL_TIME);
        if (cancelTime != null) {
            long canCancelTime = cancelTime * 60 * 1000L;
            long currentTime = System.currentTimeMillis();
            long createTime = instance.getStartTime().getTime();
            instanceDto.setCanCancel(currentTime - createTime < canCancelTime);
        }
        return instanceDto;
    }

    /**
     * 流程进度
     *
     * @param instanceId
     * @return
     */
    @Override
    public List<InstanceNodeDto> schedule(String instanceId) {

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();

        List<InstanceNodeDto> resultList = new ArrayList<>();
        for (HistoricTaskInstance taskInstance : list) {
            Map<String, Object> processVariables = taskInstance.getProcessVariables();
            String nodeVariables = (String) processVariables.get(ActivitiConstants.NODE_ + taskInstance.getTaskDefinitionKey());

            InstanceNodeDto instanceNodeDto = new InstanceNodeDto();
            instanceNodeDto.setNodeId(taskInstance.getTaskDefinitionKey())
                    .setNodeName(taskInstance.getName())
                    .setVariables(nodeVariables)
                    .setStartTime(taskInstance.getStartTime())
                    .setEndTime(taskInstance.getEndTime());
            if (taskInstance.getEndTime() != null) {
                instanceNodeDto.setIsFinish(true);
            } else {
                instanceNodeDto.setIsFinish(false)
                        .setCandidateInfo(getCandidateInfo(taskInstance.getId()));
            }
            resultList.add(instanceNodeDto);
        }
        return resultList;
    }

    /**
     * 获取获选人 或 候选组
     *
     * @param taskId
     * @return
     */
    public String getCandidateInfo(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            return null;
        }

        List<HistoricIdentityLink> identityLinks = historyService.getHistoricIdentityLinksForTask(taskId);

        // 候选组ids
        List<Integer> groupIds = identityLinks.stream()
                .filter(t -> t.getType().equals(IdentityLinkType.CANDIDATE))
                .map(HistoricIdentityLink::getGroupId)
                .filter(StringUtils::isNotEmpty)
                .map(str -> StringUtils.substringAfterLast(str, "-"))
                .map(Integer::valueOf)
                .toList();
        // 查询数据库找到候选组名称
        if (!groupIds.isEmpty()) {
            // todo 数据库查询
            return String.join(",", String.valueOf(groupIds));
        }

        // 候选人ids
        List<Integer> userIds = identityLinks.stream()
                .filter(t -> t.getType().equals(IdentityLinkType.CANDIDATE))
                .map(HistoricIdentityLink::getUserId)
                .filter(StringUtils::isNotEmpty)
                .map(Integer::valueOf)
                .toList();
        if (!userIds.isEmpty()) {
            // todo 数据库查询
            return String.join(",", String.valueOf(userIds));
        }

        return "审批人";
    }
} 
