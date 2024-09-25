package com.product.product.kyeazy.services;

import com.product.product.kyeazy.dto.CompanyDTO;
import com.product.product.kyeazy.dto.EmployeeDTO;
import com.product.product.kyeazy.entities.Company;
import com.product.product.kyeazy.entities.Employee;
import com.product.product.kyeazy.repositories.CompanyRepository;
import com.product.product.kyeazy.repositories.EmployeeRepository;
import com.product.product.kyeazy.utils.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    CompanyRepository companyRepository;

    // all-companies
    @Transactional
    public List<CompanyDTO> getCompanies(Integer pageNumber, Integer pageSize) {
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<CompanyDTO> companyDTOS = new LinkedList<>();
        for (Company company : companyRepository.findAll(pageable).getContent()) {
            companyDTOS.add(Parser.parseCompany(company));
        }
        return companyDTOS;
    }

    @Transactional
    public List<CompanyDTO> getCompaniesByName(String name, Integer pageNumber, Integer pageSize) {
        pageNumber--;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<CompanyDTO> companyDTOS = new LinkedList<>();

        for (Company company : companyRepository.findAllCompaniesByNameStartingWith(name, pageable)) {
            companyDTOS.add(Parser.parseCompany(company));
        }

        return companyDTOS;
    }


    // all-employees

    @Transactional
    public List<EmployeeDTO> viewAllApplications(
            Integer pageNumber,
            Integer pageSize,
            String sort,
            String filter
    ) {
        List<EmployeeDTO> employeeDTOS = new LinkedList<>();
        List<Employee> employees;
        Pageable pageable;
        pageNumber--;
        if(sort.equals("dateTimeOfApplication"))  {
            pageable = PageRequest.of(pageNumber, pageSize,Sort.by("dateTimeOfApplication").descending());
        } else {
            pageable = PageRequest.of(pageNumber, pageSize,Sort.by("firstName"));
        }
        if(!filter.equals("all")) {
            employees = employeeRepository.findAllByStatus(filter,pageable);
        } else {
            employees = employeeRepository.findAll(pageable).getContent();
        }
        for (Employee employee : employees) {
            employeeDTOS.add(Parser.parseEmployee(employee));
        }
        return employeeDTOS;
    }
    @Transactional
    public List<CompanyDTO> getTopPerformer(){
        List<Company> companies=companyRepository.findAll();
        List<CompanyDTO> topPerformer= new LinkedList<>();
        List<CompanyDTO> companyDTOS= new LinkedList<>();
        for(Company company:companies){
            companyDTOS.add(Parser.parseCompany(company));
        }
        List<CompanyDTO> result  = companyDTOS.stream().sorted(Comparator.comparing(CompanyDTO::getNumberOfTotalEmployees)).collect(Collectors.toList());
        int c=0;
        for(int i=result.size()-1;i>=0 && c<3;i--){
            topPerformer.add(result.get(i));
            c++;
        }
        return topPerformer;
    }

    @Transactional
    public List<EmployeeDTO> viewAllApplicantsByName(
            String name,
            Integer pageNumber,
            Integer pageSize,
            String sort,
            String filter
            ) {
        List<EmployeeDTO> employeeDTOS = new LinkedList<>();
        List<Employee> employees;
        Pageable pageable;
        pageNumber--;

        if(sort.equals("dateTimeOfApplication"))  {
            pageable = PageRequest.of(pageNumber, pageSize,Sort.by("dateTimeOfApplication").descending());
        } else {
            pageable = PageRequest.of(pageNumber, pageSize,Sort.by("displayName"));
        }

        if(!filter.equals("all")) {
            employees = employeeRepository.findAllByDisplayNameStartingWithAndStatus(name,filter,pageable);
        } else {
            employees = employeeRepository.findAllByDisplayNameStartingWith(name,pageable);
        }

        for (Employee employee : employees) {
            employeeDTOS.add(Parser.parseEmployee(employee));
        }

        return employeeDTOS;
    }

    // employee

    @Transactional
    public EmployeeDTO accept(Integer id) {
        Employee employee = getEmployeeById(id);
        employee.setStatus("Accepted");
        employee.setDateTimeOfVerification(new Date());
        employee.setAddress(employee.getAddress());
        employee.setReview("");
        //employee.setLock(true);
//        Company company =companyRepository.findById(employee.getCompanyId()).get();
//        company.setCoins(company.getCoins()-50);
//        companyRepository.save(company);
        Employee savedEmployee = employeeRepository.save(employee);
        return Parser.parseEmployee(savedEmployee);
    }

    @Transactional
    public EmployeeDTO reject(Integer id,String message) {
        Employee employee = getEmployeeById(id);
        employee.setStatus("Rejected");
        employee.setDateTimeOfVerification(new Date());
        //employee.setLock(true);
        employee.setReview(message);
//        Company company =companyRepository.findById(employee.getCompanyId()).get();
//        company.setCoins(company.getCoins()-50);
//        companyRepository.save(company);
        Employee savedEmployee = employeeRepository.save(employee);
        return Parser.parseEmployee(savedEmployee);
    }

    @Transactional
    private Employee getEmployeeById(Integer employeeId) {
        return employeeRepository.findById(employeeId).get();
    }

    @Transactional
    public EmployeeDTO viewEmployeeApplication(Integer employeeId) {
        EmployeeDTO employeeDTO;
        Employee employee = getEmployeeById(employeeId);
        return Parser.parseEmployee(employee);
    }

    @Transactional
    public ResponseEntity<byte[]> getVideo(String username) throws IOException {
        System.out.println(username);
        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/employee_videos/" + username + ".mp4"));
        System.out.println(bytes.length);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "video/mp4");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<byte[]> getDocument(String username) throws IOException {
        System.out.println(username);
        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/employee_documents/" + username + ".pdf"));
        System.out.println(bytes.length);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/pdf");
        headers.setContentLength(bytes.length);
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
    }

    // company count

    @Transactional
    public long getSearchedCompaniesSize(String name) {
        return companyRepository.findAllCompaniesByNameStartingWith(name).size();
    }

    @Transactional
    public long getTotalNumberOfCompanies() { return companyRepository.count(); }

    // employee count

    @Transactional
    public long getTotalNumberOfEmployees() {
        return employeeRepository.count();
    }

    @Transactional
    public int getTotalNumberOfAcceptedEmployees() {
        return employeeRepository.findAllByStatus("Accepted").size();
    }

    @Transactional
    public int getTotalNumberOfPendingEmployees() {
        return employeeRepository.findAllByStatus("Pending").size();
    }

    @Transactional
    public int getTotalNumberOfRegisteredEmployees() {
        return employeeRepository.findAllByStatus("Registered").size();
    }

    @Transactional
    public int getTotalNumberOfRejectEmployees() {
        return employeeRepository.findAllByStatus("Rejected").size();
    }

    @Transactional
    public long getEmployeeSize(String filter) {
        if(!filter.equals("all")) return employeeRepository.findAllByStatus(filter).size();
        return employeeRepository.findAll().size();
    }

    @Transactional
    public long getSearchedEmployeeSize(String name,String filter) {
        if(!filter.equals("all")) return employeeRepository.findAllByDisplayNameStartingWithAndStatus(name,filter).size();
        return employeeRepository.findAllByDisplayNameStartingWith(name).size();
    }

    @Transactional
    public void reset() {
        companyRepository.deleteAll();
        employeeRepository.deleteAll();
    }
}