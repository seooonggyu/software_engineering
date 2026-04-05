package com.project.software_engineering.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/permission")
@Controller
public class PermissionController {
    @GetMapping("/{page}")
    public String page(@PathVariable String page){
        return "permission/" + page;
    }
    @GetMapping("/{page}/{id}")
    public String page(@PathVariable String page, @PathVariable String id){
        return "permission/" + page;
    }
}
