package br.com.tiagoolileite.productcatalog.business;

import br.com.tiagoolileite.productcatalog.business.impl.ProductBusinessImpl;
import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductBusinessImpl Tests")
class ProductBusinessImplTest {

    @InjectMocks
    private ProductBusinessImpl productBusiness;

    private ProductDTO validProductDTO;

    @BeforeEach
    void setUp() {
        validProductDTO = ProductDTO.builder()
                .sku("SKU-RUNNER-PRO-001")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .discountPercentage(new BigDecimal("10.00"))
                .stock(50)
                .brandId(1L)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("validateProductCreation - Happy Path")
    class ValidateProductCreationHappyPath {

        @Test
        @DisplayName("Should pass validation with valid product data")
        void shouldPassValidationWithValidProductData() {
            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation when SKU is null")
        void shouldPassValidationWhenSkuIsNull() {
            validProductDTO.setSku(null);

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation when price is null")
        void shouldPassValidationWhenPriceIsNull() {
            validProductDTO.setPrice(null);

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation when discount percentage is null")
        void shouldPassValidationWhenDiscountPercentageIsNull() {
            validProductDTO.setDiscountPercentage(null);

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation with zero discount")
        void shouldPassValidationWithZeroDiscount() {
            validProductDTO.setDiscountPercentage(BigDecimal.ZERO);

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should pass validation with 90% discount (maximum allowed)")
        void shouldPassValidationWithMaximumDiscount() {
            validProductDTO.setPrice(new BigDecimal("100.00"));
            validProductDTO.setDiscountPercentage(new BigDecimal("90.00"));

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateProductCreation - SKU Validation")
    class SkuValidation {

        @ParameterizedTest
        @ValueSource(strings = {
                "SKU-RUNNER-PRO-001",
                "ABC-PRODUCT-VAR-123456",
                "XY-AB-CD-123",
                "ABCDEFGHIJ-ABCDEFGHIJ1234567890-ABCDEFGHIJ-123456"
        })
        @DisplayName("Should accept valid SKU formats")
        void shouldAcceptValidSkuFormats(String validSku) {
            validProductDTO.setSku(validSku);

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-sku",
                "SKU-RUNNER-PRO",
                "SKU-RUNNER-PRO-12",
                "SKU-RUNNER-PRO-1234567",
                "A-RUNNER-PRO-001",
                "ABCDEFGHIJK-RUNNER-PRO-001",
                "SKU-A-PRO-001",
                "SKU-ABCDEFGHIJ123456789012345-PRO-001",
                "SKU-RUNNER-P-001",
                "SKU-RUNNER-ABCDEFGHIJK-001",
                "SKU_RUNNER_PRO_001",
                "SKU RUNNER PRO 001",
                "SKU-RUNNER-PRO-001-EXTRA",
                "",
                "SKU-runner-PRO-001",
                "SKU-RUNNER-pro-001",
                "SKU-RUNNER-PRO-ABC"
        })
        @DisplayName("Should reject invalid SKU formats")
        void shouldRejectInvalidSkuFormats(String invalidSku) {
            validProductDTO.setSku(invalidSku);

            assertThatThrownBy(() -> productBusiness.validateProductCreation(validProductDTO))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("SKU must follow the pattern: PREFIX-PRODUCT-VARIANT-NUMBER")
                    .extracting("field")
                    .isEqualTo("sku");
        }
    }

    @Nested
    @DisplayName("validateProductCreation - Discount Validation")
    class DiscountValidation {

        @ParameterizedTest(name = "Should reject discount {1}% for price {0}")
        @MethodSource("invalidDiscountScenarios")
        @DisplayName("Should reject discounts exceeding 90% of price")
        void shouldRejectDiscountsExceedingNinetyPercent(
                BigDecimal price,
                BigDecimal discountPercentage
        ) {
            validProductDTO.setPrice(price);
            validProductDTO.setDiscountPercentage(discountPercentage);

            assertThatThrownBy(() -> productBusiness.validateProductCreation(validProductDTO))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Discount cannot exceed 90% of the product price")
                    .extracting("field")
                    .isEqualTo("discountPercentage");
        }

        static Stream<Arguments> invalidDiscountScenarios() {
            return Stream.of(
                    Arguments.of(new BigDecimal("100.00"), new BigDecimal("91.00")),
                    Arguments.of(new BigDecimal("100.00"), new BigDecimal("100.00")),
                    Arguments.of(new BigDecimal("99.99"), new BigDecimal("90.01"))
            );
        }

        @Test
        @DisplayName("Should handle large price values correctly")
        void shouldHandleLargePriceValues() {
            validProductDTO.setPrice(new BigDecimal("999999.99"));
            validProductDTO.setDiscountPercentage(new BigDecimal("90.00"));

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle precision in discount calculation")
        void shouldHandlePrecisionInDiscountCalculation() {
            validProductDTO.setPrice(new BigDecimal("33.33"));
            validProductDTO.setDiscountPercentage(new BigDecimal("89.99"));

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("validateProductCreation - Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null product DTO gracefully")
        void shouldHandleNullProductDto() {
            assertThatThrownBy(() -> productBusiness.validateProductCreation(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should pass validation with all optional fields null")
        void shouldPassValidationWithAllOptionalFieldsNull() {
            ProductDTO minimalProduct = ProductDTO.builder().build();

            assertThatCode(() -> productBusiness.validateProductCreation(minimalProduct))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should validate multiple rules simultaneously")
        void shouldValidateMultipleRulesSimultaneously() {
            validProductDTO.setSku("invalid-sku-format");
            validProductDTO.setDiscountPercentage(new BigDecimal("95.00"));

            assertThatThrownBy(() -> productBusiness.validateProductCreation(validProductDTO))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("SKU must follow the pattern: PREFIX-PRODUCT-VARIANT-NUMBER");
        }

        @Test
        @DisplayName("Should handle zero price with discount")
        void shouldHandleZeroPriceWithDiscount() {
            validProductDTO.setPrice(BigDecimal.ZERO);
            validProductDTO.setDiscountPercentage(new BigDecimal("10.00"));

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle negative discount percentage")
        void shouldHandleNegativeDiscountPercentage() {
            validProductDTO.setPrice(new BigDecimal("100.00"));
            validProductDTO.setDiscountPercentage(new BigDecimal("-5.00"));

            assertThatCode(() -> productBusiness.validateProductCreation(validProductDTO))
                    .doesNotThrowAnyException();
        }
    }
}
