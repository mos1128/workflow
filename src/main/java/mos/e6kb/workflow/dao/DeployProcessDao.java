package mos.e6kb.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import mos.e6kb.workflow.entity.DeployProcess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程定义
 * @author ly
 * @since 2025/1/20
 */
@Mapper
public interface DeployProcessDao extends BaseMapper<DeployProcess> {
} 
