package com.studx.service;

import com.studx.dto.FairPriceResponse;
import com.studx.entity.Category;
import com.studx.entity.ProductCondition;
import com.studx.repository.CategoryRepository;
import com.studx.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FairPriceServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private FairPriceService fairPriceService;

    @BeforeEach
    void setUp() {
        fairPriceService = new FairPriceService(productRepository, categoryRepository);
    }

    @Test
    void testGetFairPriceInsights_withSufficientData() {
        Category category = new Category("Laptops", "laptops", "img.png");
        category.setId(2L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        // Row: avg = 25000.0, min = 20000.0, max = 30000.0, count = 5
        Object[] row = new Object[]{25000.0, 20000.0, 30000.0, 5L};
        List<Object[]> statsList = new ArrayList<>();
        statsList.add(row);

        when(productRepository.findPriceStatsByCategoryAndCondition(2L, ProductCondition.GOOD))
                .thenReturn(statsList);

        FairPriceResponse response = fairPriceService.getFairPriceInsights(2L, "GOOD");

        assertNotNull(response);
        assertEquals(2L, response.getCategoryId());
        assertEquals("Laptops", response.getCategoryName());
        assertEquals("GOOD", response.getConditionGrade());
        assertEquals(5, response.getSampleSize());
        assertEquals(BigDecimal.valueOf(25000).setScale(2), response.getAveragePrice());
        assertEquals(BigDecimal.valueOf(21250.00).setScale(2), response.getSuggestedFairPrice());
        assertTrue(response.getInsightMessage().contains("Fair price range for Laptops"));
    }

    @Test
    void testGetFairPriceInsights_insufficientData() {
        Category category = new Category("Fitness", "fitness", "img.png");
        category.setId(9L);

        when(categoryRepository.findById(9L)).thenReturn(Optional.of(category));

        Object[] emptyRow = new Object[]{null, null, null, 0L};
        List<Object[]> emptyList = new ArrayList<>();
        emptyList.add(emptyRow);

        when(productRepository.findPriceStatsByCategoryAndCondition(9L, null))
                .thenReturn(emptyList);

        FairPriceResponse response = fairPriceService.getFairPriceInsights(9L, null);

        assertNotNull(response);
        assertEquals(0, response.getSampleSize());
        assertTrue(response.getInsightMessage().contains("Insufficient campus sales data"));
    }
}
