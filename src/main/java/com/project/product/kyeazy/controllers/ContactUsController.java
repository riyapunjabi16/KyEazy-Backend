package com.product.product.kyeazy.controllers;

import com.product.product.kyeazy.dto.ActionDTO;
import com.product.product.kyeazy.entities.ContactUs;
import com.product.product.kyeazy.services.ContactUsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
public class ContactUsController {

    private ContactUsService contactUsService;

    @Autowired
    public ContactUsController(ContactUsService contactUsService) {
        this.contactUsService = contactUsService;
    }

    @PostMapping(value = "/contact-us")
    public ActionDTO register(@RequestBody ContactUs contactUs) {
        return contactUsService.register(contactUs);
    }
}
