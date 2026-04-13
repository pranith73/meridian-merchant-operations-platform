package com.pranith73.meridian;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic application boot test.
 *
 * Why this exists:
 * - Confirms the Spring Boot application context starts successfully.
 * - Uses the "test" profile so test runtime is controlled and predictable.
 * - Prevents tests from silently depending on local machine settings.
 */
@SpringBootTest
@ActiveProfiles("test")
class MeridianApplicationTests {

	@Test
	void contextLoads() {
		// If the application context starts successfully, this test passes.
	}
}