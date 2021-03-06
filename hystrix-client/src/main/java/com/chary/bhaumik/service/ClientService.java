package com.chary.bhaumik.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class ClientService 
{
	@Autowired
	EurekaClient eurekaClient;
	
	@Autowired
	RestTemplate restTemplate;
	
	@HystrixCommand(fallbackMethod = "failedTestService")
	public String getTestService()
	{
		InstanceInfo instanceInfo=eurekaClient.getNextServerFromEureka("hystrix-service", false);
		String baseUrl=instanceInfo.getHomePageUrl();
		String result=restTemplate.getForObject(baseUrl+"demo", String.class);
		return result;
	}
	
	public String failedTestService()
	{
		return "Failure";
	}

}
