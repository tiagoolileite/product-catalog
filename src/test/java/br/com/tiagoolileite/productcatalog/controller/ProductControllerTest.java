package br.com.tiagoolileite.productcatalog.controller;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.exception.ResourceNotFoundException;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // para suporte ao OffsetDateTime
    }

    @Nested
    @DisplayName("POST /api/products - Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() throws Exception {
            // Arrange
            ProductDTO inputDto = createValidProductDTO();
            inputDto.setId(null);
            inputDto.setCreatedAt(null);
            inputDto.setUpdatedAt(null);

            ProductDTO responseDto = createValidProductDTO();
            responseDto.setId(1L);
            responseDto.setCreatedAt(OffsetDateTime.now());
            responseDto.setUpdatedAt(OffsetDateTime.now());

            when(productService.createProduct(any(ProductDTO.class))).thenReturn(responseDto);

            // Act & Assert
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.sku").value("SKU-RUNNER-PRO-001"))
                    .andExpect(jsonPath("$.name").value("Tênis Runner Pro"))
                    .andExpect(jsonPath("$.price").value(399.90))
                    .andExpect(jsonPath("$.discountPercentage").value(10.00))
                    .andExpect(jsonPath("$.stock").value(42))
                    .andExpect(jsonPath("$.brandId").value(1L))
                    .andExpect(jsonPath("$.rating").value(4.6))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(productService).createProduct(any(ProductDTO.class));
        }

        @Test
        @DisplayName("Should call service with correct parameters")
        void shouldCallServiceWithCorrectParameters() throws Exception {
            // Arrange
            ProductDTO inputDto = createValidProductDTO();
            ProductDTO responseDto = createValidProductDTO();
            responseDto.setId(1L);

            when(productService.createProduct(any(ProductDTO.class))).thenReturn(responseDto);

            // Act
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputDto)))
                    .andExpect(status().isCreated());

            // Assert
            verify(productService).createProduct(argThat(dto ->
                    dto.getSku().equals("SKU-RUNNER-PRO-001") &&
                            dto.getName().equals("Tênis Runner Pro") &&
                            dto.getPrice().compareTo(new BigDecimal("399.90")) == 0 &&
                            dto.getDiscountPercentage().compareTo(new BigDecimal("10.00")) == 0 &&
                            dto.getStock().equals(42) &&
                            dto.getBrandId().equals(1L) &&
                            dto.getRating().compareTo(new BigDecimal("4.6")) == 0 &&
                            dto.getActive().equals(true)
            ));
        }

        @Test
        @DisplayName("Should return bad request when JSON is malformed")
        void shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("Should return unsupported media type when content type is not JSON")
        void shouldReturnUnsupportedMediaTypeWhenContentTypeIsNotJson() throws Exception {
            // Arrange
            ProductDTO validDto = createValidProductDTO();

            // Act & Assert
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_XML)
                            .content(objectMapper.writeValueAsString(validDto)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());

            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("Should return bad request when required fields are missing")
        void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
            // Arrange
            ProductDTO invalidDto = new ProductDTO();
            // Missing required fields like sku, name, etc.

            // Act & Assert
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).createProduct(any());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{productId} - Get Product By ID")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when ID exists")
        void shouldReturnProductWhenIdExists() throws Exception {
            // Arrange
            Long productId = 1L;
            ProductDTO productDto = createValidProductDTO();
            productDto.setId(productId);

            when(productService.getProductById(productId)).thenReturn(productDto);

            // Act & Assert
            mockMvc.perform(get("/api/products/{productId}", productId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(productId))
                    .andExpect(jsonPath("$.sku").value("SKU-RUNNER-PRO-001"))
                    .andExpect(jsonPath("$.name").value("Tênis Runner Pro"))
                    .andExpect(jsonPath("$.price").value(399.90))
                    .andExpect(jsonPath("$.brandId").value(1L));

            verify(productService).getProductById(productId);
        }

        @Test
        @DisplayName("Should return 404 when product ID does not exist")
        void shouldReturn404WhenProductIdDoesNotExist() throws Exception {
            // Arrange
            Long nonExistentId = 999L;
            when(productService.getProductById(nonExistentId))
                    .thenThrow(new ResourceNotFoundException("Product"));

            // Act & Assert
            mockMvc.perform(get("/api/products/{productId}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(productService).getProductById(nonExistentId);
        }

        @Test
        @DisplayName("Should return bad request for invalid ID format")
        void shouldReturnBadRequestForInvalidIdFormat() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/products/{productId}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).getProductById(any());
        }
    }

    @Nested
    @DisplayName("GET /api/products - Search Products")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search products with default parameters")
        void shouldSearchProductsWithDefaultParameters() throws Exception {
            // Arrange
            List<ProductDTO> productList = List.of(
                    createValidProductDTO(),
                    createAnotherValidProductDTO()
            );
            Page<ProductDTO> productsPage = new PageImpl<>(productList, PageRequest.of(0, 20), 2);

            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(productsPage);

            // Act & Assert
            mockMvc.perform(get("/api/products"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.content[0].sku").value("SKU-RUNNER-PRO-001"))
                    .andExpect(jsonPath("$.content[1].sku").value("SKU-WALKER-BASIC-002"));

            verify(productService).searchProducts(argThat(filter ->
                    filter.getSort().equals("createdAt,desc") &&
                            filter.getPage().equals(0) &&
                            filter.getSize().equals(20) &&
                            filter.getQ() == null &&
                            filter.getBrandId() == null &&
                            filter.getCategoryId() == null
            ));
        }

        @Test
        @DisplayName("Should search products with all filter parameters")
        void shouldSearchProductsWithAllFilterParameters() throws Exception {
            // Arrange
            List<ProductDTO> productList = List.of(createValidProductDTO());
            Page<ProductDTO> productsPage = new PageImpl<>(productList, PageRequest.of(1, 10), 1);

            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(productsPage);

            OffsetDateTime createdFrom = OffsetDateTime.now().minusDays(7);
            OffsetDateTime createdTo = OffsetDateTime.now();

            // Act & Assert
            mockMvc.perform(get("/api/products")
                            .param("q", "runner")
                            .param("categoryId", "2")
                            .param("brandId", "1")
                            .param("priceMin", "100.00")
                            .param("priceMax", "500.00")
                            .param("discountMin", "5.00")
                            .param("inStock", "true")
                            .param("active", "true")
                            .param("ratingMin", "4.0")
                            .param("createdFrom", createdFrom.toString())
                            .param("createdTo", createdTo.toString())
                            .param("attrs", "id,sku,name")
                            .param("sort", "price,asc")
                            .param("page", "1")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.size").value(10));

            verify(productService).searchProducts(argThat(filter ->
                    filter.getQ().equals("runner") &&
                            filter.getBrandId().equals(1L) &&
                            filter.getCategoryId().equals(2L) &&
                            filter.getPriceMin().compareTo(new BigDecimal("100.00")) == 0 &&
                            filter.getPriceMax().compareTo(new BigDecimal("500.00")) == 0 &&
                            filter.getDiscountMin().compareTo(new BigDecimal("5.00")) == 0 &&
                            filter.getInStock().equals(true) &&
                            filter.getActive().equals(true) &&
                            filter.getRatingMin().compareTo(new BigDecimal("4.0")) == 0 &&
                            filter.getCreatedFrom() != null &&
                            filter.getCreatedTo() != null &&
                            filter.getAttrs().equals(List.of("id", "sku", "name")) &&
                            filter.getSort().equals("price,asc") &&
                            filter.getPage().equals(1) &&
                            filter.getSize().equals(10)
            ));
        }

        @Test
        @DisplayName("Should search products with text query only")
        void shouldSearchProductsWithTextQueryOnly() throws Exception {
            // Arrange
            List<ProductDTO> productList = List.of(createValidProductDTO());
            Page<ProductDTO> productsPage = new PageImpl<>(productList, PageRequest.of(0, 20), 1);

            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(productsPage);

            // Act & Assert
            mockMvc.perform(get("/api/products")
                            .param("q", "tênis runner"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));

            verify(productService).searchProducts(argThat(filter ->
                    filter.getQ().equals("tênis runner") &&
                            filter.getSort().equals("createdAt,desc") &&
                            filter.getPage().equals(0) &&
                            filter.getSize().equals(20)
            ));
        }

        @Test
        @DisplayName("Should search products with brand and category filters")
        void shouldSearchProductsWithBrandAndCategoryFilters() throws Exception {
            // Arrange
            Page<ProductDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/products")
                            .param("brandId", "5")
                            .param("categoryId", "3"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(productService).searchProducts(argThat(filter ->
                    filter.getBrandId().equals(5L) &&
                            filter.getCategoryId().equals(3L)
            ));
        }

        @Test
        @DisplayName("Should search products with price range")
        void shouldSearchProductsWithPriceRange() throws Exception {
            // Arrange
            List<ProductDTO> productList = List.of(createValidProductDTO());
            Page<ProductDTO> productsPage = new PageImpl<>(productList, PageRequest.of(0, 20), 1);

            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(productsPage);

            // Act & Assert
            mockMvc.perform(get("/api/products")
                            .param("priceMin", "200.00")
                            .param("priceMax", "600.00"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService).searchProducts(argThat(filter ->
                    filter.getPriceMin().compareTo(new BigDecimal("200.00")) == 0 &&
                            filter.getPriceMax().compareTo(new BigDecimal("600.00")) == 0
            ));
        }

        @Test
        @DisplayName("Should handle custom pagination")
        void shouldHandleCustomPagination() throws Exception {
            // Arrange
            List<ProductDTO> productList = List.of(createValidProductDTO());
            Page<ProductDTO> productsPage = new PageImpl<>(productList, PageRequest.of(2, 5), 11);

            when(productService.searchProducts(any(ProductFilterDTO.class))).thenReturn(productsPage);

            // Act & Assert
            mockMvc.perform(get("/api/products")
                            .param("page", "2")
                            .param("size", "5")
                            .param("sort", "name,asc"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value(2))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.totalElements").value(11))
                    .andExpect(jsonPath("$.totalPages").value(3));

            verify(productService).searchProducts(argThat(filter ->
                    filter.getPage().equals(2) &&
                            filter.getSize().equals(5) &&
                            filter.getSort().equals("name,asc")
            ));
        }

        @Test
        @DisplayName("Should handle invalid parameter types gracefully")
        void shouldHandleInvalidParameterTypesGracefully() throws Exception {
            // Act & Assert - Spring should handle type conversion errors
            mockMvc.perform(get("/api/products")
                            .param("brandId", "invalid-number")
                            .param("priceMin", "not-a-decimal"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(productService, never()).searchProducts(any());
        }
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

    private ProductDTO createAnotherValidProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setSku("SKU-WALKER-BASIC-002");
        dto.setName("Tênis Walker Basic");
        dto.setDescription("Tênis para caminhada básico");
        dto.setPrice(new BigDecimal("199.90"));
        dto.setDiscountPercentage(new BigDecimal("5.00"));
        dto.setStock(25);
        dto.setBrandId(2L);
        dto.setRating(new BigDecimal("4.2"));
        dto.setActive(true);
        return dto;
    }
}
