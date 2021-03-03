package yuan.thymeleaf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import yuan.thymeleaf.demo.entity.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 用户控制器.
 *
 */
@Controller
public class ToolsController {

    @GetMapping("/trim")
    public String trim(Model model){
        return "trim";
    }


}
