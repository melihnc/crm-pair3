package com.tcellpair3.customerservice;

import com.turkcell.tcell.core.annotations.EnableSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableSecurity
public class CustomerserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerserviceApplication.class, args);
	}

}
