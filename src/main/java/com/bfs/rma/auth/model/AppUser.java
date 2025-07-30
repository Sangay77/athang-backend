package com.bfs.rma.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "AppUser")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    @Column(length = 512)
    private String photos; // URL or uploaded filename

    private boolean enabled;

    @Column(nullable = true)
    private String provider; // e.g., "google", "facebook"

    @Column(name = "provider_id")
    private String providerId; // Unique user ID from provider (e.g., sub claim in ID token)

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }

    @Transient
    public String getPhotosImagePath() {
        if (photos == null || photos.isEmpty()) {
            return "/images/default-user.png";
        }
        if (photos.startsWith("http")) {
            return photos; // assume it's a full URL from provider
        }
        return "/user-photos/" + this.id + "/" + this.photos;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
