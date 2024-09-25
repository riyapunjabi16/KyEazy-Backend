package com.product.product.kyeazy.controllers;

import com.product.product.kyeazy.dto.JwtRequest;
import com.product.product.kyeazy.dto.JwtResponse;
import com.product.product.kyeazy.exceptions.response.ExceptionResponse;
import com.product.product.kyeazy.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @RequestMapping(value="/token",method = RequestMethod.POST)
    public ResponseEntity<JwtResponse> generateToken(@RequestBody JwtRequest jwtRequest) throws Exception
    {
        return jwtService.token(jwtRequest);
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleException(Exception exc) {
        ExceptionResponse error = new ExceptionResponse();
        error.setStatus(HttpStatus.LENGTH_REQUIRED.value());
        error.setMessage(exc.getMessage());
        error.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.LENGTH_REQUIRED);
    }
}
