package org.example.fashion_web.backend.dto;

public class UserDto {

    private String name;
    private String email;
    private String role;
    private String password;

    public UserDto(String role, String password, String email, String name) {
        this.role = "USER";
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}