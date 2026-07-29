package com.example.springbootapi.service;

import com.example.springbootapi.dto.ItemRequest;
import com.example.springbootapi.model.Item;
import com.example.springbootapi.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemService service;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item(1L, "Test Widget", "A test item", 19.99);
    }

    @Test
    void findAll_returnsList() {
        when(repository.findAll()).thenReturn(Arrays.asList(sampleItem));
        List<Item> result = service.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Widget");
    }

    @Test
    void findById_returnsItem_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        Item result = service.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Widget");
    }

    @Test
    void findById_throwsException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsItem() {
        ItemRequest request = new ItemRequest("New Item", "Description", 9.99);
        Item saved = new Item(2L, "New Item", "Description", 9.99);
        when(repository.save(any(Item.class))).thenReturn(saved);

        Item result = service.create(request);
        assertThat(result.getName()).isEqualTo("New Item");
        assertThat(result.getPrice()).isEqualTo(9.99);
        verify(repository).save(any(Item.class));
    }

    @Test
    void update_modifiesAndReturnsItem() {
        ItemRequest request = new ItemRequest("Updated", "Updated desc", 29.99);
        when(repository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(repository.save(any(Item.class))).thenReturn(sampleItem);

        Item result = service.update(1L, request);
        assertThat(result).isNotNull();
        verify(repository).save(any(Item.class));
    }

    @Test
    void delete_removesItem_whenExists() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throwsException_whenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
