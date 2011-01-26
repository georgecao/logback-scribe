package de.bitzeche.logback.scribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ScribeAppenderTest {

	private Logger logger = LoggerFactory.getLogger("test");

	@Test
	public void appendTest() {
		logger.debug("test");
	}
}
