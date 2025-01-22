package mos.e6kb.workflow.controller;

import mos.e6kb.workflow.controller.base.SimpleVo;
import mos.e6kb.workflow.dto.DeployProcessDto;
import mos.e6kb.workflow.dto.DeployProcessQueryDto;
import mos.e6kb.workflow.dto.PageResult;
import mos.e6kb.workflow.exception.BusinessException;
import mos.e6kb.workflow.service.ProcessDefinitionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 流程定义
 *
 * @author ly
 * @since 2025/1/21
 */
@RequestMapping("processDefinition")
@RestController
public class ProcessDefinitionController {

    @Resource
    private ProcessDefinitionService processDefinitionService;

    /**
     * 部署/修改流程
     *
     * @param deployProcessDto
     */
    @PostMapping("deployProcess")
    public SimpleVo<String> deployProcess(@Valid @RequestBody DeployProcessDto deployProcessDto) {
        return SimpleVo.ok(processDefinitionService.deployProcess(deployProcessDto));
    }

    /**
     * 分页
     *
     * @param deployProcessQueryDto
     */
    @PostMapping("findProcessPage")
    public PageResult<DeployProcessDto> findProcessPage(@RequestBody DeployProcessQueryDto deployProcessQueryDto) {
        return processDefinitionService.findProcessPage(deployProcessQueryDto);
    }

    /**
     * 获取流程定义节点信息
     *
     * @param deployProcessDto
     */
    @PostMapping("getProcessNode")
    public SimpleVo<String> getProcessNode(@RequestBody DeployProcessDto deployProcessDto) {
        if (deployProcessDto.getProcessId() == null) {
            throw new BusinessException("流程id不能为空");
        }
        return SimpleVo.ok(processDefinitionService.getProcessNode(deployProcessDto.getProcessId()));
    }

    /**
     * 更新流程定义状态 激活或者挂起
     *
     * @param deployProcessDto
     */
    @PostMapping("updateStatus")
    public Boolean updateStatus(@RequestBody DeployProcessDto deployProcessDto) {
        if (deployProcessDto.getProcessId() == null) {
            throw new BusinessException("流程id不能为空");
        }
        return processDefinitionService.updateStatus(deployProcessDto.getProcessId());
    }
}
