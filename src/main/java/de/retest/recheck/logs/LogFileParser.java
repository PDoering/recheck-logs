package de.retest.recheck.logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.retest.ui.descriptors.Element;
import de.retest.ui.descriptors.RootElement;

public class LogFileParser {

	public static final String CONFIGURED_PATTERN_CONFIG_KEY = "de.retest.recheck.logs.pattern";
	public static final String PATTERN_DEFAULT = "([-:\\d ]+) ([\\w]+) \\[([\\w]+)\\] ([\\w\\.]+) - (.*)";

	public RootElement parseLogFile(File logFile) {
		List<Element> lines = new ArrayList<>();
		StringBuffer current = new StringBuffer();
		LogLineParser lineParser = new LogLineParser(System.getProperty(CONFIGURED_PATTERN_CONFIG_KEY, PATTERN_DEFAULT));
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			for (String line; (line = br.readLine().trim()) != null;) {
				if (line.startsWith("at")) {
					current.append("\n\t").append(line);
				} else {
					String previous = current.toString();
					if (!previous.isEmpty()) {
						lines.add(lineParser.parseLine(previous));
					}
					current = new StringBuffer(line);
				}
			}
			String previous = current.toString().trim();
			if (!previous.isEmpty()) {
				lines.add(lineParser.parseLine(previous));
			}
			return createRoot(lines);
		} catch (IOException e) {
			throw new RuntimeException("Could not read log file '" + logFile.getPath() + "'.", e);
		}
	}

	private RootElement createRoot(List<Element> lines) {
		// TODO Auto-generated method stub
		return null;
	}

}
