package de.retest.recheck.logs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.retest.ui.Path;
import de.retest.ui.PathElement;
import de.retest.ui.descriptors.Attribute;
import de.retest.ui.descriptors.Element;
import de.retest.ui.descriptors.IdentifyingAttributes;
import de.retest.ui.descriptors.MutableAttributes;
import de.retest.ui.descriptors.PathAttribute;
import de.retest.ui.descriptors.StringAttribute;
import de.retest.ui.descriptors.SuffixAttribute;

public class LogLineParser {

	public static final String LINE_TYPE = "line";

	private final Pattern pattern;
	private final List<String> groupNames;

	public LogLineParser(String pattern) {
		this.pattern = Pattern.compile(pattern);
		groupNames = parseGroupNames(pattern);
	}

	private List<String> parseGroupNames(String pattern) {
		Pattern groupNamePattern = Pattern.compile("\\?\\<([\\w]*)\\>");
		Matcher matcher = groupNamePattern.matcher(pattern);
		List<String> result = new ArrayList<>();
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}

	public boolean isNewLine(String line) {
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	public Element parseLine(Path path, String line, int lineCounter) {
		if ((line == null) || line.isEmpty()) {
			throw new IllegalArgumentException("Line cannot be null or empty!");
		}

		Matcher matcher = pattern.matcher(line);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Parsing log file should work such that all lines that belong together are delivered together, i.e. stack trace.");
		}

		List<Element> lines = new ArrayList<>();
		for (String groupName : groupNames) {
			try {
				String group = matcher.group(groupName).trim();
				if (!group.isEmpty()) {
					lines.add(createChild(path, lineCounter, group, groupName));
				}
			} catch (IllegalStateException e) {
				// mute if a group is sometimes not found
			}
		}

		return createElement(path, lines, lineCounter);
	}

	private Element createChild(Path path, int lineCounter, String group, String groupName) {
		final List<Attribute> identAttributes = new ArrayList<>();
		identAttributes.add(new PathAttribute(Path.path(path, new PathElement(groupName, lineCounter))));
		identAttributes.add(new SuffixAttribute(Integer.toString(lineCounter)));
		identAttributes.add(new StringAttribute("type", groupName));
		MutableAttributes attributes = new MutableAttributes();
		attributes.put("text", group);
		return new Element(new IdentifyingAttributes(identAttributes), attributes.immutable());
	}

	private Element createElement(Path path, List<Element> parts, int lineCounter) {
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new PathAttribute(path));
		attributes.add(new SuffixAttribute(Integer.toString(lineCounter)));
		attributes.add(new StringAttribute("type", LINE_TYPE));
		IdentifyingAttributes identifyingAttributes = new IdentifyingAttributes(attributes);
		return new Element(identifyingAttributes, new MutableAttributes().immutable(), parts);
	}
}
