package com.abm.SpringSecurityDemo.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomepageController {

    @GetMapping("/")
    public String homePath(){
        return "Welcome to Spring Security";
    }
    @GetMapping("/sessionId")
    public String sessionIdDemo(HttpServletRequest request){
        return "Session id is: "+request.getSession().getId();
    }

}
