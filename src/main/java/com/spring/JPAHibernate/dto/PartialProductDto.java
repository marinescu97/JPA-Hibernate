package com.spring.JPAHibernate.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.io.Serializable;

@Data
public class PartialProductDto implements Serializable {
    private String name;

    @DecimalMin(value = "0.0",
                inclusive = false,
                message = "Price must be greater than 0.0")
    private Double price;
}
