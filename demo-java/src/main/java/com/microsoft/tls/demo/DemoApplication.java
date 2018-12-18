package com.microsoft.tls.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@SpringBootApplication
@RestController
public class DemoApplication extends SpringBootServletInitializer {


	RestTemplate restTemplate = new RestTemplate();

	@RequestMapping("/hello")
	public String home() {
		return "Hello world";
	}


	// https://www.oracle.com/technetwork/articles/javase/security-137537.html
	//https://blogs.msdn.microsoft.com/appserviceteam/2017/06/27/installing-public-certificates-in-app-service/
	// set-location cert:\CurrentUser\My
	// get-childitem
	@RequestMapping("/callhello")
	public String callHello() {
		//System.setProperty("javax.net.ssl.trustStore","NUL");
		//System.setProperty("javax.net.ssl.trustStoreType","Windows-MY");
		ResponseEntity<String> response = restTemplate.exchange("https://consul.altostratus.me/demo/hello",HttpMethod.GET,null,String.class);

        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Invoked on host:" + ip + " and received payload: " + response.toString();
	}


	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DemoApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);
	}
	
}

