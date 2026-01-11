package br.com.tiagoolileite.productcatalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    @NotBlank
    @Size(max = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discountPercentage;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer stock;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(precision = 3, scale = 2)
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "5.00")
    private BigDecimal rating;

    @Column(nullable = false)
    @NotNull
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    @NotNull
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        this.updatedAt = now;
        if (this.price == null) this.price = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (this.discountPercentage == null) this.discountPercentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (this.rating == null) this.rating = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (this.active == null) this.active = Boolean.TRUE;
        if (this.stock == null) this.stock = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @Transient
    public BigDecimal getFinalPrice() {
        if (price == null) return null;
        BigDecimal dp = discountPercentage == null ? BigDecimal.ZERO : discountPercentage;
        BigDecimal discount = price.multiply(dp).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return price.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}
