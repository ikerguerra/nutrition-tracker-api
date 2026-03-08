package com.nutritiontracker.modules.mealtemplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateRequestDto;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateResponseDto;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.mapper.MealTemplateMapper;
import com.nutritiontracker.modules.mealtemplate.service.MealTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MealTemplate Controller API Tests")
class MealTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MealTemplateService mealTemplateService;

    @MockBean
    private MealTemplateMapper mealTemplateMapper;

    @MockBean
    private com.nutritiontracker.modules.auth.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.nutritiontracker.modules.auth.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    private void setAuthenticatedUser(Long userId) {
        User user = new User();
        user.setId(userId);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /api/v1/meal-templates should return user templates")
    void shouldGetTemplates() throws Exception {
        setAuthenticatedUser(1L);
        MealTemplate template = new MealTemplate();
        MealTemplateResponseDto dto = MealTemplateResponseDto.builder().id(1L).name("Test").build();

        when(mealTemplateService.getUserTemplates(1L)).thenReturn(List.of(template));
        when(mealTemplateMapper.toDto(template)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/meal-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Test"));
    }

    @Test
    @DisplayName("POST /api/v1/meal-templates should create template and re-fetch")
    void shouldCreateTemplate() throws Exception {
        setAuthenticatedUser(1L);
        MealTemplateRequestDto request = MealTemplateRequestDto.builder().name("New").build();
        MealTemplate template = new MealTemplate();
        template.setId(10L);
        MealTemplateResponseDto dto = MealTemplateResponseDto.builder().id(10L).name("New").build();

        when(mealTemplateMapper.toEntity(any(), eq(1L))).thenReturn(template);
        when(mealTemplateService.createTemplate(any())).thenReturn(template);
        when(mealTemplateService.getTemplateById(10L)).thenReturn(template); // The Fix
        when(mealTemplateMapper.toDto(template)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/meal-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Template created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/meal-templates/{id}/apply should invoke apply logic")
    void shouldApplyTemplate() throws Exception {
        setAuthenticatedUser(1L);

        mockMvc.perform(post("/api/v1/meal-templates/1/apply")
                .param("date", "2024-03-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Template applied successfully"));
    }
}
