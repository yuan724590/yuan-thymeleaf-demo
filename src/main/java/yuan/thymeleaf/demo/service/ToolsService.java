package yuan.thymeleaf.demo.service;


import org.springframework.ui.Model;
import yuan.thymeleaf.demo.entity.CompareJson;

public interface ToolsService {

    /**
     * 对比内容
     */
    void compare(Model model, CompareJson compare);
}
