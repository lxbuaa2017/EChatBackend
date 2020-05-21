package com.example.echatbackend.controller.reference;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.controller.BaseController;
import com.example.echatbackend.entity.reference.Student;
import com.example.echatbackend.service.reference.StudentService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

@CrossOrigin
@RestController
public class StudentController extends BaseController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/api/student")
    public ResponseEntity<Object> getStudent(Integer id) {
        if (id == null)
            return new ResponseEntity<>("id", HttpStatus.BAD_REQUEST);
        try {
            JSONObject json = studentService.getOne(id).detail();
            return ResponseEntity.ok(json);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/editStudent")
    public ResponseEntity<Object> editStudent(Integer id, @NotNull @RequestBody String request) {
        if (id == null)
            return new ResponseEntity<>("id", HttpStatus.BAD_REQUEST);
        try {
            Student student = studentService.getOne(id);
            studentService.update(student, request);
            return requestSuccess();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/createStudent")
    public ResponseEntity<Object> createStudent(@NotNull @RequestBody String request) {
        try {
            studentService.insert(request);
        } catch (DataIntegrityViolationException e) {
            return requestFail(-1, "该身份证号已存在");
        }
        return requestSuccess();
    }

    @DeleteMapping("/api/deleteStudent")
    public ResponseEntity<Object> deleteStudent(Integer id) {
        try {
            Student student = studentService.getOne(id);
            studentService.delete(student);
            return requestSuccess();
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
