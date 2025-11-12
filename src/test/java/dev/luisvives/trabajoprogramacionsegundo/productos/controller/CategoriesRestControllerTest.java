package dev.luisvives.trabajoprogramacionsegundo.productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.DELETEcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.GENERICcategoryResponseDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.PATCHcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.dto.category.POSTandPUTcategoryRequestDTO;
import dev.luisvives.trabajoprogramacionsegundo.productos.service.CategoriesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoriesRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriesService categoryService;

    @InjectMocks
    private CategoriesRestController categoryController;

    private final ObjectMapper mapper = new ObjectMapper();

    private GENERICcategoryResponseDTO cat;

    private final String VALID_UUID_STRING = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    private final UUID VALID_UUID_OBJ = UUID.fromString(VALID_UUID_STRING);


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        cat = new GENERICcategoryResponseDTO();
        cat.setId(VALID_UUID_OBJ);
        cat.setName("ANIME");
    }

    // GET /categories
    @Test
    @DisplayName("GET /categories devuelve todas las categorías")
    void getAll() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(cat));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ANIME"))
                .andExpect(jsonPath("$[0].id").value(VALID_UUID_STRING));
    }

    // 🔹 GET /categories/{id}
    @Test
    @DisplayName("GET /categories/{id} devuelve la categoría por id")
    void getById() throws Exception {
        when(categoryService.getById(VALID_UUID_OBJ)).thenReturn(cat);

        mockMvc.perform(get("/categories/" + VALID_UUID_STRING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_UUID_STRING))
                .andExpect(jsonPath("$.name").value("ANIME"));
    }

    // POST /categories
    @Test
    @DisplayName("POST /categories crea una nueva categoría")
    void save() throws Exception {
        POSTandPUTcategoryRequestDTO dto = new POSTandPUTcategoryRequestDTO();
        dto.setName("series");

        when(categoryService.save(any(POSTandPUTcategoryRequestDTO.class))).thenReturn(cat);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(VALID_UUID_STRING))
                .andExpect(jsonPath("$.name").value("ANIME"));
    }

    // PUT /categories/{id}
    @Test
    @DisplayName("PUT /categories/{id} actualiza una categoría")
    void update() throws Exception {
        POSTandPUTcategoryRequestDTO dto = new POSTandPUTcategoryRequestDTO();
        dto.setName("movies");

        when(categoryService.update(eq(VALID_UUID_OBJ), any(POSTandPUTcategoryRequestDTO.class))).thenReturn(cat);

        mockMvc.perform(put("/categories/" + VALID_UUID_STRING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_UUID_STRING))
                .andExpect(jsonPath("$.name").value("ANIME"));
    }

    // PATCH /categories/{id}
    @Test
    @DisplayName("PATCH /categories/{id} modifica parcialmente una categoría")
    void patchCategory() throws Exception {
        PATCHcategoryRequestDTO dto = new PATCHcategoryRequestDTO();
        dto.setName("games");

        when(categoryService.patch(eq(VALID_UUID_OBJ), any(PATCHcategoryRequestDTO.class))).thenReturn(cat);

        mockMvc.perform(patch("/categories/" + VALID_UUID_STRING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_UUID_STRING))
                .andExpect(jsonPath("$.name").value("ANIME"));
    }

    // DELETE /categories/{id}
    @Test
    @DisplayName("DELETE /categories/{id} elimina una categoría")
    void deleteById() throws Exception {
        DELETEcategoryResponseDTO resp = new DELETEcategoryResponseDTO(
                "Categoría eliminada correctamente",
                cat
        );

        when(categoryService.deleteById(VALID_UUID_OBJ)).thenReturn(resp);

        mockMvc.perform(delete("/categories/" + VALID_UUID_STRING))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Categoría eliminada correctamente"))
                .andExpect(jsonPath("$.deletedCategory.id").value(VALID_UUID_STRING))
                .andExpect(jsonPath("$.deletedCategory.name").value("ANIME"));
    }

    // 🔹 Validación 400
    @Test
    @DisplayName("POST /categories devuelve 400 si el nombre está vacío")
    void save_BadRequest() throws Exception {
        POSTandPUTcategoryRequestDTO invalid = new POSTandPUTcategoryRequestDTO(); // name null

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}