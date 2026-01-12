package br.com.tiagoolileite.productcatalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductCatalogApplication {

	private static final Logger log = LoggerFactory.getLogger(ProductCatalogApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProductCatalogApplication.class, args);

		log.info("Application starting - entering main");
	}

}
