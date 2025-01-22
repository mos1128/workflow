package mos.e6kb.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import mos.e6kb.workflow.dao.DeployProcessDao;
import mos.e6kb.workflow.dao.DeployProcessNodeDao;
import mos.e6kb.workflow.dto.DeployProcessDto;
import mos.e6kb.workflow.dto.DeployProcessQueryDto;
import mos.e6kb.workflow.dto.PageResult;
import mos.e6kb.workflow.entity.DeployProcess;
import mos.e6kb.workflow.entity.DeployProcessNode;
import mos.e6kb.workflow.exception.BusinessException;
import mos.e6kb.workflow.service.ProcessDefinitionService;
import mos.e6kb.workflow.utils.BpmnBuilderUtils;
import mos.e6kb.workflow.utils.UserUtils;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: lilin
 * @CreateTime: 2024-12-06  15:36
 * @Description: 流程
 */
@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private DeployProcessDao deployProcessDao;

    @Autowired
    private DeployProcessNodeDao deployProcessNodeDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployProcess(DeployProcessDto dto) {

        BpmnModel bpmnModel = BpmnBuilderUtils.build(dto.getProcessKey(), dto.getName(), dto.getDescription(), dto.getProcessNode());

        // 部署流程
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(dto.getName() + ".bpmn", bpmnModel)
                .deploy();

        // 根据部署ID查询流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        // 记录流程信息
        DeployProcess deployProcess = new DeployProcess();
        deployProcess.setProcessKey(processDefinition.getKey())
                .setProcessId(processDefinition.getId())
                .setDeploymentId(deployment.getId())
                .setName(processDefinition.getName())
                .setDescription(processDefinition.getDescription())
                .setStatus(!processDefinition.isSuspended())
                .setUpdateTime(LocalDateTime.now())
                .setIcon(dto.getIcon())
                .setColor(dto.getColor())
                .setInitiatorUsers(dto.getInitiatorUsers())
                .setInitiatorGroups(dto.getInitiatorGroups())
                .setCcUsersSelf(dto.getCcUsersSelf())
                .setCancelTime(dto.getCancelTime())
                .setSameAutoApprove(dto.getSameAutoApprove())
                .setIsSystem(dto.getIsSystem());
        Long count = deployProcessDao.selectCount(new LambdaQueryWrapper<DeployProcess>()
                .eq(DeployProcess::getProcessKey, deployProcess.getProcessKey()));
        if (count > 0) {
            deployProcessDao.updateById(deployProcess);
        } else {
            deployProcessDao.insert(deployProcess);
        }

        // 记录流程节点
        DeployProcessNode deployProcessNode = new DeployProcessNode();
        deployProcessNode.setProcessId(processDefinition.getId())
                .setProcessNode(dto.getProcessNode());
        deployProcessNodeDao.insert(deployProcessNode);

        return processDefinition.getId();
    }

    @Override
    public PageResult<DeployProcessDto> findProcessPage(DeployProcessQueryDto dto) {
        String userId = String.valueOf(UserUtils.getCurrentUserId());
        String postId = String.valueOf(UserUtils.getPostId());

        Page<DeployProcess> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        Page<DeployProcess> deployProcessPage = deployProcessDao.selectPage(page, new LambdaQueryWrapper<DeployProcess>()
                .eq(Objects.nonNull(dto.getStatus()), DeployProcess::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getIsSystem()), DeployProcess::getIsSystem, dto.getIsSystem())
                .and(wrapper -> wrapper
                        .apply("FIND_IN_SET({0}, initiator_users)", userId)
                        .or()
                        .apply("FIND_IN_SET({0}, initiator_groups)", postId)
                ));
        List<DeployProcessDto> processDtoList = new ArrayList<>();
        for (DeployProcess record : deployProcessPage.getRecords()) {
            DeployProcessDto deployProcessDto = new DeployProcessDto();
            BeanUtils.copyProperties(record, deployProcessDto);
            processDtoList.add(deployProcessDto);
        }
        return new PageResult<>(dto.getPageNo(), dto.getPageSize(), processDtoList, page.getTotal());
    }

    @Override
    public String getProcessNode(String processId) {
        DeployProcessNode deployProcessNode = deployProcessNodeDao.selectOne(new LambdaQueryWrapper<DeployProcessNode>()
                .eq(DeployProcessNode::getProcessId, processId));
        if (Objects.isNull(deployProcessNode)) {
            throw new BusinessException("流程不存在");
        }
        return deployProcessNode.getProcessNode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(String processId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processId)
                .singleResult();
        if (processDefinition == null) {
            throw new BusinessException("流程不存在");
        }

        DeployProcess deployProcess = deployProcessDao.selectOne(new LambdaQueryWrapper<DeployProcess>()
                .eq(DeployProcess::getProcessId, processId));
        if (deployProcess != null && Boolean.TRUE.equals(deployProcess.getIsSystem())) {
            throw new BusinessException("系统流程不允许修改状态");
        }

        boolean isSuspended = repositoryService.isProcessDefinitionSuspended(processDefinition.getId());
        if (isSuspended) {
            repositoryService.activateProcessDefinitionById(processDefinition.getId());
        } else {
            repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        }

        deployProcessDao.update(null, new LambdaUpdateWrapper<DeployProcess>()
                .eq(DeployProcess::getProcessId, processId)
                .set(DeployProcess::getStatus, isSuspended));
        return isSuspended;
    }

}
