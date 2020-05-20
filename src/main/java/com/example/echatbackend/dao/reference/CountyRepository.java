package com.example.echatbackend.dao.reference;

import com.example.echatbackend.entity.reference.County;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CountyRepository extends JpaRepository<County, Integer> {
    @NotNull List<County> findAllByOrderByCounty();

    County findByCounty(String county);

    @NotNull List<County> findByCountyIn(Collection<String> county);
}
