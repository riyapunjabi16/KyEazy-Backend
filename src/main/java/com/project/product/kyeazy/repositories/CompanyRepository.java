package com.product.product.kyeazy.repositories;

import com.product.product.kyeazy.entities.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    List<Company> findAllCompaniesByNameStartingWith(String name, Pageable pageable);
    List<Company> findAllCompaniesByNameStartingWith(String name);
}
