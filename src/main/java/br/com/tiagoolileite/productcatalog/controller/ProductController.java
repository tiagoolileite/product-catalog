package br.com.tiagoolileite.productcatalog.controller;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody @Valid ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        ProductDTO foundProduct = productService.getProductById(productId);

        return ResponseEntity.status(HttpStatus.OK).body(foundProduct);
    }

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) java.math.BigDecimal priceMin,
            @RequestParam(required = false) java.math.BigDecimal priceMax,
            @RequestParam(required = false) java.math.BigDecimal discountMin,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) java.math.BigDecimal ratingMin,
            @RequestParam(required = false) java.time.OffsetDateTime createdFrom,
            @RequestParam(required = false) java.time.OffsetDateTime createdTo,
            @RequestParam(required = false) java.util.List<String> attrs,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        ProductFilterDTO productFilterDto = new ProductFilterDTO();
        productFilterDto.setQ(q);
        productFilterDto.setBrandId(brandId);
        productFilterDto.setCategoryId(categoryId);
        productFilterDto.setPriceMin(priceMin);
        productFilterDto.setPriceMax(priceMax);
        productFilterDto.setDiscountMin(discountMin);
        productFilterDto.setInStock(inStock);
        productFilterDto.setActive(active);
        productFilterDto.setRatingMin(ratingMin);
        productFilterDto.setCreatedFrom(createdFrom);
        productFilterDto.setCreatedTo(createdTo);
        productFilterDto.setAttrs(attrs);
        productFilterDto.setSort(sort);
        productFilterDto.setPage(page);
        productFilterDto.setSize(size);

        Page<ProductDTO> products = productService.searchProducts(productFilterDto);
        return ResponseEntity.ok(products);
    }
}
