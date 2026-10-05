package com.salohi.hrms;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "employees")
public class Employee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank @Column(unique = true) private String empCode;
    @NotBlank private String name;
    @Email @NotBlank @Column(unique = true) private String email;
    private String department;
    private String designation;
    private BigDecimal salary;
    private LocalDate joinDate;

    public Long getId() { return id; }
    public String getEmpCode() { return empCode; }
    public void setEmpCode(String v) { empCode = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String v) { department = v; }
    public String getDesignation() { return designation; }
    public void setDesignation(String v) { designation = v; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal v) { salary = v; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate v) { joinDate = v; }
}
