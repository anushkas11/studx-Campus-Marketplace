package com.studx.dto;

import java.time.LocalDateTime;

public class SellerDto {

    private Long id;
    private String name;
    private String email;
    private String city;
    private String college;
    private LocalDateTime joinedAt;

    public SellerDto() {
    }

    public SellerDto(Long id, String name, String email, String city, String college, LocalDateTime joinedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
        this.college = college;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
