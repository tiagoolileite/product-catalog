package br.com.tiagoolileite.productcatalog.service;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
}
