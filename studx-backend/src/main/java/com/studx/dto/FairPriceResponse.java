package com.studx.dto;

import java.math.BigDecimal;

public class FairPriceResponse {

    private Long categoryId;
    private String categoryName;
    private String conditionGrade;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal averagePrice;
    private BigDecimal suggestedFairPrice;
    private long sampleSize;
    private String insightMessage;

    public FairPriceResponse() {
    }

    public FairPriceResponse(Long categoryId, String categoryName, String conditionGrade,
                             BigDecimal minPrice, BigDecimal maxPrice, BigDecimal averagePrice,
                             BigDecimal suggestedFairPrice, long sampleSize, String insightMessage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.conditionGrade = conditionGrade;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.averagePrice = averagePrice;
        this.suggestedFairPrice = suggestedFairPrice;
        this.sampleSize = sampleSize;
        this.insightMessage = insightMessage;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(String conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getSuggestedFairPrice() {
        return suggestedFairPrice;
    }

    public void setSuggestedFairPrice(BigDecimal suggestedFairPrice) {
        this.suggestedFairPrice = suggestedFairPrice;
    }

    public long getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(long sampleSize) {
        this.sampleSize = sampleSize;
    }

    public String getInsightMessage() {
        return insightMessage;
    }

    public void setInsightMessage(String insightMessage) {
        this.insightMessage = insightMessage;
    }
}
