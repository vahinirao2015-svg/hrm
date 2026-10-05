package com.salohi.hrms;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeRepository repo;
    public EmployeeController(EmployeeRepository repo) { this.repo = repo; }

    @GetMapping public List<Employee> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping public Employee create(@Valid @RequestBody Employee e) { return repo.save(e); }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @Valid @RequestBody Employee in) {
        return repo.findById(id).map(e -> {
            e.setEmpCode(in.getEmpCode()); e.setName(in.getName()); e.setEmail(in.getEmail());
            e.setDepartment(in.getDepartment()); e.setDesignation(in.getDesignation());
            e.setSalary(in.getSalary()); e.setJoinDate(in.getJoinDate());
            return ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id); return ResponseEntity.noContent().build();
    }
}
