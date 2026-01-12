package br.com.tiagoolileite.productcatalog.service;

import br.com.tiagoolileite.productcatalog.business.ProductBusiness;
import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import br.com.tiagoolileite.productcatalog.exception.DuplicateResourceException;
import br.com.tiagoolileite.productcatalog.exception.ResourceNotFoundException;
import br.com.tiagoolileite.productcatalog.helper.BuildPageableHelper;
import br.com.tiagoolileite.productcatalog.mapper.ProductMapper;
import br.com.tiagoolileite.productcatalog.repository.ProductRepository;
import br.com.tiagoolileite.productcatalog.service.impl.ProductServiceImpl;
import br.com.tiagoolileite.productcatalog.specification.ProductSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductBusiness productBusiness;

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductSpecification productSpecification;

    private ProductDTO productDTO;
    private Product productEntity;

    @BeforeEach
    void setUp() {
        productDTO = createValidProductDTO();
        productEntity = createValidProductEntity();
    }

    @Nested
    @DisplayName("Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Arrange
            productDTO.setId(null);
            productEntity.setId(null);

            Product savedProduct = createValidProductEntity();
            savedProduct.setId(1L);

            when(productMapper.toProduct(productDTO)).thenReturn(productEntity);
            when(productRepository.existsBySku(productDTO.getSku())).thenReturn(false);
            when(productRepository.save(productEntity)).thenReturn(savedProduct);
            when(productMapper.toProductDTO(savedProduct)).thenReturn(productDTO);

            // Act
            ProductDTO result = productService.createProduct(productDTO);

            // Assert
            assertThat(result).isNotNull()
                    .isEqualTo(productDTO);

            verify(productBusiness).validateProductCreation(productDTO);
            verify(productRepository).existsBySku(productDTO.getSku());
            verify(productRepository).save(productEntity);
            verify(productMapper).toProductDTO(savedProduct);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when SKU already exists")
        void shouldThrowExceptionWhenSkuExists() {
            // Arrange
            when(productRepository.existsBySku(productDTO.getSku())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> productService.createProduct(productDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Product already exists with SKU: SKU-RUNNER-PRO-001");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when business validation fails")
        void shouldThrowExceptionWhenBusinessValidationFails() {
            // Arrange
            doThrow(new IllegalArgumentException("Invalid brand")).when(productBusiness).validateProductCreation(productDTO);

            // Act & Assert
            assertThatThrownBy(() -> productService.createProduct(productDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid brand");

            verify(productRepository, never()).existsBySku(anyString());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle repository save exception")
        void shouldHandleRepositoryException() {
            // Arrange
            when(productMapper.toProduct(productDTO)).thenReturn(productEntity);
            when(productRepository.existsBySku(productDTO.getSku())).thenReturn(false);
            when(productRepository.save(productEntity)).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThatThrownBy(() -> productService.createProduct(productDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(productMapper, never()).toProductDTO(any());
        }
    }

    @Nested
    @DisplayName("Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists")
        void shouldReturnProductWhenIdExists() {
            // Arrange
            Long productId = 1L;
            when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
            when(productMapper.toProductDTO(productEntity)).thenReturn(productDTO);

            // Act
            ProductDTO result = productService.getProductById(productId);

            // Assert
            assertThat(result).isNotNull()
                    .isEqualTo(productDTO);
            verify(productRepository).findById(productId);
            verify(productMapper).toProductDTO(productEntity);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ID does not exist")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            // Arrange
            Long productId = 99L;
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not exist with id: " + productId);

            verify(productMapper, never()).toProductDTO(any());
        }
    }

    @Nested
    @DisplayName("Search Products")
    class SearchProductsTests {

        @Test
        @DisplayName("Should return a page of products matching filter")
        void shouldReturnPageOfProducts() {
            // Arrange
            ProductFilterDTO filter = new ProductFilterDTO();
            Pageable pageable = PageRequest.of(0, 10);
            Specification<Product> spec = ProductSpecification.buildSpecification(new ProductFilterDTO());
            Page<Product> productPage = new PageImpl<>(List.of(productEntity), pageable, 1);

            try (MockedStatic<ProductSpecification> specMock = mockStatic(ProductSpecification.class);
                 MockedStatic<BuildPageableHelper> pageableMock = mockStatic(BuildPageableHelper.class)) {

                specMock.when(() -> ProductSpecification.buildSpecification(filter)).thenReturn(spec);
                pageableMock.when(() -> BuildPageableHelper.buildPageableProductFilter(filter)).thenReturn(pageable);

                when(productRepository.findAll(spec, pageable)).thenReturn(productPage);
                when(productMapper.toProductDTO(productEntity)).thenReturn(productDTO);

                // Act
                Page<ProductDTO> result = productService.searchProducts(filter);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent()).containsExactly(productDTO);

                specMock.verify(() -> ProductSpecification.buildSpecification(filter));
                pageableMock.verify(() -> BuildPageableHelper.buildPageableProductFilter(filter));
                verify(productRepository).findAll(spec, pageable);
            }
        }

        @Test
        @DisplayName("Should return an empty page when no products match filter")
        void shouldReturnEmptyPageWhenNoProductsMatch() {
            // Arrange
            ProductFilterDTO filter = new ProductFilterDTO();
            Pageable pageable = PageRequest.of(0, 10);
            Specification<Product> spec = ProductSpecification.buildSpecification(new ProductFilterDTO());
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            try (MockedStatic<ProductSpecification> specMock = mockStatic(ProductSpecification.class);
                 MockedStatic<BuildPageableHelper> pageableMock = mockStatic(BuildPageableHelper.class)) {

                specMock.when(() -> ProductSpecification.buildSpecification(filter)).thenReturn(spec);
                pageableMock.when(() -> BuildPageableHelper.buildPageableProductFilter(filter)).thenReturn(pageable);

                when(productRepository.findAll(spec, pageable)).thenReturn(emptyPage);

                // Act
                Page<ProductDTO> result = productService.searchProducts(filter);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.isEmpty()).isTrue();
                verify(productMapper, never()).toProductDTO(any());
            }
        }
    }

    // --- Helper Methods ---

    private ProductDTO createValidProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setId(1L);
        dto.setSku("SKU-RUNNER-PRO-001");
        dto.setName("Tênis Runner Pro");
        dto.setDescription("Tênis de corrida leve e confortável");
        dto.setPrice(new BigDecimal("399.90"));
        dto.setDiscountPercentage(new BigDecimal("10.00"));
        dto.setStock(42);
        dto.setBrandId(1L);
        dto.setRating(new BigDecimal("4.6"));
        dto.setActive(true);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setUpdatedAt(OffsetDateTime.now());
        return dto;
    }

    private Product createValidProductEntity() {
        Product entity = new Product();
        entity.setId(1L);
        entity.setSku("SKU-RUNNER-PRO-001");
        entity.setName("Tênis Runner Pro");
        entity.setDescription("Tênis de corrida leve e confortável");
        entity.setPrice(new BigDecimal("399.90"));
        entity.setDiscountPercentage(new BigDecimal("10.00"));
        entity.setStock(42);
        entity.setBrandId(1L);
        entity.setRating(new BigDecimal("4.6"));
        entity.setActive(true);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }
}
