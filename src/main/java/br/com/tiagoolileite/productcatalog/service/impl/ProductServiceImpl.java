package br.com.tiagoolileite.productcatalog.service.impl;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import br.com.tiagoolileite.productcatalog.exception.ResourceNotFoundException;
import br.com.tiagoolileite.productcatalog.mapper.ProductMapper;
import br.com.tiagoolileite.productcatalog.repository.ProductRepository;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import br.com.tiagoolileite.productcatalog.specification.ProductSpecification;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Override
    public ProductDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not exist with id: " + productId));

        return productMapper.toProductDTO(product);
    }

    @Override
    public Page<ProductDTO> searchProducts(ProductFilterDTO productFilterDTO) {
        Specification<Product> spec = ProductSpecification.buildSpecification(productFilterDTO);

        Pageable pageable = buildPageable(productFilterDTO);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        return productPage.map(productMapper::toProductDTO);
    }

    /**
     * Builds a Pageable object from the provided filter DTO, handling pagination and sorting parameters.
     *
     * @param productFilterDTO The filter DTO containing pagination and sorting parameters
     * @return A Pageable object configured with page number, size, and sorting criteria
     */
    private Pageable buildPageable(ProductFilterDTO productFilterDTO) {
        // Initialize with unsorted to avoid null pointer exceptions
        Sort sort = Sort.unsorted();

        // Parse sort parameter if provided (expected format: "fieldName,direction")
        if (productFilterDTO.getSort() != null && !productFilterDTO.getSort().trim().isEmpty()) {
            String[] sortParts = productFilterDTO.getSort().split(",");

            // Ensure we have both field name and direction (minimum 2 parts)
            if (sortParts.length >= 2) {
                String field = sortParts[0].trim(); // Extract field name (e.g., "price", "createdAt")
                String direction = sortParts[1].trim().toLowerCase(); // Extract direction and normalize to lowercase

                // Create Sort object based on direction ("desc" for descending, anything else for ascending)
                sort = direction.equals("desc")
                        ? Sort.by(Sort.Direction.DESC, field)
                        : Sort.by(Sort.Direction.ASC, field);
            }
        }

        // Validate and set page size (must be between 1 and 100, default to 20 if invalid)
        int size = (productFilterDTO.getSize() != null && productFilterDTO.getSize() > 0 && productFilterDTO.getSize() <= 100)
                ? productFilterDTO.getSize() : 20;

        // Validate and set page number (must be 0 or greater, default to 0 if invalid)
        int page = (productFilterDTO.getPage() != null && productFilterDTO.getPage() >= 0)
                ? productFilterDTO.getPage() : 0;

        // Create and return PageRequest with validated page, size, and sort parameters
        return PageRequest.of(page, size, sort);
    }
}
