package de.retest.recheck.logs;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import de.retest.recheck.RecheckAdapter;
import de.retest.ui.DefaultValueFinder;
import de.retest.ui.descriptors.RootElement;

public class LogFileAdapter implements RecheckAdapter {

	private static final String LOG_EXTENSION = ".log";

	@Override
	public boolean canCheck(Object toVerify) {
		return (toVerify instanceof File) && ((File) toVerify).getName().endsWith(LOG_EXTENSION);
	}

	@Override
	public Set<RootElement> convert(Object toVerify) {
		final File logFile = (File) toVerify;
		return Collections.singleton(new LogFileParser().parseLogFile(logFile));
	}

	@Override
	public DefaultValueFinder getDefaultValueFinder() {
		return (comp, attributesKey) -> null;
	}

}
