package com.chary.bhaumik.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chary.bhaumik.service.ClientService;

@RestController
public class ClientController 
{
	@Autowired
	ClientService clientService;
	
	@GetMapping("/test")
    public String test()
	{
        return clientService.getTestService();
    }
}
