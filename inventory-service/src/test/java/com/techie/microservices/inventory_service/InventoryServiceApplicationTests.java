package com.techie.microservices.inventory_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

import io.restassured.RestAssured;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");

    @LocalServerPort
    private Integer port;
            
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:";
        RestAssured.port = port;
    }
    static {
        mySQLContainer.start();
    }

    @Test
    void shouldReadInventory() {
       var response = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=1")
                .then().log().all()
                .statusCode(200)
                .extract().response()
                .as(Boolean.class);

        assertTrue(Boolean.TRUE.equals(response));

    var negativeResponse = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=1000")
                .then().log().all()
                .statusCode(200)
                .extract().response()
                .as(Boolean.class);

        assertFalse(Boolean.TRUE.equals(negativeResponse));
    }
}