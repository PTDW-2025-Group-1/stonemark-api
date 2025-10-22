package pt.estga.stonemark.dtos;

import pt.estga.stonemark.enums.Role;

public class RegisterRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String password;
    private Role role;

    public RegisterRequestDto() {
    }

    public RegisterRequestDto(String firstName, String lastName, String email, String telephone, String password, Role role) {
        this.firstName = firstName;
        this.email = email;
        this.telephone = telephone;
        this.password = password;
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
