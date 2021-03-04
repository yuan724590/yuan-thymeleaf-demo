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
public class UserController {

    @GetMapping("/users")
    public String getUser(Model model){

        // list中存放 用户
        List<User> userList = new ArrayList<>();
        for (long i = 0; i < 5; i++){
            User user = new User();
            user.setId(i);
            user.setAge(18);
            user.setName("知识追寻者");
            user.setBirthday(new Date());
            userList.add(user);
        }
        // 为视图添加用户
        model.addAttribute("users", userList);
        // 逻辑视图为 user 即在 templates 下的 user.html
        return "user";
    }


}
