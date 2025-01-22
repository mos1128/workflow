package mos.e6kb.workflow.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @author ly
 * @since 2025/1/21
 */
@Data
@Accessors(chain = true)
public class TodoCompleteDto implements Serializable {

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 完成节点时的表单的参数
     */
    private Map<String, Object> variables;
} 
