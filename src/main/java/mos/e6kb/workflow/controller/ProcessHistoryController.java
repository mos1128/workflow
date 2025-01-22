package mos.e6kb.workflow.controller;

import mos.e6kb.workflow.dto.InstanceDto;
import mos.e6kb.workflow.dto.PageResult;
import mos.e6kb.workflow.dto.TodoCompleteDto;
import mos.e6kb.workflow.dto.instanceQueryDto;
import mos.e6kb.workflow.service.ProcessHistoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 流程历史
 *
 * @author ly
 * @since 2025/1/21
 */
@RequestMapping("processHistory")
@RestController
public class ProcessHistoryController {

    @Resource
    private ProcessHistoryService processHistoryService;

    /**
     * 审批节点
     *
     * @param dto 参数
     */
    @PostMapping("complete")
    public void complete(@RequestBody TodoCompleteDto dto) {
        processHistoryService.complete(dto);
    }

    /**
     * 待我审批
     *
     * @param dto 参数
     */
    @PostMapping("queryTodoPage")
    public PageResult<InstanceDto> queryTodoPage(@RequestBody instanceQueryDto dto) {
        return processHistoryService.queryTodoPage(dto);
    }

    /**
     * 我已审批
     *
     * @param dto 参数
     */
    @PostMapping("queryPage")
    public PageResult<InstanceDto> queryPage(@RequestBody instanceQueryDto dto) {
        return processHistoryService.queryPage(dto);
    }

    /**
     * 抄送给我
     *
     * @param dto 参数
     */
    @PostMapping("queryCcPage")
    public PageResult<InstanceDto> queryCcPage(@RequestBody instanceQueryDto dto) {
        return processHistoryService.queryCcPage(dto);
    }
}
