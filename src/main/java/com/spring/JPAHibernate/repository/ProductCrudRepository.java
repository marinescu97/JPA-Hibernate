package com.spring.JPAHibernate.repository;

import com.spring.JPAHibernate.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCrudRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByName(String name);
    List<Product> findByNameIn(List<String> names);
}
