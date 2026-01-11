package br.com.tiagoolileite.productcatalog.controller;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
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
                .andExpect(jsonPath("$.brandId").value(1L))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
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
                        dto.getBrandId().equals(1L) &&
                        dto.getActive().equals(true)
        ));
    }

    @Test
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
}
