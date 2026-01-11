package br.com.tiagoolileite.productcatalog.service.impl;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import br.com.tiagoolileite.productcatalog.mapper.ProductMapper;
import br.com.tiagoolileite.productcatalog.repository.ProductRepository;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private ProductMapper productMapper;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {

        Product product = productMapper.toProduct(productDTO);
        Product savedProduct = productRepository.save(product);

        return productMapper.toProductDTO(savedProduct);
    }
}
