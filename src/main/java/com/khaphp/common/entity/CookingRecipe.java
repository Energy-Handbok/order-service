package com.khaphp.common.entity;

import com.khaphp.common.dto.usersystem.UserSystemDTOviewInOrtherEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CookingRecipe {
    private String id;
    private String name;
    private String productImg;
    private String level;
    private float timeCook;
    private Date updateDate;
    private short mealServing;
    private String description;
    private String status;

    private String employeeId;

    private String customerId;

    private List<RecipeIngredients> recipeIngredients;

    private List<FoodTutorial> foodTutorials;

    public static CookingRecipe mapFromLinkedHashMap(LinkedHashMap<String, Object> linkedHashMap) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        CookingRecipe cookingRecipe = new CookingRecipe();
        cookingRecipe.setId(linkedHashMap.get("id").toString());
        cookingRecipe.setName(linkedHashMap.get("name").toString());
        cookingRecipe.setProductImg(linkedHashMap.get("productImg").toString());
        cookingRecipe.setLevel(linkedHashMap.get("level").toString());
        cookingRecipe.setTimeCook(Float.parseFloat(linkedHashMap.get("timeCook").toString()));
        cookingRecipe.setUpdateDate(simpleDateFormat.parse((String) linkedHashMap.get("updateDate")));
        cookingRecipe.setMealServing(Short.parseShort(linkedHashMap.get("mealServing").toString()));
        cookingRecipe.setDescription(linkedHashMap.get("description").toString());
        cookingRecipe.setStatus(linkedHashMap.get("status").toString());
        cookingRecipe.setEmployeeId(mapFromLinkedhashMap((LinkedHashMap<String, Object>) linkedHashMap.get("employeeV")).getId());
        cookingRecipe.setCustomerId(mapFromLinkedhashMap((LinkedHashMap<String, Object>) linkedHashMap.get("customerV")).getId());
        return cookingRecipe;
    }

    private static UserSystemDTOviewInOrtherEntity mapFromLinkedhashMap(LinkedHashMap<String, Object> linkedHashMap) {
        UserSystemDTOviewInOrtherEntity object = new UserSystemDTOviewInOrtherEntity();
        object.setId(linkedHashMap.get("id").toString());
        object.setName(linkedHashMap.get("name").toString());
        object.setImgUrl(linkedHashMap.get("imgUrl").toString());
        return object;
    }
}
