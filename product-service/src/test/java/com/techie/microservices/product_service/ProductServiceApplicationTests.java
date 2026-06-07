package com.techie.microservices.product_service;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {


	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
	
	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}
	static {
		mongoDBContainer.start();
	}

	@Test
	void shouldCreateProduct() {
		String requestBody = """
				{
					"name": "Test Product",
					"description": "This is a test product",
					"price": 1000
				}
				""";
		RestAssured.given()
			.contentType("application/JSON")
			.body(requestBody)
		.when()
			.post("/api/products")
		.then()
			.statusCode(201)
			.body("id", Matchers.notNullValue())
			.body("name", Matchers.equalTo("Test Product"))
			.body("description", Matchers.equalTo("This is a test product"))
			.body("price", Matchers.equalTo(1000));
	}

}
