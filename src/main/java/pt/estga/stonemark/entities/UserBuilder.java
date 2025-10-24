package pt.estga.stonemark.entities;

import pt.estga.stonemark.enums.Role;

public class UserBuilder {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private String password;
    private Role role;

    public UserBuilder setId(Integer id) {
        this.id = id;
        return this;
    }

    public UserBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setTelephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public UserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public UserBuilder setRole(Role role) {
        this.role = role;
        return this;
    }

    public User createUser() {
        return new User(id, firstName, lastName, email, telephone, password, role);
    }
}