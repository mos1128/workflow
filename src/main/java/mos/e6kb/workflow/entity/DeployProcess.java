package mos.e6kb.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 流程部署表
 * </p>
 *
 * @author lin
 * @since 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeployProcess implements Serializable {

    /**
     * 流程key
     */
    @TableId(value = "process_key", type = IdType.INPUT)
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
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程状态(1:禁用,0:启用)
     */
    private Boolean status;

    /**
     * 最后更新时间
     */
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
     * 可发起流程用户
     */
    private String initiatorUsers;

    /**
     * 可发起流程用户组
     */
    private String initiatorGroups;

    /**
     * 发起人自选的抄送人
     */
    private String ccUsersSelf;

    /**
     * 可撤销时间(0:不可撤销)
     */
    private Integer cancelTime;

    /**
     * 同一审批人重复出现时自动审批(0:否,1:是)
     */
    private Boolean sameAutoApprove;

    /**
     * 是否系统流程(0:否,1:是)
     */
    private Boolean isSystem;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 是否删除 0-未删除 1-删除
     */
    @TableLogic
    private Boolean deleted;

}
