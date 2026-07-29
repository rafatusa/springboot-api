package com.example.springbootapi.repository;

import com.example.springbootapi.model.Item;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for Item resources.
 * Uses ConcurrentHashMap for thread-safe access.
 */
@Repository
public class ItemRepository {

    private final Map<Long, Item> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1L);

    public ItemRepository() {
        // Seed with sample data
        save(new Item(null, "Widget Pro", "Premium enterprise widget", 29.99));
        save(new Item(null, "Gadget Core", "Core functionality gadget", 49.99));
        save(new Item(null, "Toolkit Suite", "Complete developer toolkit", 99.99));
    }

    public List<Item> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idSequence.getAndIncrement());
        }
        store.put(item.getId(), item);
        return item;
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public long count() {
        return store.size();
    }
}
