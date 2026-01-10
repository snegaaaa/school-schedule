package ru.school.schoolschedule.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "app_user",
        indexes = {
                @Index(name = "ix_app_user_username", columnList = "username")
        }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    /**
     * Храним роль без префикса ROLE_ (то есть "ADMIN" или "VIEWER").
     * В Spring Security она станет "ROLE_ADMIN"/"ROLE_VIEWER" при сборке GrantedAuthority.
     */
    @Column(nullable = false, length = 16)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;

    public AppUser() {}

    public AppUser(String username, String passwordHash, String role, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}