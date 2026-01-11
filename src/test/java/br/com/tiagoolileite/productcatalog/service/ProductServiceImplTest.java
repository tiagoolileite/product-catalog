package br.com.tiagoolileite.productcatalog.service;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import br.com.tiagoolileite.productcatalog.mapper.ProductMapper;
import br.com.tiagoolileite.productcatalog.repository.ProductRepository;
import br.com.tiagoolileite.productcatalog.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldCreateProductSuccessfully() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();
        inputDto.setId(null);
        inputDto.setCreatedAt(null);
        inputDto.setUpdatedAt(null);

        Product productEntity = createValidProductEntity();
        productEntity.setId(null);
        productEntity.setCreatedAt(null);
        productEntity.setUpdatedAt(null);

        Product savedProductEntity = createValidProductEntity();
        savedProductEntity.setId(1L);
        savedProductEntity.setCreatedAt(OffsetDateTime.now());
        savedProductEntity.setUpdatedAt(OffsetDateTime.now());

        ProductDTO responseDto = createValidProductDTO();
        responseDto.setId(1L);
        responseDto.setCreatedAt(OffsetDateTime.now());
        responseDto.setUpdatedAt(OffsetDateTime.now());

        when(productMapper.toProduct(inputDto)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(savedProductEntity);
        when(productMapper.toProductDTO(savedProductEntity)).thenReturn(responseDto);

        // Act
        ProductDTO result = productService.createProduct(inputDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSku()).isEqualTo("SKU-RUNNER-PRO-001");
        assertThat(result.getName()).isEqualTo("Tênis Runner Pro");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("399.90"));
        assertThat(result.getBrandId()).isEqualTo(1L);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(productMapper).toProduct(inputDto);
        verify(productRepository).save(productEntity);
        verify(productMapper).toProductDTO(savedProductEntity);
    }

    @Test
    void shouldCallMapperWithCorrectInputDto() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();
        Product productEntity = createValidProductEntity();
        Product savedProductEntity = createValidProductEntity();
        ProductDTO responseDto = createValidProductDTO();

        when(productMapper.toProduct(any(ProductDTO.class))).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenReturn(savedProductEntity);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(responseDto);

        // Act
        productService.createProduct(inputDto);

        // Assert - usa ArgumentCaptor para capturar e verificar os argumentos
        ArgumentCaptor<ProductDTO> dtoCaptor = ArgumentCaptor.forClass(ProductDTO.class);
        verify(productMapper).toProduct(dtoCaptor.capture());

        ProductDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getSku()).isEqualTo("SKU-RUNNER-PRO-001");
        assertThat(capturedDto.getName()).isEqualTo("Tênis Runner Pro");
        assertThat(capturedDto.getPrice()).isEqualByComparingTo(new BigDecimal("399.90"));
        assertThat(capturedDto.getBrandId()).isEqualTo(1L);
        assertThat(capturedDto.getActive()).isTrue();
    }


    @Test
    void shouldCallRepositoryWithCorrectProductEntity() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();
        Product productEntity = createValidProductEntity();
        Product savedProductEntity = createValidProductEntity();
        ProductDTO responseDto = createValidProductDTO();

        when(productMapper.toProduct(any(ProductDTO.class))).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenReturn(savedProductEntity);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(responseDto);

        // Act
        productService.createProduct(inputDto);

        // Assert - verifica se o repository foi chamado com a entity correta
        verify(productRepository).save(argThat(entity ->
                entity.getSku().equals("SKU-RUNNER-PRO-001") &&
                        entity.getName().equals("Tênis Runner Pro") &&
                        entity.getPrice().compareTo(new BigDecimal("399.90")) == 0 &&
                        entity.getBrandId().equals(1L) &&
                        entity.getActive().equals(true)
        ));
    }

    @Test
    void shouldCallMapperToConvertSavedEntityToDto() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();
        Product productEntity = createValidProductEntity();
        Product savedProductEntity = createValidProductEntity();
        savedProductEntity.setId(1L);
        ProductDTO responseDto = createValidProductDTO();

        when(productMapper.toProduct(any(ProductDTO.class))).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenReturn(savedProductEntity);
        when(productMapper.toProductDTO(any(Product.class))).thenReturn(responseDto);

        // Act
        productService.createProduct(inputDto);

        // Assert - verifica se o mapper foi chamado para converter a entity salva
        verify(productMapper).toProductDTO(argThat(entity ->
                entity.getId().equals(1L) &&
                        entity.getSku().equals("SKU-RUNNER-PRO-001") &&
                        entity.getName().equals("Tênis Runner Pro")
        ));
    }

    @Test
    void shouldHandleRepositoryException() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();
        Product productEntity = createValidProductEntity();

        when(productMapper.toProduct(any(ProductDTO.class))).thenReturn(productEntity);
        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                productService.createProduct(inputDto));

        verify(productMapper).toProduct(inputDto);
        verify(productRepository).save(productEntity);
        verify(productMapper, never()).toProductDTO(any());
    }

    @Test
    void shouldHandleMapperException() {
        // Arrange
        ProductDTO inputDto = createValidProductDTO();

        when(productMapper.toProduct(any(ProductDTO.class)))
                .thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                productService.createProduct(inputDto));

        verify(productMapper).toProduct(inputDto);
        verify(productRepository, never()).save(any());
        verify(productMapper, never()).toProductDTO(any());
    }

    private ProductDTO createValidProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setSku("SKU-RUNNER-PRO-001");
        dto.setName("Tênis Runner Pro");
        dto.setDescription("Tênis de corrida leve e confortável");
        dto.setPrice(new BigDecimal("399.90"));
        dto.setDiscountPercentage(new BigDecimal("10.00"));
        dto.setStock(42);
        dto.setBrandId(1L);
        dto.setRating(new BigDecimal("4.6"));
        dto.setActive(true);
        return dto;
    }

    private Product createValidProductEntity() {
        Product entity = new Product();
        entity.setSku("SKU-RUNNER-PRO-001");
        entity.setName("Tênis Runner Pro");
        entity.setDescription("Tênis de corrida leve e confortável");
        entity.setPrice(new BigDecimal("399.90"));
        entity.setDiscountPercentage(new BigDecimal("10.00"));
        entity.setStock(42);
        entity.setBrandId(1L);
        entity.setRating(new BigDecimal("4.6"));
        entity.setActive(true);
        return entity;
    }
}
