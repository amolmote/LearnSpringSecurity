package com.abm.SpringSecurityDemo.controllers;

import com.abm.SpringSecurityDemo.entity.Employee;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class CsrfDemoController {

    ArrayList<Employee> list=new ArrayList<>(List.of(
            new Employee(1, "A"),
            new Employee(2,"B"),
            new Employee(3,"C")
    ));
    @GetMapping("/csrfToken")
    public CsrfToken getToken(HttpServletRequest http){
        return (CsrfToken) http.getAttribute("_csrf");
    }

    @PostMapping("/addEmployee")
    public ResponseEntity<List<Employee>> addEmployee(@RequestBody Employee emp){
        list.add(emp);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }
}
