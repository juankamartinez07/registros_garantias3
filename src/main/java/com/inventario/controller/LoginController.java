package com.inventario.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(){

        return "login";

    }

    @GetMapping("/logout-inactividad")
    public String logoutInactividad(HttpServletRequest request){

        HttpSession session = request.getSession(false);

        if(session != null){
            session.invalidate();
        }

        return "redirect:/login?timeout";

    }

}
