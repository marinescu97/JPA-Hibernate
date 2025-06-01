package com.spring.JPAHibernate.repository;

import com.spring.JPAHibernate.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    List<Product> findByPriceGreaterThan(Double price);
    List<Product> findByPriceBetween(Double priceBefore, Double priceAfter);
    List<Product> findAllByOrderByCreatedAtAsc();
    List<Product> findAllByOrderByCreatedAtDesc();
    List<Product> findAllByOrderByPriceAsc();
    List<Product> findAllByOrderByPriceDesc();
    List<Product> findByNameContaining(String name);
}
