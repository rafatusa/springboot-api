package com.example.springbootapi.controller;

import com.example.springbootapi.dto.ApiResponse;
import com.example.springbootapi.dto.ItemRequest;
import com.example.springbootapi.model.Item;
import com.example.springbootapi.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for Item CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Item>>> listItems() {
        List<Item> items = itemService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItem(@PathVariable Long id) {
        Item item = itemService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@Valid @RequestBody ItemRequest request) {
        Item created = itemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        Item updated = itemService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Item updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted successfully", null));
    }
}
