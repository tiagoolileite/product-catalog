    package br.com.tiagoolileite.productcatalog.helper;

    import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Nested;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.params.ParameterizedTest;
    import org.junit.jupiter.params.provider.MethodSource;
    import org.junit.jupiter.params.provider.ValueSource;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;

    import java.util.stream.Stream;

    import static org.assertj.core.api.Assertions.assertThat;

    @DisplayName("BuildPageableHelper Tests")
    class BuildPageableHelperTest {

        private final Sort defaultSort = Sort.by(Sort.Direction.DESC, "createdAt");

        @Nested
        @DisplayName("Cenários com Sort Padrão (Default Sort)")
        class DefaultSortScenarios {

            @Test
            @DisplayName("Aplica sort padrão quando filter DTO é vazio")
            void shouldApplyDefaultSortWhenFilterIsEmpty() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getSort()).isEqualTo(defaultSort);
            }

            @ParameterizedTest(name = "Aplica sort padrão quando sort = \"{0}\"")
            @MethodSource("invalidSortValues")
            @DisplayName("Aplica sort padrão para valores de sort inválidos")
            void shouldApplyDefaultSortForInvalidSortValues(String sort) {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setSort(sort);

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getSort()).isEqualTo(defaultSort);
            }

            static Stream<String> invalidSortValues() {
                return Stream.of(
                        null,                 // sort is null
                        " ",                  // blank
                        "invalidField,asc",   // field not allowed
                        "name"                // invalid format (no comma)
                );
            }

        }

        @Nested
        @DisplayName("Cenários com Sort Específico")
        class SpecificSortScenarios {

            @Test
            @DisplayName("Usa sort específico 'name,asc' quando fornecido")
            void shouldUseSpecificSortWhenProvided() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setSort("name,asc");

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "name"));
            }

            @Test
            @DisplayName("Usa sort específico 'price,desc' quando fornecido")
            void shouldUseSpecificSortDescending() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setSort("price,desc");

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "price"));
            }

            @Test
            @DisplayName("Assume ASC se a direção do sort for inválida mas o campo for válido")
            void shouldAssumeAscWhenDirectionIsInvalid() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setSort("stock,any_direction");

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "stock"));
            }
        }

        @Nested
        @DisplayName("Validação de Paginação (Page e Size)")
        class PaginationValidationTests {

            @Test
            @DisplayName("Retorna valores padrão de page e size quando filter é vazio")
            void shouldReturnDefaultPageAndSizeWhenFilterIsEmpty() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getPageNumber()).isZero();
                assertThat(result.getPageSize()).isEqualTo(20);
            }

            @ParameterizedTest
            @ValueSource(ints = {-10, -1})
            @DisplayName("Retorna página 0 quando page é negativo")
            void shouldReturnDefaultPageWhenNegative(int invalidPage) {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setPage(invalidPage);

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getPageNumber()).isZero();
            }

            @ParameterizedTest
            @ValueSource(ints = {0, -1, 101, 200})
            @DisplayName("Retorna size 20 quando size está fora do range (1-100)")
            void shouldReturnDefaultSizeWhenOutOfRange(int invalidSize) {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setSize(invalidSize);

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getPageSize()).isEqualTo(20);
            }

            @Test
            @DisplayName("Usa valores corretos de page e size quando são válidos")
            void shouldUseProvidedPageAndSizeWhenValid() {
                // Given
                ProductFilterDTO filter = new ProductFilterDTO();
                filter.setPage(5);
                filter.setSize(50);

                // When
                Pageable result = BuildPageableHelper.buildPageableProductFilter(filter);

                // Then
                assertThat(result.getPageNumber()).isEqualTo(5);
                assertThat(result.getPageSize()).isEqualTo(50);
            }
        }
    }
