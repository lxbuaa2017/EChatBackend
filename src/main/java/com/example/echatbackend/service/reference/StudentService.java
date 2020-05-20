package com.example.echatbackend.service.reference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.echatbackend.dao.reference.CountyRepository;
import com.example.echatbackend.dao.reference.StudentRepository;
import com.example.echatbackend.entity.reference.County;
import com.example.echatbackend.entity.reference.Student;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final CountyRepository countyRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, CountyRepository countyRepository) {
        this.studentRepository = studentRepository;
        this.countyRepository = countyRepository;
    }

    public Student getOne(Integer id) {
        return studentRepository.getOne(id);
    }

    @Transactional
    public void insert(@NotNull String json) {
        Student student = JSON.parseObject(json, Student.class);
        County county = countyRepository.findByCounty(student.getCounty().getCounty());
        if (county == null) {
            countyRepository.save(student.getCounty());
        } else {
            student.setCounty(county);
        }
        studentRepository.saveAndFlush(student);
    }

    @Transactional
    public void update(@NotNull Student student, @NotNull String json) {
        Student newStudent = JSON.parseObject(json, Student.class);
        newStudent.setId(student.getId());
        County county = countyRepository.findByCounty(newStudent.getCounty().getCounty());
        if (county == null) {
            countyRepository.save(newStudent.getCounty());
        } else {
            newStudent.setCounty(county);
        }
        studentRepository.saveAndFlush(newStudent);
        if (studentRepository.findByCounty(student.getCounty()).size() == 0) {
            countyRepository.delete(student.getCounty());
        }
    }

    @Transactional
    public void delete(@NotNull Student student) {
        studentRepository.delete(student);
        if (studentRepository.findByCounty(student.getCounty()).size() == 0) {
            countyRepository.delete(student.getCounty());
        }
    }

    public JSONArray findAllStudentToJsonString() {
        List<Student> studentList = studentRepository.findAll();
        return (JSONArray) JSON.toJSON(studentList);
    }

    @Transactional
    public void importStudent(@NotNull String json) {
        List<Student> studentList = JSON.parseArray(json, Student.class);
        for (Student newStudent : studentList) {
            newStudent.getCounty().setId(null);
            Student student = studentRepository.findByIdNumber(newStudent.getIdNumber());
            if (student != null) {
                studentRepository.delete(student);
                if (studentRepository.findByCounty(student.getCounty()).size() == 0) {
                    countyRepository.delete(student.getCounty());
                }
            }
            County county = countyRepository.findByCounty(newStudent.getCounty().getCounty());
            if (county == null) {
                countyRepository.save(newStudent.getCounty());
            } else {
                newStudent.setCounty(county);
            }
            studentRepository.save(newStudent);
        }
    }
}
