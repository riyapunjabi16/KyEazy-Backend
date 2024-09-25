package com.product.product.kyeazy.controllers;

import com.product.product.kyeazy.dto.ActionDTO;
import com.product.product.kyeazy.dto.OrderDTO;
import com.product.product.kyeazy.entities.Company;
import com.product.product.kyeazy.entities.CompanyOrder;
import com.product.product.kyeazy.repositories.CompanyRepository;
import com.product.product.kyeazy.repositories.OrderRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("/create-order/{amount}")
    public OrderDTO generateOrder(@PathVariable int amount) throws Exception {
        RazorpayClient razorpay = new RazorpayClient("rzp_test_51mlvZBHt5Cbjq", "NyOFOInK35B9BvW9RSQrhjun");
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount); // amount in the smallest currency unit
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcptid_1");
        Order order = razorpay.Orders.create(orderRequest);
        JSONObject jsonObject = new JSONObject(String.valueOf(order));
        String id = jsonObject.getString("id");

        return new OrderDTO(id);
    }
    @GetMapping("/payment-history/{companyId}")
    public List<CompanyOrder> getPaymentHistory(@PathVariable Integer companyId)
    {
        Pageable pageable=PageRequest.of(0, 5, Sort.by("orderUniqueId").descending());
        return orderRepository.findByCompanyOrderId(companyId,pageable);
    }

    @GetMapping("/payment-success/{companyId}/{coins}/{orderId}/{paymentId}/{amount}")
    public ActionDTO paymentSuccess(@PathVariable Integer companyId,@PathVariable Integer coins,@PathVariable String orderId,@PathVariable String paymentId,@PathVariable Integer amount) throws Exception {
        Company company=companyRepository.findById(companyId).get();
        company.setCoins(company.getCoins()+coins);
        Company savedCompany = companyRepository.save(company);
        CompanyOrder companyOrder=new CompanyOrder();
        companyOrder.setCompanyOrderId(companyId);
        companyOrder.setOrderId(orderId);
        companyOrder.setAmount(amount);
        companyOrder.setPaymentId(paymentId);
        CompanyOrder savedOrder=orderRepository.save(companyOrder);
        return new ActionDTO(savedCompany.getCoins(),true,"Order Added Successfully");
    }
}