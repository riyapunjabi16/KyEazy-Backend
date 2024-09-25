package com.product.product.kyeazy.services;

import com.product.product.kyeazy.dto.ActionDTO;
import com.product.product.kyeazy.dto.EmployeeDTO;
import com.product.product.kyeazy.entities.Employee;
import com.product.product.kyeazy.repositories.EmployeeRepository;
import com.product.product.kyeazy.utils.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeDTO getEmployeeData(Integer employeeId)
    {
        return Parser.parseEmployee(employeeRepository.findById(employeeId).get());
    }

    public ActionDTO updateProfileData(Employee employeeDetails)
    {
        System.out.println(employeeDetails.getEmployeeId());
        System.out.println("In Service DTO");
        System.out.println(employeeDetails.getDocumentNumber());
        System.out.println(employeeDetails.getGender());
        System.out.println(employeeDetails.getQuestion());
        System.out.println(employeeDetails.getDocumentType());

        Employee employee=this.getEmployee(employeeDetails.getEmployeeId());
        employee.setDocumentNumber(employeeDetails.getDocumentNumber());
        employee.setDocumentType(employeeDetails.getDocumentType());
        employee.setAddress(employeeDetails.getAddress());
        employee.setGender(employeeDetails.getGender());
        employee.setDisplayName(employeeDetails.getFirstName()+" "+employee.getLastName());
        employee.setQuestion(employeeDetails.getQuestion());

        System.out.println("In Service DAO");
        System.out.println(employee.getDocumentNumber());
        System.out.println(employee.getGender());
        System.out.println(employee.getQuestion());
        System.out.println(employee.getDocumentType());

        Employee savedEmployee = null;
        try {
             savedEmployee = employeeRepository.save(employee);
        } catch ( Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("In Service After ADD");
        System.out.println(savedEmployee.getDocumentNumber());
        System.out.println(savedEmployee.getGender());
        System.out.println(savedEmployee.getQuestion());
        System.out.println(savedEmployee.getDocumentType());

        return new ActionDTO(savedEmployee.getEmployeeId(),true,"Employee Details Added Successfully.");
    }

    private Employee getEmployee(Integer employeeId) {
        return employeeRepository.findById(employeeId).get();
    }

    public ActionDTO updateEmployeeImage(Integer employeeId, MultipartFile profilePicture) throws IOException
    {
        System.out.println(employeeId);
        System.out.println(profilePicture.getOriginalFilename());
        Employee employee=this.getEmployee(employeeId);
        employee.setCapturedImage(profilePicture.getBytes());
        Employee savedEmployee = employeeRepository.save(employee);
        return new ActionDTO(savedEmployee.getEmployeeId(),true,"Employee Details Added Successfully.");
    }

    public ActionDTO updateEmployeeVideo(Integer employeeId, MultipartFile profileVideo) throws IOException
    {
        System.out.println(employeeId);
        System.out.println(profileVideo.getOriginalFilename());
        Employee employee=this.getEmployee(employeeId);
        String uploadDir="src/main/resources/employee_videos";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName=profileVideo.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.indexOf('.'),originalFileName.length());
        InputStream inputStream = profileVideo.getInputStream();
        Path filePath = uploadPath.resolve(employee.getUsername()+extension);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return new ActionDTO(employeeId,true,"Employee Details Added Successfully.");
    }

    public ActionDTO updateEmployeeDocument(Integer employeeId, MultipartFile document) throws IOException
    {
        System.out.println(employeeId);
        System.out.println(document.getOriginalFilename());
        Employee employee=this.getEmployee(employeeId);
        String uploadDir="src/main/resources/employee_documents";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName=document.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.indexOf('.'),originalFileName.length());
        InputStream inputStream = document.getInputStream();
        Path filePath = uploadPath.resolve(employee.getUsername()+extension);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return new ActionDTO(employeeId,true,"Employee Details Added Successfully.");
    }

    public ActionDTO updateEmployeeStatus(Integer employeeId)
    {
        System.out.println(employeeId);
        System.out.println("Status");
        Employee employee=getEmployee(employeeId);
        employee.setStatus("Pending");
        Employee savedEmployee = employeeRepository.save(employee);
        return new ActionDTO(savedEmployee.getEmployeeId(),true,"Employee Details Added Successfully.");
    }

    @Transactional
    public Employee getEmployeeByUsername(String userName) {

        List<Employee> employees=employeeRepository.findAll();
        for(Employee employeeToCheck:employees) {
            if(employeeToCheck.getUsername().equals(userName))
            {
                return employeeToCheck;
            }

        }
        return null;
    }


}
