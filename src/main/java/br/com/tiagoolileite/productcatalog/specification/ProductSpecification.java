package br.com.tiagoolileite.productcatalog.specification;

import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for building JPA Specifications to filter Product entities.
 * Uses the Criteria API to construct dynamic queries based on filter parameters.
 */
public class ProductSpecification {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ProductSpecification() {
    }

    /**
     * Builds a JPA Specification for filtering Product entities based on the provided filter criteria.
     * Each filter parameter in the ProductFilterDTO is converted to a JPA predicate if present.
     * All predicates are combined with AND logic to create the final query condition.
     *
     * @param productFilterDTO The filter DTO containing various search and filter criteria
     * @return A JPA Specification that can be used with Spring Data JPA repositories
     */
    public static Specification<Product> buildSpecification(ProductFilterDTO productFilterDTO) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Apply all filter predicates
            addTextSearchPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addBrandPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addCategoryPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addPriceRangePredicates(predicates, root, criteriaBuilder, productFilterDTO);
            addDiscountPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addStockPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addActivePredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addRatingPredicate(predicates, root, criteriaBuilder, productFilterDTO);
            addDateRangePredicates(predicates, root, criteriaBuilder, productFilterDTO);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Adds text search predicate for searching in both product name and description (case-insensitive).
     * Uses OR logic to match either name OR description containing the search text.
     */
    private static void addTextSearchPredicate(List<Predicate> predicates, Root<Product> root,
                                               CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getQ())
                .filter(q -> !q.trim().isEmpty())
                .ifPresent(searchText -> {
                    String searchPattern = "%" + searchText.toLowerCase() + "%";
                    Predicate namePredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), searchPattern
                    );
                    Predicate descriptionPredicate = criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")), searchPattern
                    );
                    predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
                });
    }

    /**
     * Adds brand filter predicate for exact match on brand ID.
     */
    private static void addBrandPredicate(List<Predicate> predicates, Root<Product> root,
                                          CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getBrandId())
                .ifPresent(brandId ->
                        predicates.add(criteriaBuilder.equal(root.get("brandId"), brandId))
                );
    }

    /**
     * Adds category filter predicate for exact match on category ID.
     */
    private static void addCategoryPredicate(List<Predicate> predicates, Root<Product> root,
                                             CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getCategoryId())
                .ifPresent(categoryId ->
                        predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId))
                );
    }

    /**
     * Adds price range predicates for minimum and maximum price filtering.
     */
    private static void addPriceRangePredicates(List<Predicate> predicates, Root<Product> root,
                                                CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        // Minimum price filter
        Optional.ofNullable(filterDTO.getPriceMin())
                .ifPresent(priceMin ->
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceMin))
                );

        // Maximum price filter
        Optional.ofNullable(filterDTO.getPriceMax())
                .ifPresent(priceMax ->
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceMax))
                );
    }

    /**
     * Adds discount predicate for filtering products with minimum discount percentage.
     */
    private static void addDiscountPredicate(List<Predicate> predicates, Root<Product> root,
                                             CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getDiscountMin())
                .ifPresent(discountMin ->
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercentage"), discountMin))
                );
    }

    /**
     * Adds stock availability predicate based on stock quantity.
     * True filters for products in stock (stock > 0), false for out of stock (stock <= 0).
     */
    private static void addStockPredicate(List<Predicate> predicates, Root<Product> root,
                                          CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getInStock())
                .ifPresent(inStock -> {
                    if (inStock) {
                        predicates.add(criteriaBuilder.greaterThan(root.get("stock"), 0));
                    } else {
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stock"), 0));
                    }
                });
    }

    /**
     * Adds active status predicate for filtering products by active/inactive status.
     */
    private static void addActivePredicate(List<Predicate> predicates, Root<Product> root,
                                           CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getActive())
                .ifPresent(active ->
                        predicates.add(criteriaBuilder.equal(root.get("active"), active))
                );
    }

    /**
     * Adds rating predicate for filtering products with minimum rating.
     */
    private static void addRatingPredicate(List<Predicate> predicates, Root<Product> root,
                                           CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        Optional.ofNullable(filterDTO.getRatingMin())
                .ifPresent(ratingMin ->
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), ratingMin))
                );
    }

    /**
     * Adds date range predicates for filtering products by creation date range.
     */
    private static void addDateRangePredicates(List<Predicate> predicates, Root<Product> root,
                                               CriteriaBuilder criteriaBuilder, ProductFilterDTO filterDTO) {
        // Creation date range filter - start date
        Optional.ofNullable(filterDTO.getCreatedFrom())
                .ifPresent(createdFrom ->
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom))
                );

        // Creation date range filter - end date
        Optional.ofNullable(filterDTO.getCreatedTo())
                .ifPresent(createdTo ->
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo))
                );
    }
}
