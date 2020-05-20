package com.example.studentinfo.service.reference;

import com.alibaba.fastjson.JSONObject;
import com.example.studentinfo.dao.reference.CountyRepository;
import com.example.studentinfo.dao.reference.StudentRepository;
import com.example.studentinfo.entity.reference.County;
import com.example.studentinfo.entity.reference.Student;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SearchService {
    private final StudentRepository studentRepository;
    private final CountyRepository countyRepository;
    @Getter
    protected long lastSize;

    @Autowired
    public SearchService(StudentRepository studentRepository, CountyRepository countyRepository) {
        this.studentRepository = studentRepository;
        this.countyRepository = countyRepository;
    }

    public JSONObject search(@NotNull JSONObject json) {
        int page = json.getInteger("page") - 1;
        int size = json.getInteger("size");
        String keyword = json.getString("keyword");
        String[] countyStrings = json.getObject("countyList", String[].class);
        String[] genders = json.getObject("genderList", String[].class);
        List<County> countyList = null;
        ArrayList<String> genderList = null;
        if (countyStrings != null) {
            ArrayList<String> countyStringList = new ArrayList<>(Arrays.asList(countyStrings));
            countyList = countyRepository.findByCountyIn(countyStringList);
        }
        if (genders != null) {
            genderList = new ArrayList<>(Arrays.asList(genders));
        }
        List<County> finalCountyList = countyList;
        ArrayList<String> finalGenderList = genderList;
        Specification<Student> studentSpecification = (Specification<Student>) (root, criteriaQuery, cb) -> {
            Predicate predicate = cb.conjunction();
            if (keyword != null) {
                predicate = cb.or(cb.like(root.get("name"), "%" + keyword + "%"), cb.like(root.get("idNumber"), "%" + keyword + "%"));
            }
            if (finalCountyList != null) {
                CriteriaBuilder.In<County> inCounty = cb.in(root.get("county"));
                for (County county : finalCountyList) {
                    inCounty.value(county);
                }
                predicate = cb.and(predicate, cb.and(inCounty));
            }
            if (finalGenderList != null) {
                CriteriaBuilder.In<String> inGender = cb.in(root.get("gender"));
                for (String string : finalGenderList) {
                    inGender.value(string);
                }
                predicate = cb.and(predicate, cb.and(inGender));
            }
            return predicate;
        };
        Sort sort = Sort.by(Sort.Order.asc("name"));
        Page<Student> students = studentRepository.findAll(studentSpecification, PageRequest.of(page, size, sort));
        lastSize = students.getTotalElements();
        JSONObject response = new JSONObject();
        response.put("data", students.getContent());
        response.put("length", students);
        return response;
    }
}
