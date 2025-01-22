package mos.e6kb.workflow.dto;

import lombok.Data;

/**
 * @author ly
 * @since 2025/1/21
 */
@Data
public class instanceQueryDto {

    /**
     * 页码
     */
    private int pageNo = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;

    /**
     * 业务key
     */
    private String businessKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 搜索变量
     */
    private String variable;

    /**
     * 是否完成
     */
    private Boolean isFinish;

    /**
     * 是否同意
     */
    private Boolean isAgree;

}
