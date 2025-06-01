package com.spring.JPAHibernate.controller;

import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.service.ProductQueryService;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products/query")
public class ProductQueryController {
    private final ProductQueryService service;

    @Autowired
    public ProductQueryController(ProductQueryService service) {
        this.service = service;
    }

    @GetMapping("/price-greater-than")
    public ResponseEntity<List<Product>> getProductsWithPriceGreaterThan(
            @RequestParam @DecimalMin(value = "0.0", inclusive = false) Double price){

        List<Product> result = service.findByPriceGreaterThan(price);

        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/price-between")
    public ResponseEntity<List<Product>> getProductsWithPriceBetween(
            @RequestParam(name = "minPrice") @DecimalMin(value = "0.0", inclusive = false) Double minPrice,
            @RequestParam(name = "maxPrice") @DecimalMin(value = "0.0", inclusive = false) Double maxPrice){

        List<Product> result = service.findByPriceBetween(minPrice, maxPrice);

        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<Product>> getProductsSorted(
            @RequestParam(name = "sortBy") String sortBy,
            @RequestParam(defaultValue = "true", name = "ascending") boolean ascending) {
        List<Product> products = service.findAllOrderBy(sortBy, ascending);
        return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
    }

    @GetMapping("/name-containing")
    public ResponseEntity<List<Product>> getProductsByNameContaining(@RequestParam(name = "name") String name){
        List<Product> products = service.findByNameContaining(name);
        return products.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(products);
    }
}
