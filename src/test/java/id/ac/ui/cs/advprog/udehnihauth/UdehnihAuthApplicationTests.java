package id.ac.ui.cs.advprog.udehnihauth;

import id.ac.ui.cs.advprog.udehnihauth.config.CorsConfigTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class UdehnihAuthApplicationTests {

	@Test
	void contextLoads() {

	}

}