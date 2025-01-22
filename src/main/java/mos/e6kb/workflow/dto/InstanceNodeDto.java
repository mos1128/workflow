package mos.e6kb.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author ly
 * @since 2025/1/15
 */
@Data
@Accessors(chain = true)
public class InstanceNodeDto {

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 流程变量
     */
    private String variables;

    /**
     * 节点是否完成
     */
    private Boolean isFinish;

    /**
     * 候选用户/组
     */
    private String candidateInfo;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
}
