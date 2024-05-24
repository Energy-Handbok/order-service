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
public class FoodTutorial {
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    private short numberOrder;
    private String description;
    private String img;

    private CookingRecipe cookingRecipe;
}
