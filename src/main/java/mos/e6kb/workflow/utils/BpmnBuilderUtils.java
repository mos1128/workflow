package mos.e6kb.workflow.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import mos.e6kb.workflow.constant.ActivitiConstants;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author ly
 * @since 2025/1/13
 */
public class BpmnBuilderUtils {

    private static Process process;

    public static BpmnModel build(String processKey, String name, String documentation, String processNodeJson) {
        BpmnModel model = new BpmnModel();
        process = new Process();

        JSONObject processNode = JSON.parseObject(processNodeJson, JSONObject.class);

        // 设置流程基本信息
        model.addProcess(process);
        process.setId(processKey);
        process.setName(name);
        process.setDocumentation(documentation);

        // 创建节点
        create(null, processNode);

        // 自动布局
        // new BpmnAutoLayout(model).execute();
        // 生成xml
        // String bpmnXml = new String(new BpmnXMLConverter().convertToXML(model));
        return model;
    }

    /**
     * 创建节点
     *
     * @param parentNodeId
     * @param flowNode
     * @return 最终节点id
     */
    private static String create(String parentNodeId, JSONObject flowNode) {
        if (flowNode == null) {
            return parentNodeId;
        }

        String nodeType = flowNode.getString("nodeType");
        if (ProcessNodeType.START_EVENT.isEqual(nodeType)) {
            return createStartEvent(flowNode);
        } else if (ProcessNodeType.EXCLUSIVE_GATEWAY.isEqual(nodeType)) {
            return createExclusiveGateway(parentNodeId, flowNode);
        } else if (ProcessNodeType.USER_TASK.isEqual(nodeType)) {
            return createUserTask(parentNodeId, flowNode);
        } else if (ProcessNodeType.END_EVENT.isEqual(nodeType)) {
            return createEndEvent(parentNodeId, flowNode);
        }
        return flowNode.getString("nodeId");
    }

    /**
     * 创建开始节点
     *
     * @param flowNode
     */
    private static String createStartEvent(JSONObject flowNode) {
        String nodeId = flowNode.getString("nodeId");
        StartEvent startEvent = new StartEvent();
        startEvent.setId(nodeId);
        startEvent.setName(flowNode.getString("nodeName"));
        process.addFlowElement(startEvent);
        return create(nodeId, flowNode.getJSONObject("nextNode"));
    }

    /**
     * 创建连线
     *
     * @param parentNodeId
     * @param nextNodeId
     */
    private static void connect(String parentNodeId, String nextNodeId, String condition) {
        // 避免自引用
        if (parentNodeId.equals(nextNodeId)) {
            throw new RuntimeException("存在自引用节点");
        }

        // 生成id
        String flowId = "flow_" + parentNodeId + "_" + nextNodeId;

        // 先检查是否已经存在该ID的连线
        FlowElement existingElement = process.getFlowElement(flowId);
        if (existingElement instanceof SequenceFlow) {
            throw new RuntimeException("存在重复连线");
        }

        // 如果不存在，创建新的连线
        SequenceFlow flow = new SequenceFlow();
        flow.setId(flowId);
        flow.setName(flowId);
        flow.setSourceRef(parentNodeId);
        flow.setTargetRef(nextNodeId);

        // 设置条件表达式
        if (StringUtils.isNotEmpty(condition)) {
            flow.setConditionExpression(condition);
        }
        process.addFlowElement(flow);
    }

    /**
     * 创建结束节点
     *
     * @param parentNodeId
     * @param flowNode
     */
    private static String createEndEvent(String parentNodeId, JSONObject flowNode) {
        String nodeId = flowNode.getString("nodeId");
        EndEvent endEvent = new EndEvent();
        endEvent.setId(nodeId);
        endEvent.setName(flowNode.getString("nodeName"));
        process.addFlowElement(endEvent);
        // 创建连线
        connect(parentNodeId, nodeId, flowNode.getString("condition"));
        return "end";
    }

