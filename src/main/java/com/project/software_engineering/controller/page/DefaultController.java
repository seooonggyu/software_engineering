package com.project.software_engineering.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DefaultController {
    @GetMapping({"/index", "", "/"})
    public String index(){
        return "index";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    @GetMapping("/{page}")
    public String page(@PathVariable String page){
        return page;
    }

}