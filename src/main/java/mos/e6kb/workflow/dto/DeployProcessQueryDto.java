package mos.e6kb.workflow.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author ly
 * @since 2025/1/21
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeployProcessQueryDto extends DeployProcessDto {

    /**
     * 页码
     */
    private int pageNo = 1;

    /**
     * 每页大小
     */
    private int pageSize = 10;
}