    /**
     * 创建排他网关
     *
     * @param parentNodeIdId
     * @param flowNode
     */
    private static String createExclusiveGateway(String parentNodeIdId, JSONObject flowNode) {
        // 创建分支节点
        String nodeId = flowNode.getString("nodeId");
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(nodeId);
        exclusiveGateway.setName(flowNode.getString("nodeName"));
        process.addFlowElement(exclusiveGateway);
        // 创建连线
        connect(parentNodeIdId, nodeId, flowNode.getString("condition"));

        // 处理分支
        List<String> lastNodeIdList = new ArrayList<>();
        List<JSONObject> branchNodes = Optional.ofNullable(flowNode.getJSONArray("branchNodes"))
                .map(e -> e.toJavaList(JSONObject.class))
                .orElse(Collections.emptyList());
        for (JSONObject branchNode : branchNodes) {
            // 创建分支下的节点
            String lastNodeId = create(nodeId, branchNode);
            // 连接未结束的分支到汇聚网关
            if (!"end".equals(lastNodeId)) {
                lastNodeIdList.add(lastNodeId);
            }
        }

        // 创建合并节点
        JSONObject mergeNode = flowNode.getJSONObject("mergeNode");
        if (mergeNode != null) {
            String mergeNodeId = mergeNode.getString("nodeId");
            ExclusiveGateway mergeGateway = new ExclusiveGateway();
            mergeGateway.setId(mergeNodeId);
            mergeGateway.setName(mergeNode.getString("nodeName"));
            process.addFlowElement(mergeGateway);
            for (String lastNodeId : lastNodeIdList) {
                connect(lastNodeId, mergeNodeId, mergeNode.getString("condition"));
            }
            // 处理汇聚节点的后续节点
            return create(mergeNodeId, mergeNode.getJSONObject("nextNode"));
        }

        return create(nodeId, null);
    }

    /**
     * 创建用户节点
     *
     * @param parentNodeId
     * @param flowNode
     */
    private static String createUserTask(String parentNodeId, JSONObject flowNode) {
        String nodeId = flowNode.getString("nodeId");
        UserTask userTask = new UserTask();
        userTask.setId(nodeId);
        userTask.setName(flowNode.getString("nodeName"));

        // 设置审批人
        Optional.ofNullable(flowNode.getJSONArray("candidateUsers"))
                .map(e -> e.toJavaList(String.class))
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(userTask::setCandidateUsers);

        // 设置审批组
        Optional.ofNullable(flowNode.getJSONArray("candidateGroups"))
                .map(e -> e.toJavaList(String.class))
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(userTask::setCandidateGroups);

        // 存储节点配置
        addAttribute(userTask, ActivitiConstants.SAME_DEPARTMENT, flowNode.get(ActivitiConstants.SAME_DEPARTMENT));
        addAttribute(userTask, ActivitiConstants.ALLOW_SELF, flowNode.get(ActivitiConstants.ALLOW_SELF));
        addAttribute(userTask, ActivitiConstants.CC_USERS_ATTRIBUTE, flowNode.get(ActivitiConstants.CC_USERS_ATTRIBUTE));

        process.addFlowElement(userTask);

        // 创建连线
        connect(parentNodeId, nodeId, flowNode.getString("condition"));
        return create(nodeId, flowNode.getJSONObject("nextNode"));
    }

    private static void addAttribute(BaseElement element, String name, Object value) {
        if (value == null) {
            return;
        }
        if (element.getAttributes() == null) {
            element.setAttributes(new HashMap<>());
        }

        ExtensionAttribute attribute = new ExtensionAttribute("http://activiti.org/bpmn", name);
        attribute.setValue(String.valueOf(value));

        List<ExtensionAttribute> attributes = element.getAttributes().computeIfAbsent(name, k -> new ArrayList<>());
        attributes.add(attribute);
    }

    enum ProcessNodeType {

        /**
         * 开始事件
         */
        START_EVENT("startEvent"),

        /**
         * 排他事件
         */
        EXCLUSIVE_GATEWAY("exclusiveGateway"),

        /**
         * 并行事件
         */
        PARALLEL_GATEWAY("parallelGateway"),

        /**
         * 用户任务
         */
        USER_TASK("userTask"),

        /**
         * 服务任务
         */
        SERVICE_TASK("serviceTask"),

        /**
         * 结束事件
         */
        END_EVENT("endEvent");

        private final String type;

        ProcessNodeType(String type) {
            this.type = type;
        }

        public boolean isEqual(String type) {
            return this.type.equals(type);
        }
    }
}
