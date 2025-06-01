package com.spring.JPAHibernate.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.JPAHibernate.dto.PartialProductDto;
import com.spring.JPAHibernate.dto.ProductDto;
import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.exception.ProductUpdateException;
import com.spring.JPAHibernate.exception.UniqueFieldException;
import com.spring.JPAHibernate.repository.ProductCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductCrudRepository repository;
    private final ObjectMapper mapper;

    @Autowired
    public ProductService(ProductCrudRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Product save(ProductDto productDto){
        checkUniqueName(productDto.getName());

        Product product = mapper.convertValue(productDto, Product.class);
        return repository.save(product);
    }

    private void checkUniqueName(String name){
        repository.findByName(name)
                .ifPresent(n -> {
                    throw new UniqueFieldException("The product name '" + name + "' already exists.");
                });
    }

    public Iterable<Product> saveAll(List<ProductDto> productDtoList) {
        validateNoDuplicatesInInput(productDtoList);
        validateNoExistingNames(productDtoList);

        return productDtoList.stream()
                .map(dto -> mapper.convertValue(dto, Product.class))
                .collect(Collectors.collectingAndThen(Collectors.toList(), repository::saveAll));
    }

    private void validateNoDuplicatesInInput(List<ProductDto> productDtoList) {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = productDtoList.stream()
                .map(ProductDto::getName)
                .filter(name -> !seen.add(name))
                .toList();

        if (!duplicates.isEmpty()) {
            throw new UniqueFieldException("Duplicate product names in input: " + duplicates);
        }
    }

    private void validateNoExistingNames(List<ProductDto> productDtoList) {
        List<String> names = productDtoList.stream()
                .map(ProductDto::getName)
                .toList();

        List<String> existingNames = repository.findByNameIn(names).stream()
                .map(Product::getName)
                .toList();

        if (!existingNames.isEmpty()) {
            throw new UniqueFieldException("Product names already exist in DB: " + existingNames);
        }
    }

    public Product findById(Long id){
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("The product with id " + id + " was not found."));
    }

    public Iterable<Product> findAll(){
        return repository.findAll();
    }

    public <T> Product updateProduct(Long id, T newProduct) {
        Product product = findById(id);

        try {
            if (newProduct instanceof ProductDto){
                checkUniqueName(((ProductDto) newProduct).getName());
                mapper.updateValue(product, newProduct);
            } else if (newProduct instanceof PartialProductDto) {
                checkUniqueName(((PartialProductDto) newProduct).getName());
                ObjectMapper patchMapper = mapper.copy();

                patchMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                patchMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                String json = patchMapper.writeValueAsString(newProduct);
                patchMapper.readerForUpdating(product).readValue(json);
            }

            return repository.save(product);
        } catch (JsonProcessingException e){
            throw new ProductUpdateException("Error on updating product: ", e);
        }
    }

    public void deleteById(Long id){
        if (repository.existsById(id)){
            repository.deleteById(id);
        } else {
            throw new NoSuchElementException("Product not found with id: " + id);
        }
    }
}
