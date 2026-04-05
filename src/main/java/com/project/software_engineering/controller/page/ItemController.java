package com.project.software_engineering.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/item")
@Controller
public class ItemController {
    @GetMapping("/{page}")
    public String page(@PathVariable String page){
        return "item/" + page;
    }
    @GetMapping("/{page}/{id}")
    public String page(@PathVariable String page, @PathVariable String id){
        return "item/" + page;
    }
}