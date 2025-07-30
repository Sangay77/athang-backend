package com.bfs.rma.auth.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 40, nullable = false, unique = true)
    private String name;

    @Column(length = 60, nullable = false)
    private String description;

    public Role(String rolename, String roleDescription) {

        this.name = rolename;
        this.description = roleDescription;
    }

    public Role() {
    }

    public Role(int roleId) {
        this.id = roleId;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
