package com.product.product.kyeazy.repositories;

import com.product.product.kyeazy.entities.CompanyOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface OrderRepository extends JpaRepository<CompanyOrder,String> {

    List<CompanyOrder> findByCompanyOrderId(Integer companyOrderId, Pageable pageable) ;

}
