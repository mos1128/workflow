package mos.e6kb.workflow.listener;

import mos.e6kb.workflow.constant.ActivitiConstants;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 全局事件监听器
 *
 * @author ly
 * @since 2024/12/11
 */
@Component
public class GlobalTaskEventListener implements ActivitiEventListener {

    private final static String NAME_SPACE = "http://activiti.org/bpmn";

    @Autowired
    private TaskService taskService;

    @Override
    public void onEvent(ActivitiEvent event) {
        // 任务创建
        if (event.getType() == ActivitiEventType.TASK_CREATED) {
            ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
            TaskEntity taskEntity = (TaskEntity) entityEvent.getEntity();
            UserTask userTask = (UserTask) taskEntity.getExecution().getCurrentFlowElement();

            // 是否抄送任务
            String allowSelf = userTask.getAttributeValue(NAME_SPACE, ActivitiConstants.ALLOW_SELF);
            String ccUsersAttribute = userTask.getAttributeValue(NAME_SPACE, ActivitiConstants.CC_USERS_ATTRIBUTE);
            if (allowSelf != null || ccUsersAttribute != null) {
                if (ccUsersAttribute != null) {
                    for (String ccUserId : ccUsersAttribute.split(",")) {
                        // 为抄送的userId加个前缀，避免和候选人冲突
                        ccUserId = ActivitiConstants.CC + ccUserId;
                        taskEntity.addUserIdentityLink(ccUserId, ActivitiConstants.CC);
                    }
                } else {
                    String ccUsersVariable = (String) taskEntity.getVariable(ActivitiConstants.CC_USERS_SELF);
                    if (ccUsersVariable != null) {
                        for (String ccUserId : ccUsersVariable.split(",")) {
                            ccUserId = ActivitiConstants.CC + ccUserId;
                            taskEntity.addUserIdentityLink(ccUserId, ActivitiConstants.CC);
                        }
                    }
                }
                taskService.complete(taskEntity.getId());
                return;
            }

            // 同一审批人重复出现时自动审批
            String assigneeString = (String) taskEntity.getVariable(ActivitiConstants.ASSIGNEE_LIST);
            if (StringUtils.isNotEmpty(assigneeString)) {
                List<String> candidateUsers = userTask.getCandidateUsers();
                for (String assignee : List.of(assigneeString.split(","))) {
                    if (candidateUsers.contains(assignee)) {
                        taskService.complete(taskEntity.getId());
                        return;
                    }
                }
            }

            // 获取流程定义的候选组
            List<String> candidateGroups = userTask.getCandidateGroups();
            if (!candidateGroups.isEmpty()) {
                String companyId = (String) taskEntity.getVariable(ActivitiConstants.COMPANY_ID);
                String departmentId = (String) taskEntity.getVariable(ActivitiConstants.DEPARTMENT_ID);
                // 删除原有候选组
                for (IdentityLink candidate : taskEntity.getCandidates()) {
                    taskEntity.deleteCandidateGroup(candidate.getGroupId());
                }
                // 是否同部门审批
                String sameDeptApproval = userTask.getAttributeValue(NAME_SPACE, ActivitiConstants.SAME_DEPARTMENT);
                if (Boolean.TRUE.toString().equals(sameDeptApproval)) {
                    candidateGroups.stream()
                            .map(groupId -> companyId + "-" + departmentId + "-" + groupId)
                            .forEach(taskEntity::addCandidateGroup);
                } else {
                    candidateGroups.stream()
                            .map(groupId -> companyId + "-*-" + groupId)
                            .forEach(taskEntity::addCandidateGroup);
                }
            }
        }

        // 流程结束
        if (event.getType() == ActivitiEventType.PROCESS_COMPLETED) {
            ActivitiEntityEvent entityEvent = (ActivitiEntityEvent) event;
            ExecutionEntity executionEntity = (ExecutionEntity) entityEvent.getEntity();
            String processInstanceId = executionEntity.getProcessInstanceId();
            String businessKey = executionEntity.getBusinessKey();
            Boolean approvalResult = (Boolean) executionEntity.getVariable(ActivitiConstants.APPROVAL_RESULT);
            // 存在业务类型再发送通知
            if (businessKey != null) {
                // todo 通知业务系统
            }
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
