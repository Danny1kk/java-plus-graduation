package ru.practicum.category.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}