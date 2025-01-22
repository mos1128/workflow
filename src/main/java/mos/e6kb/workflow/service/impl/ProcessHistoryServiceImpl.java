package mos.e6kb.workflow.service.impl;

import com.alibaba.fastjson.JSON;
import de.odysseus.el.tree.TreeBuilderException;
import lombok.extern.slf4j.Slf4j;
import mos.e6kb.workflow.constant.ActivitiConstants;
import mos.e6kb.workflow.dto.InstanceDto;
import mos.e6kb.workflow.dto.PageResult;
import mos.e6kb.workflow.dto.TodoCompleteDto;
import mos.e6kb.workflow.dto.instanceQueryDto;
import mos.e6kb.workflow.exception.BusinessException;
import mos.e6kb.workflow.service.ProcessHistoryService;
import mos.e6kb.workflow.utils.UserUtils;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * 流程历史相关
 *
 * @author liuguofeng
 * @date 2023/11/24 09:40
 **/
@Slf4j
@Service("processHistoryService")
public class ProcessHistoryServiceImpl implements ProcessHistoryService {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 节点审批
     *
     * @param dto 参数
     */
    @Transactional
    @Override
    public void complete(TodoCompleteDto dto) {
        try {
            Task task = taskService.createTaskQuery()
                    .active()
                    .includeProcessVariables()
                    .processInstanceId(dto.getInstanceId())
                    .orderByTaskCreateTime()
                    .desc()
                    .singleResult();
            if (task == null) {
                throw new BusinessException("未找到审批节点!");
            }
            // 没有指定代理人就认领任务
            if (StringUtils.isEmpty(task.getAssignee())) {
                String userId = String.valueOf(UserUtils.getCurrentUserId());
                taskService.claim(task.getId(), userId);
                task.setAssignee(userId);
            }

            Map<String, Object> nodeVariables = dto.getVariables();
            if (Objects.isNull(nodeVariables) || !nodeVariables.containsKey(ActivitiConstants.APPROVAL_RESULT)) {
                throw new BusinessException("审批结果不能为空");
            }
            HashMap<String, Object> variables = new HashMap<>();
            // 设置审批结果
            variables.put(ActivitiConstants.APPROVAL_RESULT, nodeVariables.get(ActivitiConstants.APPROVAL_RESULT));
            // 设置具体节点的表单参数
            variables.put(ActivitiConstants.NODE_ + task.getTaskDefinitionKey(), JSON.toJSONString(nodeVariables));
            // 同一审批人重复出现时自动审批
            Boolean sameAutoApprove = (Boolean) task.getProcessVariables().get(ActivitiConstants.SAME_AUTO_APPROVE);
            if (Boolean.TRUE.equals(sameAutoApprove)) {
                // 设置审批人列表
                Set<String> assigneeSet = new HashSet<>();
                String assigneeString = (String) task.getProcessVariables().get(ActivitiConstants.ASSIGNEE_LIST);
                if (StringUtils.isNotEmpty(assigneeString)) {
                    assigneeSet.addAll(Arrays.asList(assigneeString.split(",")));
                }
                assigneeSet.add(task.getAssignee());
                variables.put(ActivitiConstants.ASSIGNEE_LIST, String.join(",", assigneeSet));
            }
            // 完成任务
            taskService.complete(task.getId(), variables);
        } catch (TreeBuilderException ex) {
            log.error("流程条件表达式错误:{}", ex.toString());
            throw new BusinessException("流程条件表达式错误:" + ex.getMessage());
        } catch (ActivitiException ex) {
            log.error("Activiti流程异常:{}", ex.toString());
            throw new BusinessException("Activiti流程异常:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("流程提交未知异常:{}", ex.toString());
            throw new BusinessException("流程提交未知异常!");
        }
    }

    /**
     * 待我审批
     *
     * @param dto 参数
     */
    @Override
    public PageResult<InstanceDto> queryTodoPage(instanceQueryDto dto) {
        String companyId = String.valueOf(UserUtils.getCompanyId());
        String departmentId = String.valueOf(UserUtils.getDepartmentId());
        String userId = String.valueOf(UserUtils.getCurrentUserId());
        String postId = String.valueOf(UserUtils.getPostId());

        List<String> usersGroups = new ArrayList<>();
        usersGroups.add(companyId + "-" + departmentId + "-" + postId);
        usersGroups.add(companyId + "-*-" + postId);

        TaskQuery query = taskService.createTaskQuery()
                .active()
                .taskCandidateOrAssigned(userId, usersGroups)
                .includeProcessVariables()
                .orderByTaskCreateTime()
                .desc();
        if (StringUtils.isNoneEmpty(dto.getBusinessKey())) {
            query.processInstanceBusinessKey(dto.getBusinessKey());
        }
        if (StringUtils.isNoneEmpty(dto.getProcessName())) {
            query.processDefinitionName(dto.getProcessName());
        }
        if (StringUtils.isNoneEmpty(dto.getVariable())) {
            query.processVariableValueEquals(dto.getVariable());
        }
        List<Task> list = query.listPage(dto.getPageNo() - 1, dto.getPageSize());
        long totalCount = query.count();

        List<InstanceDto> resultList = new ArrayList<>();
        for (Task task : list) {
            // 流程实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();

            InstanceDto instanceDto = new InstanceDto();
            instanceDto.setInstanceId(task.getProcessInstanceId())
                    .setInstanceName(instance.getName())
                    .setBusinessKey(instance.getBusinessKey())
                    .setProcessName(instance.getProcessDefinitionName())
                    .setVariables(task.getProcessVariables())
                    .setTaskId(task.getId())
                    .setTaskName(task.getName())
                    .setStartTime(task.getCreateTime());
            resultList.add(instanceDto);
        }
        return new PageResult<>(dto.getPageNo(), dto.getPageSize(), resultList, totalCount);
    }

    /**
     * 我已审批
     *
     * @param dto 参数
     * @return 结果
     */
    @Override
    public PageResult<InstanceDto> queryPage(instanceQueryDto dto) {
        String userId = String.valueOf(UserUtils.getCurrentUserId());

        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .includeProcessVariables()
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc();

        if (dto.getIsAgree() != null) {
            if (Boolean.TRUE.equals(dto.getIsAgree())) {
                query.processVariableValueEquals(ActivitiConstants.APPROVAL_RESULT, true);
            } else {
                query.processVariableValueEquals(ActivitiConstants.APPROVAL_RESULT, false);
            }
        }
        if (StringUtils.isNoneEmpty(dto.getBusinessKey())) {
            query.processInstanceBusinessKey(dto.getBusinessKey());
        }
        if (StringUtils.isNoneEmpty(dto.getProcessName())) {
            query.processDefinitionName(dto.getProcessName());
        }
        if (StringUtils.isNoneEmpty(dto.getVariable())) {
            query.processVariableValueEquals(dto.getVariable());
        }
        List<HistoricTaskInstance> list = query.listPage(dto.getPageNo() - 1, dto.getPageSize());
        long totalCount = query.count();

        List<InstanceDto> resultList = new ArrayList<>();
        for (HistoricTaskInstance taskInstance : list) {
            // 流程实例
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(taskInstance.getProcessInstanceId())
                    .singleResult();

            InstanceDto instanceDto = new InstanceDto();
            instanceDto.setInstanceId(taskInstance.getProcessInstanceId())
                    .setInstanceName(taskInstance.getName())
                    .setBusinessKey(instance.getBusinessKey())
                    .setProcessName(instance.getProcessDefinitionName())
                    .setVariables(taskInstance.getProcessVariables())
                    .setTaskId(taskInstance.getId())
                    .setTaskName(taskInstance.getName())
                    .setStartTime(taskInstance.getCreateTime());
            resultList.add(instanceDto);
        }
        return new PageResult<>(dto.getPageNo(), dto.getPageSize(), resultList, totalCount);
    }

    /**
     * 抄送给我
     *
     * @param dto 参数
     * @return 结果
     */
    @Override
    public PageResult<InstanceDto> queryCcPage(instanceQueryDto dto) {
        String userId = String.valueOf(UserUtils.getCurrentUserId());
        // 为抄送的userId加个前缀，避免和候选人冲突
        String ccUserId = ActivitiConstants.CC + userId;

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .involvedUser(ccUserId)
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
            resultList.add(instanceDto);
        }
        return new PageResult<>(dto.getPageNo(), dto.getPageSize(), resultList, totalCount);
    }
}
