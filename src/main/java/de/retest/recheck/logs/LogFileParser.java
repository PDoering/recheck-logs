package de.retest.recheck.logs;

import static de.retest.recheck.logs.LogLineParser.LINE_TYPE;
import static de.retest.recheck.ui.descriptors.RetestIdProviderUtil.getRetestId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.retest.recheck.ui.Path;
import de.retest.recheck.ui.PathElement;
import de.retest.recheck.ui.descriptors.Attribute;
import de.retest.recheck.ui.descriptors.IdentifyingAttributes;
import de.retest.recheck.ui.descriptors.MutableAttributes;
import de.retest.recheck.ui.descriptors.PathAttribute;
import de.retest.recheck.ui.descriptors.RootElement;
import de.retest.recheck.ui.descriptors.StringAttribute;
import de.retest.recheck.ui.descriptors.SuffixAttribute;

public class LogFileParser {

	public static final String CONFIGURED_PATTERN_CONFIG_KEY = "de.retest.recheck.logs.pattern";
	public static final String LOGBACK_DEFAULT_PATTERN = "(?<dateTime>[-:\\d ]+) (?<level>[\\w]+) \\[(?<thread>.+)\\] (?<logger>[\\w\\.]+) - (?<message>.*)";
	public static final String LOG4J_DEFAULT_PATTERN = "(?<message>.*)";
	public static final String LOG4J_TTCC_CONVERSION_PATTERN = "(?<millis>[\\d]+) \\[(?<thread>.+)\\] (?<level>[\\w]+) (?<category>.+) (?<nestedDiagnosticContext>.+) - (?<message>.*)";

	private static final String LOG_FILE_TYPE = "log-file";

	public RootElement parseLogFile(File logFile) {
		LogLineParser lineParser = new LogLineParser(System.getProperty(CONFIGURED_PATTERN_CONFIG_KEY, LOGBACK_DEFAULT_PATTERN));
		Path rootPath = Path.fromString(LOG_FILE_TYPE);
		RootElement root = createRoot(rootPath, logFile);
		StringBuffer current = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			int lineCounter = 0;
			for (String line; (line = br.readLine()) != null;) {
				Path path = Path.path(rootPath, new PathElement(LINE_TYPE, lineCounter));
				line = line.trim();
				if (lineParser.isNewLine(line)) {
					String previous = current.toString();
					if (!previous.isEmpty()) {
						root.addChildren(lineParser.parseLine(root, path, previous, lineCounter));
					}
					current = new StringBuffer(line);
				} else {
					current.append("\n\t").append(line);
				}
				lineCounter++;
			}
			String previous = current.toString().trim();
			if (!previous.isEmpty()) {
				Path path = Path.path(rootPath, new PathElement(LINE_TYPE, lineCounter));
				root.addChildren(lineParser.parseLine(root, path, previous, lineCounter));
			}
			return root;
		} catch (IOException e) {
			throw new RuntimeException("Could not read log file '" + logFile.getPath() + "'.", e);
		}
	}

	private RootElement createRoot(Path path, File logFile) {
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new PathAttribute(path));
		attributes.add(new SuffixAttribute(0));
		attributes.add(new StringAttribute("type", LOG_FILE_TYPE));
		attributes.add(new StringAttribute("fileName", logFile.getName()));
		IdentifyingAttributes identifyingAttributes = new IdentifyingAttributes(attributes);
		return new RootElement(getRetestId(identifyingAttributes), identifyingAttributes, new MutableAttributes().immutable(), null, "", 0, logFile.getName());
	}

}
