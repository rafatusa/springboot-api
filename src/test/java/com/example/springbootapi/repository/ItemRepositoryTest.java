package com.example.springbootapi.repository;

import com.example.springbootapi.model.Item;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest {

    private final ItemRepository repository = new ItemRepository();

    @Test
    void findAll_returnsSeededItems() {
        List<Item> items = repository.findAll();
        assertThat(items).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void save_assignsId_whenNull() {
        Item item = new Item(null, "Test", "desc", 5.0);
        Item saved = repository.save(item);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findById_returnsItem_whenExists() {
        Item saved = repository.save(new Item(null, "Lookup", "desc", 1.0));
        Optional<Item> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Lookup");
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        Optional<Item> found = repository.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_removesItem() {
        Item saved = repository.save(new Item(null, "ToDelete", "desc", 1.0));
        repository.deleteById(saved.getId());
        assertThat(repository.existsById(saved.getId())).isFalse();
    }
}
