package br.com.tiagoolileite.productcatalog.service;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import org.springframework.data.domain.Page;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO getProductById(Long productId);

    Page<ProductDTO> searchProducts(ProductFilterDTO productFilterDTO);
}
