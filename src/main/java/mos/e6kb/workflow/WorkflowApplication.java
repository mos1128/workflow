package mos.e6kb.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 工作流应用程序启动类
 * @author ly
 * @since 2025/1/21
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }

}
