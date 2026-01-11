package br.com.tiagoolileite.productcatalog.repository;

import br.com.tiagoolileite.productcatalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
