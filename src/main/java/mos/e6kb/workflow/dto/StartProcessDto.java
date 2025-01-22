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
public class StartProcessDto implements Serializable {

    /**
     * 流程id
     */
    private String processId;

    /**
     * 启动流程时的表单的参数
     */
    private Map<String, Object> variables;

    /**
     * 业务编号（仅业务流程需要）
     */
    private String businessKey;

    /**
     * 业务数据（仅业务流程需要）
     */
    private String businessJson;
}
