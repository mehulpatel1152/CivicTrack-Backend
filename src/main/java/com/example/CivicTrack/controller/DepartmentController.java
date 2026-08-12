package com.example.CivicTrack.controller;

import com.example.CivicTrack.model.Department;
import com.example.CivicTrack.Repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @PostMapping
    public Department create(@RequestBody Department dept) {
        return departmentRepository.save(dept);
    }

    @GetMapping
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }
}