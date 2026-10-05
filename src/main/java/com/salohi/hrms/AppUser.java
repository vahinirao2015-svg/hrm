package com.salohi.hrms;

import jakarta.persistence.*;

@Entity @Table(name = "app_users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    private boolean enabled = true;

    public AppUser() {}
    public AppUser(String username, String password, Role role) {
        this.username = username; this.password = password; this.role = role;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
    public Role getRole() { return role; }
    public void setRole(Role r) { this.role = r; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
}
