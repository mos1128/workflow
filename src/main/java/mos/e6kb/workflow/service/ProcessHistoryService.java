package mos.e6kb.workflow.service;

import mos.e6kb.workflow.dto.InstanceDto;
import mos.e6kb.workflow.dto.PageResult;
import mos.e6kb.workflow.dto.TodoCompleteDto;
import mos.e6kb.workflow.dto.instanceQueryDto;

/**
 * 流程历史服务接口，提供任务完成、待办查询、已办查询等功能
 *
 * @author ly
 * @since 2025/1/21
 */
public interface ProcessHistoryService {

    /**
     * 完成任务
     *
     * @param dto 完成参数
     */
    void complete(TodoCompleteDto dto);

    /**
     * 待我审批
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult<InstanceDto> queryTodoPage(instanceQueryDto dto);

    /**
     * 我已审批
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult<InstanceDto> queryPage(instanceQueryDto dto);

    /**
     * 抄送给我
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    PageResult<InstanceDto> queryCcPage(instanceQueryDto dto);
} 
