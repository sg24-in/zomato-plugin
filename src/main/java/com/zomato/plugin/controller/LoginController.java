package com.zomato.plugin.controller;

import com.zomato.plugin.service.ConnectionService;
import com.zomato.plugin.service.OrderService;
import com.zomato.plugin.service.PlaywrightService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final ConnectionService connectionService;
    private final PlaywrightService playwrightService;
    private final OrderService orderService;

    public LoginController(ConnectionService connectionService,
                          PlaywrightService playwrightService,
                          OrderService orderService) {
        this.connectionService = connectionService;
        this.playwrightService = playwrightService;
        this.orderService = orderService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("connected", connectionService.isConnected());
        return "login";
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String username,
                         @RequestParam String password,
                         RedirectAttributes redirectAttributes) {
        try {
            String sessionPath = playwrightService.login(username, password);
            connectionService.connect(username, sessionPath);
            playwrightService.setOrderCallback(order -> orderService.saveOrder(order));
            playwrightService.startListening();
            redirectAttributes.addFlashAttribute("message", "Connected successfully");
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/disconnect")
    public String disconnect(@RequestParam String username,
                            RedirectAttributes redirectAttributes) {
        try {
            playwrightService.stopListening();
            connectionService.disconnect(username);
            redirectAttributes.addFlashAttribute("message", "Disconnected successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Disconnect failed: " + e.getMessage());
        }
        return "redirect:/login";
    }
}
