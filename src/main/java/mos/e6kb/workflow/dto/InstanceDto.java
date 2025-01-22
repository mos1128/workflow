package mos.e6kb.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author ly
 * @since 2025/1/21
 */
@Data
@Accessors(chain = true)
public class InstanceDto implements Serializable {

    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 业务key
     */
    private String businessKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 是否可撤销
     */
    private Boolean canCancel;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 用户名称
     */
    private String userName;

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
