package yuan.thymeleaf.demo.service;


import org.springframework.ui.Model;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.entity.EntityToVo;
import yuan.thymeleaf.demo.entity.WordToConstant;

public interface ToolsService {

    /**
     * 对比内容
     */
    void compare(Model model, CompareJson compare);

    /**
     * 对比内容-老版本
     */
    void compareOld(Model model, CompareJson compare);

    /**
     * entity转dto
     */
    void entityToVo(Model model, EntityToVo entityToVo);

    /**
     * 单词转常量
     */
    String wordToConstant(WordToConstant wordToConstant);
}
