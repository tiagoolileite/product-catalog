package br.com.tiagoolileite.productcatalog.mapper;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;
import br.com.tiagoolileite.productcatalog.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE // avoid warnings for unmapped fields
)
public interface ProductMapper {

    ProductDTO toProductDTO(Product product);
    Product toProduct(ProductDTO productDTO);
}
