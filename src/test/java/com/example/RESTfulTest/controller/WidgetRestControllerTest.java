package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc
class WidgetRestControllerTest {

    @MockBean
    private WidgetService service;

    //Se crea una instancia
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getExitoso() throws Exception {
        //Arrange
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);
        Widget widget2 = new Widget(2l, "Widget 2 Name", "Description 2", 4);

        //Crea una lista sin constructor
        doReturn(Lists.newArrayList(widget1, widget2)).when(service).findAll();

        //Act && Assert
        mockMvc.perform(get("/rest/widgets"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widgets"))

                // Validate the returned fields
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Widget Name")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].version", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Widget 2 Name")))
                .andExpect(jsonPath("$[1].description", is("Description 2")))
                .andExpect(jsonPath("$[1].version", is(4)));
    }

    @Test
    @DisplayName("GET /rest/widget/1 - Not Found")
    void testGetWidgetByIdNotFound() throws Exception {
        doReturn(Optional.empty()).when(service).findById(1l);

        mockMvc.perform(get("/rest/widget/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /rest/widget")
    void testCreateWidget() throws Exception {
        Widget widgetToPost = new Widget( "New Widget", "This is my widget");
        Widget widgetToReturn = new Widget(1L, "New Widget", "This is my widget", 1);
        doReturn(widgetToReturn).when(service).save(any());

        mockMvc.perform(post("/rest/widget")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToPost)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.name", Matchers.is("New Widget")))
                .andExpect(jsonPath("$.description", Matchers.is("This is my widget")))
                .andExpect(jsonPath("$.version", Matchers.is(1)));
    }

    @Test
    @DisplayName("PUT /rest/widget/1 ")
    void testUpdateWidget() throws Exception{
        Widget widget1 = new Widget(1L, "New Widget", "This is my widget",1);
        Widget widgetToPut = new Widget(1L,"New Widget", "This is my widget",1);
        Widget widgetToReturn = new Widget(1L, "Edited Widget", "This is my edited widget", 2);

        when(service.findById(any())).thenReturn(Optional.of(widget1));
        when(service.save(any())).thenReturn(widgetToReturn);

        final ResultActions resultActions = mockMvc.perform(put("/rest/widget/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToPut))
                .header("If-Match", 1));

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.name", Matchers.is("Edited Widget")))
                .andExpect(jsonPath("$.description", Matchers.is("This is my edited widget")))
                .andExpect(jsonPath("$.version", Matchers.is(2)));
    }

    @Test
    @DisplayName("PUT /rest/widget/1 - Not Found")
    void testPutWidgetByIdNotFound() throws Exception {
        Widget widgetToPut = new Widget(1L,"New Widget", "This is my widget",1);
        when(service.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/rest/widget/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(widgetToPut))
                .header("If-Match", 0))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /widget success")
    void testGetWidgetSuccess() throws Exception {
        Widget widget1 = new Widget(1l, "Widget Name", "Description", 1);

        when(service.findById(1L)).thenReturn(Optional.of(widget1));


        mockMvc.perform(get("/rest/widget/1"))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.name", Matchers.is("Widget Name")))
                .andExpect(jsonPath("$.description", Matchers.is("Description")))
                .andExpect(jsonPath("$.version", Matchers.is(1)));

    }

    static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}