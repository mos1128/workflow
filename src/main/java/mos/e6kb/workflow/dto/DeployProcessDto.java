package mos.e6kb.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ly
 * @since 2025/1/21
 */
@Data
@Accessors(chain = true)
public class DeployProcessDto implements Serializable {

    /**
     * 流程key
     */
    private String processKey;

    /**
     * 流程id（流程key:版本:部署id）
     */
    private String processId;

    /**
     * 部署id
     */
    private String deploymentId;

    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程状态
     */
    private Boolean status;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 流程图标
     */
    private String icon;

    /**
     * 图标颜色
     */
    private String color;

    /**
     * 可发起流程用户（为空不限制）
     */
    private String initiatorUsers;

    /**
     * 可发起流程用户组（为空不限制）
     */
    private String initiatorGroups;

    /**
     * 发起人自选的抄送人
     */
    private String ccUsersSelf;

    /**
     * 可撤销时间（为0不可撤销）
     */
    @NotNull(message = "可撤销时间不能为空")
    private Integer cancelTime;

    /**
     * 同一审批人在流程中重复出现时自动审批
     */
    @NotNull(message = "同一审批人在流程中重复出现时自动审批不能为空")
    private Boolean sameAutoApprove;

    /**
     * 是否系统流程
     */
    @NotNull(message = "是否系统流程不能为空")
    private Boolean isSystem;

    /**
     * 流程节点信息
     */
    @NotBlank(message = "流程节点信息不能为空")
    private String processNode;
}
