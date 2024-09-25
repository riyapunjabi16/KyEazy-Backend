package com.product.product.kyeazy.controllers;

import com.product.product.kyeazy.dto.ActionDTO;
import com.product.product.kyeazy.dto.EmployeeDTO;
import com.product.product.kyeazy.entities.Employee;
import com.product.product.kyeazy.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @PatchMapping("/update-profile")
    public ActionDTO updateProfile(@RequestBody Employee employee)
    {
        System.out.println("In Controller");
        System.out.println(employee.getDocumentNumber());
        System.out.println(employee.getGender());
        System.out.println(employee.getQuestion());
        System.out.println(employee.getDocumentType());
        return  employeeService.updateProfileData(employee);
    }

    @PatchMapping(value="/update-captured-image/{id}",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionDTO updateCapturedImage(@PathVariable Integer id, @RequestParam("profilePicture") MultipartFile profilePicture) throws IOException
    {
        return  employeeService.updateEmployeeImage(id,profilePicture);
    }

    @PatchMapping(value="/update-video/{id}",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionDTO updateVideo(@PathVariable Integer id, @RequestParam("employeeVideo") MultipartFile employeeVideo) throws IOException
    {
        return  employeeService.updateEmployeeVideo(id,employeeVideo);
    }

    @PatchMapping(value="/update-document/{id}",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionDTO updateDocument(@PathVariable Integer id, @RequestParam("employeeDocument") MultipartFile employeeVideo) throws IOException
    {
        return  employeeService.updateEmployeeDocument(id,employeeVideo);
    }

    @GetMapping(value="/{id}")
    public EmployeeDTO getEmployee(@PathVariable Integer id)
    {
        return  employeeService.getEmployeeData(id);
    }

    @GetMapping(value="/update-status/{id}")
    public ActionDTO updateStatus(@PathVariable Integer id)
    {
        return  employeeService.updateEmployeeStatus(id);
    }

    @GetMapping("/view-profile/{employeeId}")
    public EmployeeDTO viewProfile(@PathVariable Integer employeeId)
    {
        return  employeeService.getEmployeeData(employeeId);
    }

}
