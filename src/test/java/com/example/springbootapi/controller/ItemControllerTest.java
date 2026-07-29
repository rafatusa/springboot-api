package com.example.springbootapi.controller;

import com.example.springbootapi.model.Item;
import com.example.springbootapi.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listItems_returns200WithList() throws Exception {
        when(itemService.findAll()).thenReturn(Arrays.asList(
                new Item(1L, "Widget", "desc", 9.99)
        ));

        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Widget"));
    }

    @Test
    void getItem_returns200_whenFound() throws Exception {
        when(itemService.findById(1L)).thenReturn(new Item(1L, "Widget", "desc", 9.99));

        mockMvc.perform(get("/api/v1/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getItem_returns404_whenNotFound() throws Exception {
        when(itemService.findById(99L)).thenThrow(new NoSuchElementException("Item not found with id: 99"));

        mockMvc.perform(get("/api/v1/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createItem_returns201_withValidPayload() throws Exception {
        Item created = new Item(4L, "New Item", "desc", 15.00);
        when(itemService.create(any())).thenReturn(created);

        String payload = "{\"name\":\"New Item\",\"description\":\"desc\",\"price\":15.00}";
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(4));
    }

    @Test
    void createItem_returns400_withMissingName() throws Exception {
        String payload = "{\"description\":\"desc\",\"price\":15.00}";
        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_returns200_whenValid() throws Exception {
        Item updated = new Item(1L, "Updated", "desc", 20.00);
        when(itemService.update(eq(1L), any())).thenReturn(updated);

        String payload = "{\"name\":\"Updated\",\"description\":\"desc\",\"price\":20.00}";
        mockMvc.perform(put("/api/v1/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    void deleteItem_returns200_whenExists() throws Exception {
        mockMvc.perform(delete("/api/v1/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_returns404_whenNotFound() throws Exception {
        doThrow(new NoSuchElementException("Item not found with id: 99"))
                .when(itemService).delete(99L);

        mockMvc.perform(delete("/api/v1/items/99"))
                .andExpect(status().isNotFound());
    }
}
