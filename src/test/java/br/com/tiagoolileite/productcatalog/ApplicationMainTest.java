package br.com.tiagoolileite.productcatalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(OutputCaptureExtension.class)
class ApplicationMainTest {

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
