package de.retest.recheck.logs;

import static de.retest.recheck.ui.descriptors.RetestIdProviderUtil.getRetestId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.retest.recheck.ui.Path;
import de.retest.recheck.ui.PathElement;
import de.retest.recheck.ui.descriptors.Attribute;
import de.retest.recheck.ui.descriptors.Element;
import de.retest.recheck.ui.descriptors.IdentifyingAttributes;
import de.retest.recheck.ui.descriptors.MutableAttributes;
import de.retest.recheck.ui.descriptors.PathAttribute;
import de.retest.recheck.ui.descriptors.RootElement;
import de.retest.recheck.ui.descriptors.StringAttribute;
import de.retest.recheck.ui.descriptors.SuffixAttribute;

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

	public Element parseLine(RootElement root, Path path, String line, int lineCounter) {
		if ((line == null) || line.isEmpty()) {
			throw new IllegalArgumentException("Line cannot be null or empty!");
		}

		Matcher matcher = pattern.matcher(line);
		if (!matcher.find()) {
			throw new IllegalArgumentException("Parsing log file should work such that all lines that belong together are delivered together, i.e. stack trace.");
		}

		Element lineElement = createLineElement(root, path, lineCounter);
		for (String groupName : groupNames) {
			try {
				String group = matcher.group(groupName).trim();
				if (!group.isEmpty()) {
					lineElement.addChildren(createChild(lineElement, path, lineCounter, group, groupName));
				}
			} catch (IllegalStateException e) {
				// mute if a group is sometimes not found
			}
		}

		return lineElement;
	}

	private Element createChild(Element parent, Path path, int lineCounter, String group, String groupName) {
		final List<Attribute> identAttributes = new ArrayList<>();
		identAttributes.add(new PathAttribute(Path.path(path, new PathElement(groupName, lineCounter))));
		identAttributes.add(new SuffixAttribute(lineCounter));
		identAttributes.add(new StringAttribute("type", groupName));
		MutableAttributes attributes = new MutableAttributes();
		attributes.put("text", group);
		IdentifyingAttributes identifyingAttributes = new IdentifyingAttributes(identAttributes);
		return Element.create(getRetestId(identifyingAttributes), parent, identifyingAttributes, attributes.immutable());
	}

	private Element createLineElement(RootElement root, Path path, int lineCounter) {
		final List<Attribute> attributes = new ArrayList<>();
		attributes.add(new PathAttribute(path));
		attributes.add(new SuffixAttribute(lineCounter));
		attributes.add(new StringAttribute("type", LINE_TYPE));
		IdentifyingAttributes identifyingAttributes = new IdentifyingAttributes(attributes);
		return Element.create(getRetestId(identifyingAttributes), root, identifyingAttributes, new MutableAttributes().immutable());
	}
}
