package br.com.tiagoolileite.productcatalog.helper;

import br.com.tiagoolileite.productcatalog.dto.ProductFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class BuildPageableHelper {

    private BuildPageableHelper() {}

    private static final Logger log = LoggerFactory.getLogger(BuildPageableHelper.class);

    /**
     * Constants for pagination and sorting configuration
     */
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    // NOVO: Default sort order
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "sku", "name", "price", "discountPercentage",
            "stock", "rating", "active", "createdAt", "updatedAt"
    );

    /**
     * Builds a Pageable object from the provided filter DTO, handling pagination and sorting parameters.
     *
     * @param productFilterDTO The filter DTO containing pagination and sorting parameters
     * @return A Pageable object configured with page number, size, and sorting criteria
     */
    public static Pageable buildPageableProductFilter(ProductFilterDTO productFilterDTO) {
        Sort sort = parseSort(productFilterDTO.getSort());
        int size = validateAndGetSize(productFilterDTO.getSize());
        int page = validateAndGetPage(productFilterDTO.getPage());

        // ALTERAÇÃO: Se o sort for 'unsorted', aplica o default
        if (sort.isUnsorted()) {
            sort = DEFAULT_SORT;
        }

        return PageRequest.of(page, size, sort);
    }

    /**
     * Parses the sort parameter and creates a Sort object.
     * Expected format: "fieldName,direction" (e.g., "price,desc", "name,asc")
     * Includes security validation to prevent SQL injection through field names.
     *
     * @param sortParam The sort parameter string
     * @return Sort object or Sort.unsorted() if invalid/empty
     */
    private static Sort parseSort(String sortParam) {
        if (isBlank(sortParam)) {
            return Sort.unsorted();
        }

        String[] sortParts = sortParam.split(",");
        if (sortParts.length < 2) {
            log.warn("Invalid sort format: {}. Expected 'field,direction'", sortParam);
            return Sort.unsorted();
        }

        String field = sortParts[0].trim();
        String direction = sortParts[1].trim().toLowerCase();

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            log.warn("Invalid sort field: {}. Allowed fields: {}", field, ALLOWED_SORT_FIELDS);
            return Sort.unsorted();
        }

        return "desc".equals(direction)
                ? Sort.by(Sort.Direction.DESC, field)
                : Sort.by(Sort.Direction.ASC, field);
    }

    /**
     * Validates and returns the page size, applying business constraints.
     *
     * @param requestedSize The requested page size
     * @return Validated page size (between MIN_PAGE_SIZE-MAX_PAGE_SIZE)
     */
    private static int validateAndGetSize(Integer requestedSize) {
        if (requestedSize == null || requestedSize < MIN_PAGE_SIZE || requestedSize > MAX_PAGE_SIZE) {
            return DEFAULT_PAGE_SIZE;
        }
        return requestedSize;
    }

    /**
     * Validates and returns the page number, ensuring it's not negative.
     *
     * @param requestedPage The requested page number
     * @return Validated page number (0 or greater)
     */
    private static int validateAndGetPage(Integer requestedPage) {
        if (requestedPage == null || requestedPage < 0) {
            return DEFAULT_PAGE_NUMBER;
        }
        return requestedPage;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
