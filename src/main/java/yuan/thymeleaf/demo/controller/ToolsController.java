package yuan.thymeleaf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.entity.EntityToVo;
import yuan.thymeleaf.demo.entity.Trim;
import yuan.thymeleaf.demo.entity.WordToConstant;
import yuan.thymeleaf.demo.service.ToolsService;

import javax.annotation.Resource;


/**
 * 用户控制器.
 *
 */
@Controller
public class ToolsController {

    @Resource
    private ToolsService toolsService;

    @GetMapping("/index")
    public String index(Model model){
        return "index";
    }

    @GetMapping("/trim")
    public String trim(Model model){
        model.addAttribute("trim", new Trim());
        return "trim";
    }

    @PostMapping("/trim")
    public String trim(Model model, @ModelAttribute Trim trim){
        trim.setContent(trim.getContent().replaceAll("\\s",""));
        model.addAttribute("trim", trim);
        return "trim";
    }

    /**
     * 比较内容
     */
    @GetMapping("/compare")
    public String compare(Model model){
        CompareJson compareJson = new CompareJson();
        model.addAttribute("compare", compareJson);
        return "compare";
    }

    @PostMapping("/compare")
    public String compare(Model model, @ModelAttribute("compare") CompareJson compare){
        toolsService.compare(model, compare);
        return "compare";
    }

    /**
     * 比较内容-老版本
     */
    @GetMapping("/compareOld")
    public String compareOld(Model model){
        CompareJson compareJson = new CompareJson();
        model.addAttribute("compare", compareJson);
        return "compare";
    }

    @PostMapping("/compareOld")
    public String compareOld(Model model, @ModelAttribute("compare") CompareJson compare){
        toolsService.compareOld(model, compare);
        return "compare";
    }

    /**
     * entity转vo
     */
    @GetMapping("/entityToVo")
    public String entityToVo(Model model){
        EntityToVo entityToVo = new EntityToVo();
        model.addAttribute("entityToVo", entityToVo);
        return "entityToVo";
    }

    @PostMapping("/entityToVo")
    public String entityToDto(Model model, @ModelAttribute("entityToVo") EntityToVo entityToVo){
        toolsService.entityToVo(model, entityToVo);
        return "entityToVo";
    }

    /**
     * 单词转常量
     */
    @GetMapping("/wordToConstant")
    public String wordToConstant(Model model){
        WordToConstant wordToConstant = new WordToConstant();
        model.addAttribute("wordToConstant", wordToConstant);
        return "wordToConstant";
    }

    @PostMapping("/wordToConstant")
    public String wordToConstant(Model model, @ModelAttribute("wordToConstant") WordToConstant wordToConstant){
        String constantContent = toolsService.wordToConstant(wordToConstant);
        model.addAttribute("constantContent", constantContent);
        return "wordToConstant";
    }
}
