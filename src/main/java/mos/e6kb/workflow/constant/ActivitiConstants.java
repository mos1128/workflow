package mos.e6kb.workflow.constant;

/**
 * 常量参数 - 前端的表单命名需要避开这些参数
 *
 * @author ly
 * @since 2025/1/20
 */
public class ActivitiConstants {

    /**
     * 节点参数前缀-流程变量名
     */
    public final static String NODE_ = "node_";

    /**
     * 业务数据-流程变量名
     */
    public final static String BUSINESS_JSON = "businessJson";

    /**
     * 自选抄送人-流程变量名
     */
    public final static String CC_USERS_SELF = "ccUsersSelf";

    /**
     * 同一审批人重复出现时自动审批-流程变量名
     */
    public final static String SAME_AUTO_APPROVE = "sameAutoApprove";

    /**
     * 审批人列表
     */
    public final static String ASSIGNEE_LIST = "assigneeList";

    /**
     * 可撤销时间-流程变量名
     */
    public final static String CANCEL_TIME = "cancelTime";

    /**
     * 发起人id-流程变量名
     */
    public final static String INITIATOR = "initiator";

    /**
     * 发起人公司id-流程变量名
     */
    public final static String COMPANY_ID = "companyId";

    /**
     * 发起人部门id-流程变量名
     */
    public final static String DEPARTMENT_ID = "departmentId";

    /**
     * 审批结果-流程变量名
     */
    public final static String APPROVAL_RESULT = "approvalResult";

    /**
     * 是否同部门审批-属性变量名
     */
    public final static String SAME_DEPARTMENT = "sameDepartment";

    /**
     * 允许自选抄送人-属性变量名
     */
    public final static String ALLOW_SELF = "allowSelf";

    /**
     * 模板指定抄送人-属性变量名
     */
    public final static String CC_USERS_ATTRIBUTE = "ccUsers";

    /**
     * 抄送类型-身份类型变量名
     */
    public final static String CC = "cc";

}
