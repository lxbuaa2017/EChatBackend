package com.example.echatbackend.dao.reference;

import com.example.echatbackend.entity.reference.County;
import com.example.echatbackend.entity.reference.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {
    List<Student> findByCounty(County county);

    Student findByIdNumber(String idNumber);
}
