package de.retest.recheck.logs;

import static de.retest.recheck.logs.LogLineParser.LINE_TYPE;
import static de.retest.ui.descriptors.RetestIdProviderUtil.getRetestId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.retest.ui.Path;
import de.retest.ui.PathElement;
import de.retest.ui.descriptors.Attribute;
import de.retest.ui.descriptors.Element;
import de.retest.ui.descriptors.IdentifyingAttributes;
import de.retest.ui.descriptors.MutableAttributes;
import de.retest.ui.descriptors.PathAttribute;
import de.retest.ui.descriptors.RootElement;
import de.retest.ui.descriptors.StringAttribute;
import de.retest.ui.descriptors.SuffixAttribute;

public class LogFileParser {

	public static final String CONFIGURED_PATTERN_CONFIG_KEY = "de.retest.recheck.logs.pattern";
	public static final String LOGBACK_DEFAULT_PATTERN = "(?<dateTime>[-:\\d ]+) (?<level>[\\w]+) \\[(?<thread>.+)\\] (?<logger>[\\w\\.]+) - (?<message>.*)";
	public static final String LOG4J_DEFAULT_PATTERN = "(?<message>.*)";
	public static final String LOG4J_TTCC_CONVERSION_PATTERN = "(?<millis>[\\d]+) \\[(?<thread>.+)\\] (?<level>[\\w]+) (?<category>.+) (?<nestedDiagnosticContext>.+) - (?<message>.*)";

	private static final String LOG_FILE_TYPE = "log-file";

	public RootElement parseLogFile(File logFile) {
		List<Element> lines = new ArrayList<>();
		LogLineParser lineParser = new LogLineParser(System.getProperty(CONFIGURED_PATTERN_CONFIG_KEY, LOGBACK_DEFAULT_PATTERN));
		Path root = Path.fromString(LOG_FILE_TYPE);
		StringBuffer current = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			int lineCounter = 0;
			for (String line; (line = br.readLine()) != null;) {
				Path path = Path.path(root, new PathElement(LINE_TYPE, lineCounter));
				line = line.trim();
				if (lineParser.isNewLine(line)) {
					String previous = current.toString();
					if (!previous.isEmpty()) {
						lines.add(lineParser.parseLine(path, previous, lineCounter));
					}
					current = new StringBuffer(line);
				} else {
					current.append("\n\t").append(line);
				}
				lineCounter++;
			}
			String previous = current.toString().trim();
			if (!previous.isEmpty()) {
				Path path = Path.path(root, new PathElement(LINE_TYPE, lineCounter));
				lines.add(lineParser.parseLine(path, previous, lineCounter));
			}
			return createRoot(root, logFile, lines);
		} catch (IOException e) {
			throw new RuntimeException("Could not read log file '" + logFile.getPath() + "'.", e);
		}
	}

	private RootElement createRoot(Path path, File logFile, List<Element> lines) {
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new PathAttribute(path));
		attributes.add(new SuffixAttribute(Integer.toString(0)));
		attributes.add(new StringAttribute("type", LOG_FILE_TYPE));
		attributes.add(new StringAttribute("fileName", logFile.getName()));
		IdentifyingAttributes identifyingAttributes = new IdentifyingAttributes(attributes);
		return new RootElement(getRetestId(identifyingAttributes), identifyingAttributes, new MutableAttributes().immutable(), null, lines, "", 0, logFile.getName());
	}

}
