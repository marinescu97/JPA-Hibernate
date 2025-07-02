package com.spring.JPAHibernate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.JPAHibernate.dto.ProductDto;
import com.spring.JPAHibernate.entity.Product;
import com.spring.JPAHibernate.repository.ProductJpaRepository;
import com.spring.JPAHibernate.service.ProductQueryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductQueryControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductJpaRepository repository;

    @Autowired
    private ProductQueryService service;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        List<ProductDto> dtoList = List.of(
                new ProductDto("Product 1", 12.5),
                new ProductDto("Product 2", 34.7),
                new ProductDto("Product 3", 11.6)
        );

        List<Product> testProducts = dtoList.stream()
                .map(dto -> mapper.convertValue(dto, Product.class))
                .toList();

        repository.saveAll(testProducts);
    }

    @Test
    void getProductsWithPriceGreaterThan_shouldReturnProducts() throws Exception {
        double minPrice = 12.0;
        mockMvc.perform(get("/products/query/price-greater-than")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("price", String.valueOf(minPrice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Product> foundProducts = service.findByPriceGreaterThan(minPrice);

        assertEquals(2, foundProducts.size());
        assertEquals("Product 1", foundProducts.getFirst().getName());
        assertEquals("Product 2", foundProducts.getLast().getName());
    }

    @Test
    void getProductsWithPriceGreaterThan_shouldReturnNoContent() throws Exception {
        double minPrice = 50.0;

        mockMvc.perform(get("/products/query/price-greater-than")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("price", String.valueOf(minPrice)))
                .andExpect(status().isNoContent());

        List<Product> foundProducts = service.findByPriceGreaterThan(minPrice);

        assertTrue(foundProducts.isEmpty());
    }

    @Test
    void getProductsWithPriceGreaterThan_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/products/query/price-greater-than")
                        .param("price", "0.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsWithPriceBetween_shouldReturnProducts() throws Exception{
        double minPrice = 10.0;
        double maxPrice = 15.0;

        mockMvc.perform(get("/products/query/price-between")
                .param("minPrice", String.valueOf(minPrice))
                .param("maxPrice", String.valueOf(maxPrice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<Product> foundProducts = service.findByPriceBetween(minPrice, maxPrice);

        assertEquals(2, foundProducts.size());
        assertEquals("Product 1", foundProducts.getFirst().getName());
        assertEquals("Product 3", foundProducts.getLast().getName());
    }

    @Test
    void getProductsWithPriceBetween_shouldReturnNoContent() throws Exception{
        double minPrice = 100.0;
        double maxPrice = 150.0;

        mockMvc.perform(get("/products/query/price-between")
                        .param("minPrice", String.valueOf(minPrice))
                        .param("maxPrice", String.valueOf(maxPrice)))
                .andExpect(status().isNoContent());

        List<Product> foundProducts = service.findByPriceBetween(minPrice, maxPrice);

        assertTrue(foundProducts.isEmpty());
    }

    @Test
    void getProductsWithPriceBetween_shouldReturnBadRequest() throws Exception{
        mockMvc.perform(get("/products/query/price-between")
                        .param("minPrice", "0.0")
                        .param("maxPrice", "0.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsSorted_sortByPriceAsc_shouldReturnSortedProducts() throws Exception{
        String sortBy = "price";

        mockMvc.perform(get("/products/query/sorted")
                .param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        List<Product> sortedProducts = service.findAllOrderBy(sortBy, true);

        assertEquals(3, sortedProducts.size());
        assertEquals("Product 1", sortedProducts.get(1).getName());
        assertEquals("Product 2", sortedProducts.get(2).getName());
        assertEquals("Product 3", sortedProducts.get(0).getName());
    }

    @Test
    void getProductsSorted_sortByPriceDesc_shouldReturnSortedProducts() throws Exception{
        String sortBy = "price";

        mockMvc.perform(get("/products/query/sorted")
                        .param("sortBy", sortBy)
                        .param("ascending", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        List<Product> sortedProducts = service.findAllOrderBy(sortBy, false);

        assertEquals(3, sortedProducts.size());
        assertEquals("Product 1", sortedProducts.get(1).getName());
        assertEquals("Product 2", sortedProducts.get(0).getName());
        assertEquals("Product 3", sortedProducts.get(2).getName());
    }

    @Test
    void getProductsSorted_sortByNameAsc_shouldReturnBadRequest() throws Exception{
        String sortBy = "name";

        mockMvc.perform(get("/products/query/sorted")
                        .param("sortBy", sortBy)
                        .param("ascending", "false"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductsByNameContaining_shouldReturnProducts() throws Exception {
        String text = "2";

        mockMvc.perform(get("/products/query/name-containing")
                .param("name", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getProductsByNameContaining_shouldReturnNoContent() throws Exception {
        String text = "Test";

        mockMvc.perform(get("/products/query/name-containing")
                        .param("name", text))
                .andExpect(status().isNoContent());
    }
}
