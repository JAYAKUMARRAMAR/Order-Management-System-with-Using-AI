package com.techie.microservices.product_service.model;

@Document(value = "product")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product {
    @Id
    private String Id;
    private String name;
    private String description;
    private BigDecimal price;

}
