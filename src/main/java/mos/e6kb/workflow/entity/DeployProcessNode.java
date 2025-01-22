package mos.e6kb.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 流程部署节点表
 * </p>
 *
 * @author lin
 * @since 2025-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeployProcessNode implements Serializable {

    /**
     * 流程id（流程key:版本:随机值）
     */
    @TableId(value = "process_id", type = IdType.INPUT)
    private String processId;

    /**
     * 流程节点信息
     */
    private String processNode;

}
