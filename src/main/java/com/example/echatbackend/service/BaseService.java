package com.example.echatbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID, R extends JpaRepository<T, ID>> {

    @Autowired
    protected R baseRepository;

    public <S extends T> S save(S var1) {
        return baseRepository.save(var1);
    }

    public <S extends T> List<S> saveAll(List<S> var1) {
        return baseRepository.saveAll(var1);
    }

    public Optional<T> findById(ID var1) {
        return baseRepository.findById(var1);
    }

    public boolean existsById(ID var1) {
        return baseRepository.existsById(var1);
    }

    public List<T> findAll() {
        return baseRepository.findAll();
    }

    public List<T> findAllById(Iterable<ID> var1) {
        return baseRepository.findAllById(var1);
    }

    public long count() {
        return baseRepository.count();
    }

    public void deleteById(ID var1) {
        baseRepository.deleteById(var1);
    }

    public void delete(T var1) {
        baseRepository.delete(var1);
    }

    public void deleteAll(Iterable<? extends T> var1) {
        baseRepository.deleteAll(var1);
    }

    public void deleteAll() {
        baseRepository.deleteAll();
    }

    public List<T> findAll(Sort var1) {
        return baseRepository.findAll(var1);
    }

    public Page<T> findAll(Pageable var1) {
        return baseRepository.findAll(var1);
    }

    public void flush() {
        baseRepository.flush();
    }

    public <S extends T> S saveAndFlush(S var1) {
        return baseRepository.saveAndFlush(var1);
    }

    public void deleteInBatch(Iterable<T> var1) {
        baseRepository.deleteInBatch(var1);
    }

    public void deleteAllInBatch() {
        baseRepository.deleteAllInBatch();
    }

    public T getOne(ID var1) {
        return baseRepository.getOne(var1);
    }

}
