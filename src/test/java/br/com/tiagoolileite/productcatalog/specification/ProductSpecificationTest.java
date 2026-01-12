package br.com.tiagoolileite.productcatalog.specification;

import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

    @Mock
    Root<Product> root;

    @Mock
    CriteriaQuery<?> query;

    @Mock
    CriteriaBuilder cb;

    @Mock
    Predicate predicate;

    @Mock
    Expression<String> stringExpression;

    @Mock
    Path<Object> path;

    private ProductFilterDTO filter;

    @BeforeEach
    void setUp() {
        filter = new ProductFilterDTO();

        // stubbings comuns marcados como lenient para não disparar UnnecessaryStubbing
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn(stringExpression);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);

        // BigDecimal (price, discount, rating)
        lenient().when(cb.greaterThanOrEqualTo(any(Expression.class), any(BigDecimal.class))).thenReturn(predicate);
        lenient().when(cb.lessThanOrEqualTo(any(Expression.class), any(BigDecimal.class))).thenReturn(predicate);

        // OffsetDateTime (createdAt)
        lenient().when(cb.greaterThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class))).thenReturn(predicate);
        lenient().when(cb.lessThanOrEqualTo(any(Expression.class), any(OffsetDateTime.class))).thenReturn(predicate);

        // Integer para stock (greaterThan)
        lenient().when(cb.greaterThan(any(Expression.class), any(Integer.class))).thenReturn(predicate);

        lenient().when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
    }

    @Test
    @DisplayName("Retorna predicate mesmo com filtro vazio")
    void shouldBuildSpecificationWithEmptyFilter() {
        Specification<Product> spec = ProductSpecification.buildSpecification(filter);

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNotNull();
        verify(cb).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("Text search gera like em name e description com OR")
    void shouldAddTextSearchPredicate() {
        filter.setQ("Runner");

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("name");
        verify(root).get("description");
        verify(cb, times(2)).lower(any());
        verify(cb, times(2)).like(any(), eq("%runner%"));
        verify(cb).or(any(Predicate.class), any(Predicate.class));
    }

    @Test
    @DisplayName("Filtro por brandId gera equal em brandId")
    void shouldAddBrandPredicate() {
        filter.setBrandId(10L);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("brandId");
        verify(cb).equal(any(), eq(10L));
    }

    @Test
    @DisplayName("Filtro por categoryId gera equal em categoryId")
    void shouldAddCategoryPredicate() {
        filter.setCategoryId(5L);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("categoryId");
        verify(cb).equal(any(), eq(5L));
    }

    @Test
    @DisplayName("priceMin usa greaterThanOrEqualTo em price")
    void shouldAddPriceMinPredicate() {
        filter.setPriceMin(new BigDecimal("100.00"));

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("price");
        verify(cb).greaterThanOrEqualTo(any(), eq(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("priceMax usa lessThanOrEqualTo em price")
    void shouldAddPriceMaxPredicate() {
        filter.setPriceMax(new BigDecimal("500.00"));

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("price");
        verify(cb).lessThanOrEqualTo(any(), eq(new BigDecimal("500.00")));
    }

    @Test
    @DisplayName("discountMin usa greaterThanOrEqualTo em discountPercentage")
    void shouldAddDiscountPredicate() {
        filter.setDiscountMin(new BigDecimal("10.00"));

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("discountPercentage");
        verify(cb).greaterThanOrEqualTo(any(), eq(new BigDecimal("10.00")));
    }

    @Test
    @DisplayName("inStock=true usa greaterThan em stock")
    void shouldAddInStockPredicate() {
        filter.setInStock(true);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("stock");
        verify(cb).greaterThan(any(), eq(0));
    }

    @Test
    @DisplayName("inStock=false usa lessThanOrEqualTo em stock")
    void shouldAddOutOfStockPredicate() {
        filter.setInStock(false);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("stock");
        verify(cb).lessThanOrEqualTo(any(), eq(0));
    }

    @Test
    @DisplayName("active gera equal em active")
    void shouldAddActivePredicate() {
        filter.setActive(true);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("active");
        verify(cb).equal(any(), eq(true));
    }

    @Test
    @DisplayName("ratingMin usa greaterThanOrEqualTo em rating")
    void shouldAddRatingPredicate() {
        filter.setRatingMin(new BigDecimal("4.5"));

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("rating");
        verify(cb).greaterThanOrEqualTo(any(), eq(new BigDecimal("4.5")));
    }

    @Test
    @DisplayName("createdFrom usa greaterThanOrEqualTo em createdAt")
    void shouldAddCreatedFromPredicate() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        filter.setCreatedFrom(from);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("createdAt");
        verify(cb).greaterThanOrEqualTo(any(), eq(from));
    }

    @Test
    @DisplayName("createdTo usa lessThanOrEqualTo em createdAt")
    void shouldAddCreatedToPredicate() {
        OffsetDateTime to = OffsetDateTime.now();
        filter.setCreatedTo(to);

        Specification<Product> spec = ProductSpecification.buildSpecification(filter);
        spec.toPredicate(root, query, cb);

        verify(root).get("createdAt");
        verify(cb).lessThanOrEqualTo(any(), eq(to));
    }
}
