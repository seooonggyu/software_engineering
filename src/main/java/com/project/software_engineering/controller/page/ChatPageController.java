package com.project.software_engineering.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/chat")
public class ChatPageController {

    @GetMapping("/test")
    public String chatTest(Model model) {
        return "chat-test";
    }

    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable("roomId") Long roomId, Model model) {
        model.addAttribute("roomId", roomId);
        return "chat-room";
    }
}
