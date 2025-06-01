package com.spring.JPAHibernate.service;

import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.repository.ProductJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {
    private final ProductJpaRepository repository;

    @Autowired
    public ProductQueryService(ProductJpaRepository repository) {
        this.repository = repository;
    }

    public List<Product> findByPriceGreaterThan(Double price){
        return repository.findByPriceGreaterThan(price);
    }

    public List<Product> findByPriceBetween(Double minPrice, Double maxPrice){
        return repository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> findAllOrderBy(String value, boolean ascending){
        return switch (value) {
            case "createdAt" -> ascending ? repository.findAllByOrderByCreatedAtAsc() : repository.findAllByOrderByCreatedAtDesc();
            case "price"     -> ascending ? repository.findAllByOrderByPriceAsc() : repository.findAllByOrderByPriceDesc();
            default          -> throw new IllegalArgumentException("Sort field must be 'createdAt' or 'price'");
        };
    }

    public List<Product> findByNameContaining(String name){
        return repository.findByNameContaining(name);
    }
}
