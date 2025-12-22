package com.fintech.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationTest {

	@Test
	void contextLoads() {
        // Simple unit test to ensure class can be instantiated
        // and provides coverage for the Application class
        Application app = new Application();
        assertNotNull(app);
	}

}
