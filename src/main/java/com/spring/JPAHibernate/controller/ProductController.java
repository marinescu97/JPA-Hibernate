package com.spring.JPAHibernate.controller;

import com.spring.JPAHibernate.dto.PartialProductDto;
import com.spring.JPAHibernate.dto.ProductDto;
import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.exception.ProductUpdateException;
import com.spring.JPAHibernate.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDto productDto){
        Product product = service.save(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PostMapping("/all")
    public ResponseEntity<Iterable<Product>> createAllProducts(@RequestBody @Valid List<ProductDto> productDtoList){
        Iterable<Product> createdUsers = service.saveAll(productDtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
    }

    @GetMapping
    public ResponseEntity<Iterable<Product>> getAllProducts(){
        Iterable<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id){
        try {
            Product product = service.findById(id);
            return ResponseEntity.ok(product);
        } catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @Valid @RequestBody ProductDto productDto){
        return handleUpdate(id, productDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePartialProduct(@PathVariable Long id,
                                                  @Valid @RequestBody PartialProductDto productDto){
        return handleUpdate(id, productDto);
    }

    private <T> ResponseEntity<?> handleUpdate(@PathVariable Long id, @RequestBody @Valid T newProduct) {
        try {
            Product product = service.updateProduct(id, newProduct);
            return ResponseEntity.ok(product);
        } catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (ProductUpdateException ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable Long id){
        try {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
