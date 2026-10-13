package com.studx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studx.dto.LoginRequest;
import com.studx.dto.ProductCreateRequest;
import com.studx.dto.SignupRequest;
import com.studx.entity.Category;
import com.studx.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setupCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("Books", "books", "/CategoryImages/Books.png"));
            categoryRepository.save(new Category("Laptops", "laptops", "/CategoryImages/Laptop.png"));
        }
    }

    @Test
    void testGetCategories_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void testAuthSignupAndLoginFlow() throws Exception {
        SignupRequest signup = new SignupRequest("Integration User", "integ@campus.edu", "Password@123", "MIT", "Boston");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        LoginRequest login = new LoginRequest("integ@campus.edu", "Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.jwtToken", notNullValue()))
                .andExpect(jsonPath("$.name", is("Integration User")));
    }

    @Test
    void testCreateProduct_shouldReturnCreatedProduct() throws Exception {
        Category cat = categoryRepository.findAll().get(0);

        ProductCreateRequest createRequest = new ProductCreateRequest();
        createRequest.setTitle("Engineering Physics Vol 2");
        createRequest.setDescription("Clean textbook in great shape");
        createRequest.setPrice(BigDecimal.valueOf(450));
        createRequest.setImage("https://image.url/physics.jpg");
        createRequest.setCategoryId(cat.getId());
        createRequest.setDetails("No highlights, 2024 edition");
        createRequest.setSeller(new com.studx.dto.SellerDto(null, "Test Seller", "seller@mit.edu", "Boston", "MIT", null));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Engineering Physics Vol 2")))
                .andExpect(jsonPath("$.price", notNullValue()))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }
}
