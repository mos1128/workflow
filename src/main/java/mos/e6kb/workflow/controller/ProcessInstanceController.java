package mos.e6kb.workflow.controller;

import mos.e6kb.workflow.controller.base.SimpleVo;
import mos.e6kb.workflow.dto.*;
import mos.e6kb.workflow.service.ProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例
 *
 * @author ly
 * @since 2025/1/21
 */
@RequestMapping("processInstance")
@RestController
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 我发起的任务列表
     *
     * @param dto 参数
     */
    @PostMapping("page")
    public PageResult<InstanceDto> page(@RequestBody instanceQueryDto dto) {
        return processInstanceService.page(dto);
    }

    /**
     * 启动流程
     *
     * @param dto 启动流程参数
     * @return 结果
     */
    @PostMapping("start")
    public SimpleVo<String> start(@RequestBody StartProcessDto dto) {
        return SimpleVo.ok(processInstanceService.startProcess(dto));
    }

    /**
     * 删除流程实例
     *
     * @param instanceId 流程实例id
     */
    @PostMapping("delete")
    public void delete(@RequestParam String instanceId) {
        processInstanceService.delete(instanceId);
    }

    /**
     * 流程详情
     *
     * @param instanceId
     * @return
     */
    @PostMapping("detail")
    public InstanceDto detail(@RequestParam String instanceId) {
        return processInstanceService.detail(instanceId);
    }

    /**
     * 流程进度
     *
     * @param instanceId
     * @return
     */
    @PostMapping("schedule")
    public List<InstanceNodeDto> schedule(@RequestParam String instanceId) {
        return processInstanceService.schedule(instanceId);
    }
} 
