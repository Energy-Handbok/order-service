package com.khaphp.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FoodEncylopedia {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "VARCHAR(36)")
    private String id;
    private String name;
    private String unit;
    private float calo;
    private Date updateDate;
    private String img;
    @Column(columnDefinition = "VARCHAR(36)")
    private String employeeId;
    @OneToMany(mappedBy = "foodEncylopedia")
    @JsonIgnore
    private List<Food> foods;

}
