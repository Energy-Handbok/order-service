package com.khaphp.common.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeIngredients {
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    private String name;
    private String amount;
    private String img;
    private String note;

    private CookingRecipe cookingRecipe;
}
