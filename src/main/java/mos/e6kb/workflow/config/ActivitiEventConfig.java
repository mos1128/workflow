package mos.e6kb.workflow.config;

import mos.e6kb.workflow.listener.GlobalTaskEventListener;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


/**
 * 配置全局监听器
 * @author ly
 * @since 2025/1/21
 */
@Configuration
public class ActivitiEventConfig {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private GlobalTaskEventListener globalTaskEventListener;

    @PostConstruct
    public void registerEventListeners() {
        // 注册全局事件监听器
        runtimeService.addEventListener(globalTaskEventListener);
    }
}
