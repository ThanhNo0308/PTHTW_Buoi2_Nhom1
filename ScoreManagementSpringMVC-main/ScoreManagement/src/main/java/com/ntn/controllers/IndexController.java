/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// Controller Trang chủ
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/check-path")
    @ResponseBody
    public String checkPath(HttpServletRequest request) {
        return "Context path: " + request.getContextPath()
                + "<br>Servlet path: " + request.getServletPath()
                + "<br>Request URI: " + request.getRequestURI();
    }
}
