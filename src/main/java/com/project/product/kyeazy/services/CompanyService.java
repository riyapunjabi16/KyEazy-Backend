package com.product.product.kyeazy.services;

import com.product.product.kyeazy.dto.ActionDTO;
import com.product.product.kyeazy.dto.CompanyDTO;
import com.product.product.kyeazy.dto.EmployeeDTO;
import com.product.product.kyeazy.entities.Company;
import com.product.product.kyeazy.entities.Employee;
import com.product.product.kyeazy.exceptions.DataAlreadyExistsException;
import com.product.product.kyeazy.repositories.CompanyRepository;
import com.product.product.kyeazy.repositories.EmployeeRepository;
import com.product.product.kyeazy.utils.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
public class CompanyService {

    private EmployeeRepository employeeRepository;
    private CompanyRepository companyRepository;
    private EmailSender emailSender;

    @Autowired
    public CompanyService(CompanyRepository companyRepository, EmailSender emailSender, EmployeeRepository employeeRepository) {
        this.companyRepository = companyRepository;
        this.emailSender = emailSender;
        this.employeeRepository = employeeRepository;
    }

    // company

    @Transactional
    public CompanyDTO register(Company company) {
        List<Company> companies = companyRepository.findAll();
        for (Company companyToCheck : companies) {
            if (companyToCheck.getUsername().equals(company.getUsername()))
                throw new DataAlreadyExistsException("The Username is already registered !!!!!");
            if (companyToCheck.getCinNumber().equalsIgnoreCase(company.getCinNumber()))
                throw new DataAlreadyExistsException("The Company is already registered !!!!!");
        }
        Company addedCompany = companyRepository.save(company);
        return Parser.parseCompany(addedCompany);
    }

    public EmployeeDTO registerEmployee(Employee employee, int companyId) throws Exception {
        if(companyRepository.findById(companyId).get().getCoins()<50) throw new Exception("Not Enough Coins");
        Company company = companyRepository.findById(companyId).get();
        company.setCoins(company.getCoins()-50);
        companyRepository.save(company);
        List<Employee> employees = employeeRepository.findAll();
        for (Employee employeeToCheck : employees) {
            if (employeeToCheck.getEmailID().equals(employee.getEmailID()))
            {
                if(employeeToCheck.getStatus().equals("Registered"))
                {
                    employeeToCheck.setCompanyId(companyId);
                    Parser.parseEmployee(employeeRepository.save(employee));
                    // return new ActionDTO(employee.getEmployeeId(),false,"Already Exists") ;
                }
                if(!employeeToCheck.getStatus().equals("Reported") )
                {
                    employeeToCheck.setStatus("Existed");
                    employeeToCheck.setPreviousStatus(employeeToCheck.getStatus());
                    employeeToCheck.setCompanyId(companyId);
                    Parser.parseEmployee(employeeRepository.save(employeeToCheck));
                    // return new ActionDTO(employee.getEmployeeId(),false,"Already Exists") ;
                }
            }
        }
        String username = this.generateUsername(employee);
        String passkey = String.valueOf(this.generatePassword(employee));
        String link = "https://kycfront-amxbp6pvia-as.a.run.app/";
        String mailBody = "Hey " + employee.getFirstName() + "," + "Your Id password for doing kyc is  Username: " + username + " Password:" + passkey + " Link:" + link;
        this.emailSender.sendMail(employee.getEmailID(), "Regarding KYC", mailBody);
        employee.setUsername(username);
        employee.setPassword(passkey);
        employee.setStatus("Registered");
        employee.setDateTimeOfApplication(new Date());
        employee.setCompanyId(companyId);
        Employee addedEmployee = employeeRepository.save(employee);
        return Parser.parseEmployee(addedEmployee);
//        return new ActionDTO(addedEmployee.getEmployeeId(), true, "Employee KYC Under Progress Wait for 2-3 days");
    }


    public ActionDTO registerEmployees(Integer id, MultipartFile employeesList) throws Exception {
        String DELIMITER = ",";
        InputStreamReader isr = new InputStreamReader(employeesList.getInputStream(),
                StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String line;
        Employee employee;
        br.readLine();
        List<Employee> employees=new LinkedList<>();
        while ((line = br.readLine()) != null) {
            employee = new Employee();
            String[] columns = line.split(DELIMITER);
            employee.setFirstName(columns[0]);
            employee.setLastName(columns[1]);
            employee.setEmailID(columns[2]);
            employee.setContactNumber(columns[3]);
            employee.setCompanyId(id);
            employee.setStatus("Registered");
            employee.setDateTimeOfApplication(new Date());
            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee employeeToCheck : allEmployees) {
                if (employeeToCheck.getEmailID().equals(employee.getEmailID()))
                {
                    employee.setEmployeeId(employeeToCheck.getEmployeeId());
                    if(employeeToCheck.getStatus().equals("Registered"))
                    {
                        employee.setCompanyId(id);
                        employees.add(employee);
                        break;
                    }
                    if(!employeeToCheck.getStatus().equals("Reported"))
                    {
                        employee.setStatus("Existed");
                        employee.setPreviousStatus(employeeToCheck.getStatus());
                        employee.setCompanyId(id);
                        break;
                    }
                }
            }
            employees.add(employee);
        }
        if(companyRepository.findById(id).get().getCoins()<employees.size()*50) throw new Exception("Not Enough Coins");
        Company company=companyRepository.findById(id).get();
        company.setCoins(company.getCoins()-employees.size()*50);
        employeeRepository.saveAll(employees);
        return new ActionDTO(1, true, "Employees Added Successfully !");
    }

