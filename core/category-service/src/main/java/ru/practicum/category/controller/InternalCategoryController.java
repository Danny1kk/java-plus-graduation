package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exeption.NotFoundException;

@RestController
@RequestMapping("/internal/categories")
@RequiredArgsConstructor
public class InternalCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}