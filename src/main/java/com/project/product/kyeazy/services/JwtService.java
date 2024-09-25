package com.product.product.kyeazy.services;

import com.product.product.kyeazy.dto.JwtRequest;
import com.product.product.kyeazy.dto.JwtResponse;
import com.product.product.kyeazy.entities.Company;
import com.product.product.kyeazy.entities.Employee;
import com.product.product.kyeazy.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Autowired
    private CompanyService companyService;
    @Autowired
    private  EmployeeService employeeService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private CustomUserDetailsService  customUserDetailsService;
    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<JwtResponse> token( JwtRequest jwtRequest) throws Exception
    {
        int userId=0;
        String username="";
        if(jwtRequest.getRole().equals("ADMIN"))
        {
            if(!jwtRequest.getUsername().equals("Riya")) throw new Exception("Wrong Username!");
            if(! jwtRequest.getPassword().equals("Riya123")) throw new Exception("Invalid Credentials!");
            username+="A"+jwtRequest.getUsername();
        }
        if(jwtRequest.getRole().equals("COMPANY"))
        {
            Company company =companyService.getCompanyByUsername(jwtRequest.getUsername());
            if(company==null) throw new Exception("Wrong Username!");
            if(! (company.getPassword().equals(jwtRequest.getPassword()))) throw new Exception("Invalid Credentials!");
            userId=company.getCompanyId();
            username+="C"+jwtRequest.getUsername();
        }
        if(jwtRequest.getRole().equals("EMPLOYEE"))
        {
            Employee employee =employeeService.getEmployeeByUsername(jwtRequest.getUsername());
            if(employee==null) throw new Exception("Wrong Username!");
            userId=employee.getEmployeeId();
            if(! (employee.getPassword().equals(jwtRequest.getPassword()))) throw new Exception("Invalid Credentials!");
            username+="E"+jwtRequest.getUsername();
        }
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,jwtRequest.getPassword()));
        UserDetails userDetails=this.customUserDetailsService.loadUserByUsername(username);
        String token=this.jwtUtil.generateToken(userDetails);
            return  ResponseEntity.ok(new JwtResponse(token,userId));
    }

}
