package de.retest.recheck.logs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.retest.ui.descriptors.Element;

public class LogLineParser {

	private final Pattern pattern;

	public LogLineParser(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	public Element parseLine(String line) {
		if ((line == null) || line.isEmpty()) {
			throw new IllegalArgumentException("Line cannot be null or empty!");
		}
		if (line.startsWith("at ")) {
			throw new IllegalArgumentException("Parsing log file should work such that all lines that belong together are delivered together, i.e. stack trace.");
		}
		Matcher matcher = pattern.matcher(line);
		matcher.find();
		for (int groupIdx = 1; groupIdx <= matcher.groupCount(); groupIdx++) {
			String group = matcher.group(groupIdx);
			System.out.println("Group " + groupIdx + ": " + group);
		}
		throw new RuntimeException("not implemented");
	}

}
