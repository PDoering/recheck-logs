package de.retest.recheck.logs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import de.retest.ui.descriptors.RootElement;

public class LogFileAdapterTest {

	private static final File resources = new File("src/test/resources");

	@Test
	public void should_be_able_to_parse_logfile() {
		RootElement root = new LogFileAdapter().convert(new File(resources, "logback-default-layout.log")).iterator().next();
		assertThat(root.getContainedElements().size()).isEqualTo(19);
	}

}
