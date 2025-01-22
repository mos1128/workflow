package mos.e6kb.workflow.service;

import mos.e6kb.workflow.dto.DeployProcessDto;
import mos.e6kb.workflow.dto.DeployProcessQueryDto;
import mos.e6kb.workflow.dto.PageResult;

/**
 * 流程定义服务接口，提供流程部署、查询、节点管理等核心功能
 *
 * @author ly
 * @since 2025/1/21
 */
public interface ProcessDefinitionService {

    /**
     * 部署流程
     *
     * @param deployProcessDto 部署参数
     * @return 流程定义ID
     */
    String deployProcess(DeployProcessDto deployProcessDto);

    /**
     * 分页查询流程定义
     *
     * @param deployProcessQueryDto 查询参数
     * @return 分页结果
     */
    PageResult<DeployProcessDto> findProcessPage(DeployProcessQueryDto deployProcessQueryDto);

    /**
     * 获取流程节点信息
     *
     * @param processId 流程定义ID
     * @return 节点信息
     */
    String getProcessNode(String processId);

    /**
     * 更新流程状态
     *
     * @param processId 流程定义ID
     * @return 是否成功
     */
    Boolean updateStatus(String processId);
} 
