package com.meng.custom.controller;

import com.meng.custom.annotion.Controller;
import com.meng.custom.annotion.Qualifier;
import com.meng.custom.annotion.RequestMapping;
import com.meng.custom.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//未设置method  和controller关联 ，只支持Controller和RequestMapping value一致
@Controller("/userController")
@RequestMapping("/userController")
public class UserController {

    @Qualifier("userService")
    private UserService userService;

    @RequestMapping("/addUser")
    public String addUser(HttpServletRequest request, HttpServletResponse response){
        userService.addUser();
        return "ok成功";
    }

}
