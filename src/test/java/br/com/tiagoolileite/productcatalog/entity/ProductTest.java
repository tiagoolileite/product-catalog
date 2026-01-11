package br.com.tiagoolileite.productcatalog.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import java.time.Duration;

class ProductTest {
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void getFinalPrice_shouldCalculateCorrectly() {
        Product p = new Product();
        p.setPrice(new BigDecimal("100.00"));
        p.setDiscountPercentage(new BigDecimal("10.00"));
        assertThat(p.getFinalPrice()).isEqualByComparingTo(new BigDecimal("90.00"));
    }

    @Test
    void getFinalPrice_shouldHandleNullDiscount() {
        Product p = new Product();
        p.setPrice(new BigDecimal("50.00"));
        p.setDiscountPercentage(null);
        assertThat(p.getFinalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void getFinalPrice_shouldHandleNullPrice() {
        Product p = new Product();
        p.setPrice(null);
        p.setDiscountPercentage(new BigDecimal("10.00"));
        assertThat(p.getFinalPrice()).isNull();
    }

    @Test
    void onCreate_shouldSetDefaults() {
        Product p = new Product();
        p.onCreate();
        assertThat(p.getCreatedAt()).isNotNull();
        assertThat(p.getUpdatedAt()).isNotNull();
        assertThat(p.getPrice()).isNotNull();
        assertThat(p.getDiscountPercentage()).isNotNull();
        assertThat(p.getRating()).isNotNull();
        assertThat(p.getActive()).isTrue();
        assertThat(p.getStock()).isZero();
    }

    @Test
    void onUpdate_shouldUpdateTimestamp() {
        Product p = new Product();
        p.onCreate();
        OffsetDateTime before = p.getUpdatedAt();
        await().atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            p.onUpdate();
            assertThat(p.getUpdatedAt()).isAfter(before);
        });
    }

    @Test
    void validation_shouldFailForInvalidFields() {
        Product p = new Product();
        p.setSku("");
        p.setName(null);
        p.setPrice(new BigDecimal("-1.00"));
        p.setDiscountPercentage(new BigDecimal("101.00"));
        p.setStock(-5);
        p.setRating(new BigDecimal("6.00"));
        p.setActive(null);
        p.setCreatedAt(null);
        p.setUpdatedAt(null);

        Set<ConstraintViolation<Product>> violations = validator.validate(p);
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("sku","name","price","discountPercentage","stock","rating","active","createdAt","updatedAt");
    }

    @Test
    void validation_shouldPassForValidProduct() {
        Product p = new Product();
        p.setSku("SKU-123");
        p.setName("Product Name");
        p.setPrice(new BigDecimal("99.99"));
        p.setDiscountPercentage(new BigDecimal("10.00"));
        p.setStock(5);
        p.setBrandId(1L);
        p.setRating(new BigDecimal("4.50"));
        p.setActive(true);
        p.setCreatedAt(OffsetDateTime.now());
        p.setUpdatedAt(OffsetDateTime.now());
        Set<ConstraintViolation<Product>> violations = validator.validate(p);
        assertThat(violations).isEmpty();
    }

    @Test
    void onCreate_shouldNotOverridePresetValues() {
        Product p = new Product();

        OffsetDateTime created = OffsetDateTime.now().minusHours(1);
        OffsetDateTime updated = OffsetDateTime.now().minusHours(1);

        p.setCreatedAt(created);                // branch: createdAt != null
        p.setUpdatedAt(updated);                // será sempre sobrescrito por onCreate()
        p.setPrice(new BigDecimal("123.45"));   // branch: price != null
        p.setDiscountPercentage(new BigDecimal("7.50")); // branch: discountPercentage != null
        p.setRating(new BigDecimal("4.20"));    // branch: rating != null
        p.setActive(false);                     // branch: active != null
        p.setStock(10);                         // branch: stock != null

        // Campos obrigatórios para não interferir em outras lógicas
        p.setSku("SKU-KEEP");
        p.setName("Name-KEEP");

        p.onCreate();

        // Não deve sobrescrever valores já definidos
        assertThat(p.getCreatedAt()).isEqualTo(created);
        assertThat(p.getPrice()).isEqualByComparingTo("123.45");
        assertThat(p.getDiscountPercentage()).isEqualByComparingTo("7.50");
        assertThat(p.getRating()).isEqualByComparingTo("4.20");
        assertThat(p.getActive()).isFalse();
        assertThat(p.getStock()).isEqualTo(10);

        // updatedAt SEMPRE é atualizado em onCreate()
        assertThat(p.getUpdatedAt()).isAfter(updated);
    }
}
