package yuan.thymeleaf.demo.service;


import org.springframework.ui.Model;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.entity.EntityToVo;

public interface ToolsService {

    /**
     * 对比内容
     */
    void compare(Model model, CompareJson compare);

    /**
     * entity转dto
     */
    void entityToVo(Model model, EntityToVo entityToVo);
}
