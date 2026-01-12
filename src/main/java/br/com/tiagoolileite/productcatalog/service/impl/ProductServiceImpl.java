package br.com.tiagoolileite.productcatalog.service.impl;

import br.com.tiagoolileite.productcatalog.business.ProductBusiness;
import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import br.com.tiagoolileite.productcatalog.exception.DuplicateResourceException;
import br.com.tiagoolileite.productcatalog.exception.ResourceNotFoundException;
import br.com.tiagoolileite.productcatalog.helper.BuildPageableHelper;
import br.com.tiagoolileite.productcatalog.mapper.ProductMapper;
import br.com.tiagoolileite.productcatalog.repository.ProductRepository;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import br.com.tiagoolileite.productcatalog.specification.ProductSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private ProductMapper productMapper;
    private ProductBusiness productBusiness;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {

        productBusiness.validateProductCreation(productDTO);
        validateDatabaseConstraints(productDTO);

        Product product = productMapper.toProduct(productDTO);
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductDTO(savedProduct);
    }

    /**
     * Validates database constraints before saving to prevent ID consumption on failures
     */
    private void validateDatabaseConstraints(ProductDTO productDTO) {
        // Check SKU uniqueness
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product", "SKU", productDTO.getSku());
        }

        // Brand foreign key constraint
    }

    @Override
    public ProductDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not exist with id: " + productId));

        return productMapper.toProductDTO(product);
    }

    @Override
    public Page<ProductDTO> searchProducts(ProductFilterDTO productFilterDTO) {
        Specification<Product> spec = ProductSpecification.buildSpecification(productFilterDTO);

        Pageable pageable = BuildPageableHelper.buildPageableProductFilter(productFilterDTO);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(productMapper::toProductDTO);
    }

}
