package com.studx.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long id;
    private String _id;
    private String name;
    private String email;
    private String college;
    private String city;
    private boolean isVerified;
    private String role;
    private LocalDateTime createdAt;
    private String membership;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String name, String email, String college,
                               String city, boolean isVerified, String role, LocalDateTime createdAt) {
        this.id = id;
        this._id = String.valueOf(id);
        this.name = name;
        this.email = email;
        this.college = college;
        this.city = city;
        this.isVerified = isVerified;
        this.role = role;
        this.createdAt = createdAt;
        this.membership = isVerified ? "Verified Student" : "Campus Member";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }
}
