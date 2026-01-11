package br.com.tiagoolileite.productcatalog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProductFilterDTO {
    private String q;
    private Long brandId;
    private Long categoryId;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private BigDecimal discountMin;
    private Boolean inStock;
    private Boolean active;
    private BigDecimal ratingMin;
    private OffsetDateTime createdFrom;
    private OffsetDateTime createdTo;
    private List<String> attrs;

    // Pagination and Sorting
    private Integer page = 0;
    private Integer size = 20;
    private String sort = "createdAt,desc";
}