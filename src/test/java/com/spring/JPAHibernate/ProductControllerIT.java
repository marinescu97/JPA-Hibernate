package com.spring.JPAHibernate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.JPAHibernate.dto.PartialProductDto;
import com.spring.JPAHibernate.dto.ProductDto;
import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.repository.ProductCrudRepository;
import com.spring.JPAHibernate.service.ProductService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductCrudRepository repository;

    @Autowired
    private ProductService service;

    @Autowired
    private ObjectMapper mapper;

    private Product testProduct;
    private static List<ProductDto> dtoList;

    @BeforeAll
    static void beforeAll() {
        dtoList = new ArrayList<>(List.of(
                new ProductDto("Product 1", 12.5),
                new ProductDto("Product 2", 50.3),
                new ProductDto("Product 3", 10.4)
        ));
    }

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setName("Test product");
        testProduct.setPrice(12.5);

        testProduct = repository.save(testProduct);
    }

    @Test
    void createProduct_validData_shouldCreateProduct() throws Exception {
        ProductDto dto = new ProductDto("Product", 12.5);

        mockMvc.perform(post("/products")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.price").value(dto.getPrice()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        List<Product> products =(List<Product>) repository.findAll();

        assertEquals(2, products.size());
        assertEquals(dto.getName(), products.getLast().getName());
    }

    @Test
    void createProduct_invalidData_shouldThrowException() throws Exception {
        ProductDto dto = new ProductDto(testProduct.getName(), 12.5);

        mockMvc.perform(post("/products")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());

        List<Product> products = (List<Product>) repository.findAll();

        assertEquals(1, products.size());
    }

    @Test
    void createAllProducts_validData_shouldCreateAllProducts() throws Exception{
        List<ProductDto> dtoList = new ArrayList<>(List.of(
                new ProductDto("Product 1", 12.5),
                new ProductDto("Product 2", 50.3),
                new ProductDto("Product 3", 10.4)
        ));

        mockMvc.perform(post("/products/all")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dtoList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"))
                .andExpect(jsonPath("$[2].name").value("Product 3"));

        List<Product> products =(List<Product>) repository.findAll();

        assertEquals(4, products.size());
    }

    @Test
    void createAllProducts_invalidData_shouldThrowException() throws Exception{
        List<ProductDto> dtoList = new ArrayList<>(List.of(
                new ProductDto(testProduct.getName(), 12.5),
                new ProductDto("Product 2", null),
                new ProductDto("Product 3", 10.4)
        ));

        mockMvc.perform(post("/products/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dtoList)))
                .andExpect(status().isBadRequest());

        List<Product> products =(List<Product>) repository.findAll();

        assertEquals(1, products.size());
    }

    @Test
    void getAllProducts_shouldGet3Products() throws Exception{
        service.saveAll(dtoList);

        mockMvc.perform(get("/products"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(4)
                );
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/products/" + testProduct.getId()))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(testProduct.getId()),
                        jsonPath("$.name").value(testProduct.getName())
                );
    }

    @Test
    void getProductById_shouldReturnNotFound() throws Exception{
        mockMvc.perform(get("/products/" + 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_validData_shouldUpdateProduct() throws Exception{
        ProductDto dto = dtoList.getFirst();
        dto.setPrice(120.0);

        mockMvc.perform(put("/products/" + testProduct.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(testProduct.getId()),
                        jsonPath("$.name").value(dto.getName()),
                        jsonPath("$.price").value(dto.getPrice())
                );
        
        testProduct = service.findById(testProduct.getId());
        
        assertEquals(dto.getName(), testProduct.getName());
        assertEquals(dto.getPrice(), testProduct.getPrice());
        assertNotEquals(testProduct.getCreatedAt(), testProduct.getUpdatedAt());
    }

    @Test
    void updateProduct_invalidData_shouldThrowException() {

    }

    @Test
    void updateProduct_shouldReturnNotFound() throws Exception{
        ProductDto dto = dtoList.getFirst();
        dto.setPrice(120.0);

        mockMvc.perform(put("/products/" + 99)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.error").exists()
                );
    }

    @Test
    void updatePartialProduct_validData_shouldUpdateOnlySpecifiedFields() throws Exception{
        PartialProductDto dto = new PartialProductDto();
        dto.setName("New product");

        mockMvc.perform(patch("/products/" + testProduct.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(testProduct.getId()),
                        jsonPath("$.name").value(dto.getName()),
                        jsonPath("$.price").value(testProduct.getPrice())
                );

        testProduct = service.findById(testProduct.getId());

        assertEquals(dto.getName(), testProduct.getName());
        assertNotEquals(dto.getPrice(), testProduct.getPrice());
        assertNotEquals(testProduct.getCreatedAt(), testProduct.getUpdatedAt());
    }

    @Test
    void updatePartialProduct_shouldReturnNotFound() throws Exception{
        ProductDto dto = dtoList.getFirst();
        dto.setPrice(120.0);

        mockMvc.perform(patch("/products/" + 99)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.error").exists()
                );
    }

    @Test
    void deleteById_shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/products/" + testProduct.getId()))
                .andExpect(status().isNoContent());

        assertThrows(NoSuchElementException.class,() -> service.findById(testProduct.getId()));
    }

    @Test
    void deleteById_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/products/" + 99))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
