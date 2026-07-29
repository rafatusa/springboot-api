package com.example.springbootapi.controller;

import com.example.springbootapi.dto.ApiResponse;
import com.example.springbootapi.service.ItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Home controller providing API info at the root endpoint.
 * The static index.html in resources/static serves the UI at '/'.
 */
@RestController
public class HomeController {

    private final ItemService itemService;

    public HomeController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/api/v1/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", "springboot-api");
        data.put("version", "1.0.0");
        data.put("description", "Enterprise Spring Boot REST API");
        data.put("itemCount", itemService.count());
        data.put("endpoints", new String[]{
            "GET  /api/v1/items",
            "GET  /api/v1/items/{id}",
            "POST /api/v1/items",
            "PUT  /api/v1/items/{id}",
            "DELETE /api/v1/items/{id}",
            "GET  /actuator/health",
            "GET  /actuator/info"
        });
        return ApiResponse.ok(data);
    }
}
