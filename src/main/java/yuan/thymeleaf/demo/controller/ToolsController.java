package yuan.thymeleaf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import yuan.thymeleaf.demo.entity.CompareJson;
import yuan.thymeleaf.demo.entity.Trim;
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
}
