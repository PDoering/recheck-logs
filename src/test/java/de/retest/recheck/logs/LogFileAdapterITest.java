package de.retest.recheck.logs;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.retest.recheck.Recheck;
import de.retest.recheck.RecheckImpl;

public class LogFileAdapterITest {

	private Recheck re;

	@Before
	public void setUp() {
		System.setProperty(LogFileParser.CONFIGURED_PATTERN_CONFIG_KEY, "(?<dateTime>[-:\\d ]+) (?<level>[\\w]+) \\[(?<thread>.+)\\] (?<logger>[\\w\\.]+) - (?<message>.*)");
		re = new RecheckImpl();
	}

	@Test
	public void check_example_log() throws Exception {
		re.startTest( "example" );
		re.check( new File("src/test/resources/example.log"), "open" );
		re.capTest();
	}

	@After
	public void tearDown() {
		re.cap();
	}
}