    @Transactional
    public ActionDTO reportEmployee(Integer employeeId, String message){
        Employee employee=employeeRepository.findById(employeeId).get();
        employee.setStatus("Reported");
        employee.setReview(message);
        employeeRepository.save(employee);
        return new ActionDTO(1,true,"Reporting Done");
    }

    public EmployeeDTO reKycEmployee(Integer employeeId) {
        Employee employee=employeeRepository.findById(employeeId).get();
        if(employee.getStatus().equals("Rejected")) {
            Company company = companyRepository.findById(employee.getCompanyId()).get();
            company.setCoins(company.getCoins()-50);
            if(company.getCoins() < 0) {
                return null;
            }
            companyRepository.save(company);
        }
        //employee.setLock(false);
        employee.setStatus("Registered");
        Employee savedEmployee=employeeRepository.save(employee);
        return Parser.parseEmployee(savedEmployee);
    }

    @Transactional
    public ActionDTO updateCompanyIcon(Integer companyId, MultipartFile icon) throws IOException
    {
        Company company=companyRepository.findById(companyId).get();
        company.setIcon(icon.getBytes());
        Company savedCompany = companyRepository.save(company);
        return new ActionDTO(savedCompany.getCompanyId(),true,"Company Details Added Successfully.");
    }

    @Transactional
    public CompanyDTO getCompanyDetails(Integer id) {
        Company company = getCompanyById(id);
        return Parser.parseCompany(company);
    }

    @Transactional
    public Company getCompanyByUsername(String userName) {
        List<Company> companies = companyRepository.findAll();
        for (Company companyToCheck : companies) {
            if (companyToCheck.getUsername().equals(userName)) return companyToCheck;

        }
        return null;
    }

    @Transactional
    private Company getCompanyById(Integer companyId) {
        return companyRepository.findById(companyId).get();
    }

    // employees

    @Transactional
    public List<EmployeeDTO> getEmployees(Integer id, Integer pageNumber, Integer pageSize,String sort,String filter) {
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
            employees = employeeRepository.findAllByStatusAndCompanyId(filter,id,pageable);
        } else {
            employees = employeeRepository.findAllByCompanyId(id,pageable);
        }
        for (Employee employee : employees) {
            employeeDTOS.add(Parser.parseEmployee(employee));
        }
        return employeeDTOS;
    }

    @Transactional
    public List<EmployeeDTO> getEmployeesByName(Integer companyId, String name, Integer pageNumber, Integer pageSize,String sort, String filter) {

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
            employees = employeeRepository.findAllByDisplayNameStartingWithAndCompanyIdAndStatus(name,companyId,filter,pageable);
        } else {
            employees = employeeRepository.findAllByDisplayNameStartingWithAndCompanyId(name,companyId,pageable);
        }

        for (Employee employee : employees) {
            employeeDTOS.add(Parser.parseEmployee(employee));
        }

        return employeeDTOS;
    }

    // count

    @Transactional
    public long getEmployeesSize(int id, String filter) {
        List<Employee> employees;

        if(!filter.equals("all")) {
            employees = employeeRepository.findAllByStatusAndCompanyId(filter,id);
        } else {
            employees = employeeRepository.findAllByCompanyId(id);
        }

        return employees.size();
    }

    @Transactional
    public long getSearchedEmployeesSize(int id,String name,String filter) {
        List<Employee> employees;
        if(!filter.equals("all")) {
            employees = employeeRepository.findAllByDisplayNameStartingWithAndCompanyIdAndStatus(name,id,filter);
        } else {
            employees = employeeRepository.findAllByDisplayNameStartingWithAndCompanyId(name,id);
        }
        return employees.size();
    }

    // util

    private String generateUsername(Employee employee) {
        String username = "";
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        username += employee.getFirstName().substring(0, 1);
        username += employee.getLastName();
        username += uuidAsString.substring(0, 3);
        return username;
    }

    private static char[] generatePassword(Employee employee) {
        String name = employee.getFirstName();
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$_";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[7];
        password[0] = name.toUpperCase().charAt(0);
        password[1] = name.toLowerCase().charAt(1);
        password[2] = name.toLowerCase().charAt(2);
        password[3] = numbers.charAt(random.nextInt(numbers.length()));
        password[4] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        for (int i = 5; i < 7; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return password;
    }

}
