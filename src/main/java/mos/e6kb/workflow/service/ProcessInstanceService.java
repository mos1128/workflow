package mos.e6kb.workflow.service;

import mos.e6kb.workflow.dto.*;

import java.util.List;

/**
 * 流程实例服务接口
 * 提供流程实例的创建、查询、管理等核心功能
 *
 * @author ly
 * @since 2025/1/21
 */
public interface ProcessInstanceService {
    /**
     * 分页查询我发起的流程
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult<InstanceDto> page(instanceQueryDto dto);

    /**
     * 启动流程
     *
     * @param dto 启动参数
     * @return 流程实例ID
     */
    String startProcess(StartProcessDto dto);

    /**
     * 删除流程实例
     *
     * @param instanceId 实例ID
     */
    void delete(String instanceId);

    /**
     * 获取流程详情
     *
     * @param instanceId 实例ID
     * @return 流程详情
     */
    InstanceDto detail(String instanceId);

    /**
     * 获取流程进度
     *
     * @param instanceId 实例ID
     * @return 节点列表
     */
    List<InstanceNodeDto> schedule(String instanceId);
} 
