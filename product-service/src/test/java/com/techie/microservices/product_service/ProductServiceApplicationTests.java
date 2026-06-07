package com.techie.microservices.product_service;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductServiceApplicationTests {


	@Container
	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
	
	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		RestAssured.basePath = "/api";
	}
    
	// Container lifecycle is managed by Testcontainers + Spring Boot @ServiceConnection

	@Test
	void shouldCreateProduct() {
		String requestBody = """
				{
					"name": "iPhone 15",
					"description": "iPhone 15 is the latest smartphone from Apple, featuring a sleek design, powerful performance, and advanced camera capabilities.",
					"price": 1000
				}
				""";
		RestAssured.given()
			.contentType(ContentType.JSON)
			.body(requestBody)
		.when()
			.post("/product")
		.then()
			.statusCode(201)
			.body("id", Matchers.notNullValue())
			.body("name", Matchers.equalTo("iPhone 15"))
			.body("description", Matchers.equalTo("iPhone 15 is the latest smartphone from Apple, featuring a sleek design, powerful performance, and advanced camera capabilities."))
			.body("price", Matchers.equalTo(1000));
	}

}
