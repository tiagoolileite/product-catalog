package br.com.tiagoolileite.productcatalog.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;

    @NotBlank(message = "SKU is required and cannot be blank")
    @Size(max = 64, message = "SKU must not exceed 64 characters")
    @Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
    private String sku;

    @NotBlank(message = "Product name is required and cannot be blank")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Price must not exceed 999,999,999.99")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount percentage must be greater than or equal to 0")
    @DecimalMax(value = "100.00", message = "Discount percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount percentage must have at most 3 integer digits and 2 decimal places")
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    @Max(value = 999999, message = "Stock quantity must not exceed 999,999")
    private Integer stock;

    @NotNull(message = "Brand ID is required")
    @Positive(message = "Brand ID must be a positive number")
    private Long brandId;

    @DecimalMin(value = "0.00", message = "Rating must be greater than or equal to 0")
    @DecimalMax(value = "5.00", message = "Rating must not exceed 5.00")
    @Digits(integer = 1, fraction = 2, message = "Rating must have at most 1 integer digit and 2 decimal places")
    private BigDecimal rating;

    private Boolean active;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
