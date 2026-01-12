package br.com.tiagoolileite.productcatalog.business;

import br.com.tiagoolileite.productcatalog.dto.ProductDTO;

public interface ProductBusiness {
    void validateProductCreation(ProductDTO productDTO);
}
