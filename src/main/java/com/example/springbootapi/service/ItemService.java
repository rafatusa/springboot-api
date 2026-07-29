package com.example.springbootapi.service;

import com.example.springbootapi.dto.ItemRequest;
import com.example.springbootapi.model.Item;
import com.example.springbootapi.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Business logic layer for Item operations.
 */
@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));
    }

    public Item create(ItemRequest request) {
        Item item = new Item(null, request.getName(), request.getDescription(), request.getPrice());
        return repository.save(item);
    }

    public Item update(Long id, ItemRequest request) {
        Item existing = findById(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Item not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
}
