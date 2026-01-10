package br.com.tiagoolileite.productcatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
class ProductCatalogApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testMain(CapturedOutput output) {
		String[] args = {"--spring.main.web-application-type=none"};
		// ensure main does not throw and capture startup logs
		assertDoesNotThrow(() -> ProductCatalogApplication.main(args));

		assertThat(output.getOut())
			.contains("Application starting - entering main")
			.contains("Started ProductCatalogApplication");
	}
}
