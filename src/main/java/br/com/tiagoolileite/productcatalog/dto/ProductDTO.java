package br.com.tiagoolileite.productcatalog.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPercentage;
    private Integer stock;
    private Long brandId;
    private BigDecimal rating;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
