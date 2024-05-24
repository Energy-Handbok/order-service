package com.khaphp.common.dto.foodrecipe;

import com.khaphp.common.dto.usersystem.UserSystemDTOviewInOrtherEntity;
import com.khaphp.common.entity.FoodTutorial;
import com.khaphp.common.entity.RecipeIngredients;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CookingRecipeDTOdetail {
    private String id;
    private String name;
    private String productImg;
    private String level;
    private float timeCook;
    private Date updateDate;
    private short mealServing;
    private String description;
    private String status;
    private int cmtSize;
    private int like;
    private float star;
    private int vote;

    private UserSystemDTOviewInOrtherEntity employeeV;

    private UserSystemDTOviewInOrtherEntity customerV;

    private List<RecipeIngredients> recipeIngredients;

    private List<FoodTutorial> foodTutorials;
}
