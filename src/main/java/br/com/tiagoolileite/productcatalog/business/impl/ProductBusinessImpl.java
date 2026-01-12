package br.com.tiagoolileite.productcatalog.business.impl;

import br.com.tiagoolileite.productcatalog.business.ProductBusiness;
import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ProductBusinessImpl implements ProductBusiness {

    /**
     * Performs additional business validation for product creation
     */
    @Override
    public void validateProductCreation(ProductDTO productDTO) {
        // Validate SKU format (additional business rules)
        if (productDTO.getSku() != null && !isValidSkuFormat(productDTO.getSku())) {
            throw new ValidationException("sku", productDTO.getSku(),
                    "SKU must follow the pattern: PREFIX-PRODUCT-VARIANT-NUMBER");
        }

        // Validate price consistency with discount
        if (productDTO.getPrice() != null && productDTO.getDiscountPercentage() != null) {
            BigDecimal maxDiscount = productDTO.getPrice().multiply(new BigDecimal("0.9"));
            BigDecimal discountAmount = productDTO.getPrice()
                    .multiply(productDTO.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (discountAmount.compareTo(maxDiscount) > 0) {
                throw new ValidationException("discountPercentage", productDTO.getDiscountPercentage(),
                        "Discount cannot exceed 90% of the product price");
            }
        }
    }

    private boolean isValidSkuFormat(String sku) {
        // Example: SKU-RUNNER-PRO-001
        return sku.matches("^[A-Z]{2,10}-[A-Z0-9]{2,20}-[A-Z0-9]{2,10}-\\d{3,6}$");
    }
}
