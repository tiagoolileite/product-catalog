package br.com.tiagoolileite.productcatalog.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

class ProductDTOTest {
    @Test
    void allArgsConstructor_setsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();
        ProductDTO dto = new ProductDTO(
            1L,
            "SKU-001",
            "Product Name",
            "Description",
            new BigDecimal("99.99"),
            new BigDecimal("10.00"),
            5,
            2L,
            new BigDecimal("4.5"),
            true,
            now,
            now
        );
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSku()).isEqualTo("SKU-001");
        assertThat(dto.getName()).isEqualTo("Product Name");
        assertThat(dto.getDescription()).isEqualTo("Description");
        assertThat(dto.getPrice()).isEqualByComparingTo("99.99");
        assertThat(dto.getDiscountPercentage()).isEqualByComparingTo("10.00");
        assertThat(dto.getStock()).isEqualTo(5);
        assertThat(dto.getBrandId()).isEqualTo(2L);
        assertThat(dto.getRating()).isEqualByComparingTo("4.5");
        assertThat(dto.getActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void noArgsConstructor_setsFieldsToNullOrDefault() {
        ProductDTO dto = new ProductDTO();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getSku()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getDescription()).isNull();
        assertThat(dto.getPrice()).isNull();
        assertThat(dto.getDiscountPercentage()).isEqualTo(BigDecimal.ZERO);
        assertThat(dto.getStock()).isNull();
        assertThat(dto.getBrandId()).isNull();
        assertThat(dto.getRating()).isNull();
        assertThat(dto.getActive()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    void settersAndGetters_workForAllFields() {
        ProductDTO dto = new ProductDTO();
        OffsetDateTime now = OffsetDateTime.now();
        dto.setId(10L);
        dto.setSku("SKU-010");
        dto.setName("Test Product");
        dto.setDescription("Test Desc");
        dto.setPrice(new BigDecimal("123.45"));
        dto.setDiscountPercentage(new BigDecimal("5.00"));
        dto.setStock(99);
        dto.setBrandId(7L);
        dto.setRating(new BigDecimal("3.75"));
        dto.setActive(false);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now.plusDays(1));

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getSku()).isEqualTo("SKU-010");
        assertThat(dto.getName()).isEqualTo("Test Product");
        assertThat(dto.getDescription()).isEqualTo("Test Desc");
        assertThat(dto.getPrice()).isEqualByComparingTo("123.45");
        assertThat(dto.getDiscountPercentage()).isEqualByComparingTo("5.00");
        assertThat(dto.getStock()).isEqualTo(99);
        assertThat(dto.getBrandId()).isEqualTo(7L);
        assertThat(dto.getRating()).isEqualByComparingTo("3.75");
        assertThat(dto.getActive()).isFalse();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now.plusDays(1));
    }

    @Test
    void equalsAndHashCode_shouldWorkForSameValues() {
        OffsetDateTime now = OffsetDateTime.now();
        ProductDTO dto1 = new ProductDTO(1L, "SKU", "Name", "Desc", new BigDecimal("1.00"), new BigDecimal("2.00"), 3, 4L, new BigDecimal("5.00"), true, now, now);
        ProductDTO dto2 = new ProductDTO(1L, "SKU", "Name", "Desc", new BigDecimal("1.00"), new BigDecimal("2.00"), 3, 4L, new BigDecimal("5.00"), true, now, now);
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).hasSameHashCodeAs(dto2.hashCode());
    }

    @Test
    void equalsAndHashCode_shouldDifferForDifferentValues() {
        ProductDTO dto1 = new ProductDTO();
        ProductDTO dto2 = new ProductDTO();
        dto2.setId(999L);
        assertThat(dto1).isNotEqualTo(dto2);
        assertThat(dto1.hashCode()).isNotEqualTo(dto2.hashCode());
    }
}


