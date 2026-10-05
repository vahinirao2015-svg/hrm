package com.salohi.hrms;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/** Admin-only user management. Passwords are never returned. */
@RestController @RequestMapping("/api/users")
public class UserController {
    record UserView(Long id, String username, Role role, boolean enabled) {}
    record UserForm(@NotBlank String username, String password, Role role, Boolean enabled) {}

    private final UserRepository repo; private final PasswordEncoder enc;
    public UserController(UserRepository repo, PasswordEncoder enc) { this.repo = repo; this.enc = enc; }

    private static UserView view(AppUser u) { return new UserView(u.getId(), u.getUsername(), u.getRole(), u.isEnabled()); }

    @GetMapping public List<UserView> all() { return repo.findAll().stream().map(UserController::view).toList(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserForm f) {
        if (f.password() == null || f.password().length() < 8)
            return ResponseEntity.badRequest().body("Password must be at least 8 characters");
        if (repo.findByUsername(f.username()).isPresent())
            return ResponseEntity.status(409).body("Username already exists");
        AppUser u = new AppUser(f.username(), enc.encode(f.password()), f.role() == null ? Role.USER : f.role());
        return ResponseEntity.ok(view(repo.save(u)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserForm f) {
        return repo.findById(id).<ResponseEntity<?>>map(u -> {
            if (f.role() != null) u.setRole(f.role());
            if (f.enabled() != null) u.setEnabled(f.enabled());
            if (f.password() != null && !f.password().isBlank()) u.setPassword(enc.encode(f.password()));
            return ResponseEntity.ok(view(repo.save(u)));
        }).orElse(ResponseEntity.notFound().build());
    }

     @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AppUser u = repo.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();
        if ("admin".equals(u.getUsername())) return ResponseEntity.status(403).build();
        repo.delete(u);
        return ResponseEntity.noContent().build();
    }

 }
