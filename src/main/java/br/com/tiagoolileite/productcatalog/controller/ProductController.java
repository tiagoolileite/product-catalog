package br.com.tiagoolileite.productcatalog.controller;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
