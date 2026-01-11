package br.com.tiagoolileite.productcatalog.exception.handler;

import br.com.tiagoolileite.productcatalog.dto.ErrorResponseDTO;
import br.com.tiagoolileite.productcatalog.exception.DuplicateResourceException;
import br.com.tiagoolileite.productcatalog.exception.ResourceNotFoundException;
import br.com.tiagoolileite.productcatalog.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpInputMessage httpInputMessage;

    @BeforeEach
    void setUp() {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/products");
    }

    @Nested
    @DisplayName("DuplicateResourceException")
    class DuplicateResourceExceptionTests {

        @Test
        @DisplayName("Retorna 409 Conflict para recurso duplicado")
        void shouldReturnConflictForDuplicateResource() {
            // Given
            String resourceName = "Product";
            String fieldName = "sku";
            String fieldValue = "ABC-123";
            DuplicateResourceException exception = new DuplicateResourceException(resourceName, fieldName, fieldValue);

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleDuplicateResourceException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getError()).isEqualTo("Resource Conflict");
            assertThat(response.getBody().getMessage()).contains("Product already exists with sku: ABC-123");
            assertThat(response.getBody().getPath()).isEqualTo("/api/products");
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Retorna 404 Not Found para recurso não encontrado")
        void shouldReturnNotFoundForResourceNotFound() {
            // Given
            String message = "Product not found with id: 123";
            ResourceNotFoundException exception = new ResourceNotFoundException(message);

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleResourceNotFoundException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
            assertThat(response.getBody().getError()).isEqualTo("Resource Not Found");
            assertThat(response.getBody().getMessage()).isEqualTo(message);
            assertThat(response.getBody().getPath()).isEqualTo("/api/products");
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ValidationException")
    class ValidationExceptionTests {

        @Test
        @DisplayName("Retorna 400 Bad Request para erro de validação customizado")
        void shouldReturnBadRequestForCustomValidationError() {
            // Given
            String message = "Invalid price value";
            ValidationException exception = new ValidationException(message);

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleCustomValidationException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Validation Error");
            assertThat(response.getBody().getMessage()).isEqualTo(message);
            assertThat(response.getBody().getPath()).isEqualTo("/api/products");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class MethodArgumentNotValidExceptionTests {

        @Test
        @DisplayName("Retorna 400 Bad Request com detalhes dos campos inválidos")
        void shouldReturnBadRequestWithValidationDetails() {
            // Given
            FieldError fieldError1 = new FieldError("productDTO", "name", null, false, null, null, "Name is required");
            FieldError fieldError2 = new FieldError("productDTO", "price", null, false, null, null, "Price is required");

            when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleValidationException(methodArgumentNotValidException, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid input parameters");
            assertThat(response.getBody().getValidationErrors()).hasSize(2);
            assertThat(response.getBody().getValidationErrors().get(0).getField()).isEqualTo("name");
            assertThat(response.getBody().getValidationErrors().get(1).getField()).isEqualTo("price");
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException")
    class DataIntegrityViolationExceptionTests {

        @Test
        @DisplayName("Retorna 409 Conflict para violação de integridade com unique constraint")
        void shouldReturnConflictForUniqueConstraintViolation() {
            // Given
            DataIntegrityViolationException exception = new DataIntegrityViolationException(
                    "could not execute statement; SQL [n/a]; constraint [uk_product_sku]; nested exception unique constraint");

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleDataIntegrityViolation(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getError()).isEqualTo("Data Integrity Violation");
            assertThat(response.getBody().getMessage()).isEqualTo("A resource with this information already exists");
        }

        @Test
        @DisplayName("Retorna 409 Conflict para violação de integridade genérica")
        void shouldReturnConflictForGenericDataIntegrityViolation() {
            // Given
            DataIntegrityViolationException exception = new DataIntegrityViolationException("Generic constraint violation");

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleDataIntegrityViolation(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(409);
            assertThat(response.getBody().getError()).isEqualTo("Data Integrity Violation");
            assertThat(response.getBody().getMessage()).isEqualTo("Data integrity violation");
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException")
    class HttpMessageNotReadableExceptionTests {

        @Test
        @DisplayName("Retorna 400 Bad Request para JSON malformado")
        void shouldReturnBadRequestForMalformedJson() {
            // Given
            HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON", httpInputMessage);

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleHttpMessageNotReadable(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("Malformed JSON");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid JSON format in request body");
        }
    }

    @Nested
    @DisplayName("HttpMediaTypeNotSupportedException")
    class HttpMediaTypeNotSupportedExceptionTests {

        @Test
        @DisplayName("Retorna 415 Unsupported Media Type para Content-Type não suportado")
        void shouldReturnUnsupportedMediaType() {
            // Given
            HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException(
                    MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON)
            );

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleHttpMediaTypeNotSupported(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(415);
            assertThat(response.getBody().getError()).isEqualTo("Unsupported Media Type");
            assertThat(response.getBody().getMessage()).contains("Content type 'application/xml' is not supported");
            assertThat(response.getBody().getMessage()).contains("Supported media types are: [application/json]");
        }
    }

    @Nested
    @DisplayName("HttpMediaTypeNotAcceptableException")
    class HttpMediaTypeNotAcceptableExceptionTests {

        @Test
        @DisplayName("Retorna 406 Not Acceptable para Accept header não aceitável")
        void shouldReturnNotAcceptable() {
            // Given
            HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException(
                    List.of(MediaType.APPLICATION_JSON)
            );

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleHttpMediaTypeNotAcceptable(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(406);
            assertThat(response.getBody().getError()).isEqualTo("Media Type Not Acceptable");
            assertThat(response.getBody().getMessage()).contains("Media type not acceptable.");
            assertThat(response.getBody().getMessage()).contains("Supported media types are: [application/json]");
        }
    }
    // ====================================================

    @Nested
    @DisplayName("Generic Exception")
    class GenericExceptionTests {

        @Test
        @DisplayName("Retorna 500 Internal Server Error para exceção genérica")
        void shouldReturnInternalServerErrorForGenericException() {
            // Given
            Exception exception = new RuntimeException("Unexpected error");

            // When
            ResponseEntity<ErrorResponseDTO> response = globalExceptionHandler
                    .handleGenericException(exception, httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
            assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        }
    }
}
