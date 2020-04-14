package com.soa.rs.discordbot.v3.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * This class provides a central place to obtain the logger for the bot,
 * removing the need to instantiate one for each class that needs to log.
 *
 * <p>
 * Once the logger object is created (by running <tt>initializeLogging</tt> at
 * the beginning of the application), the logger can always be obtained by
 * calling the <tt>getLogger</tt> method. A new instance of the
 * <tt>SoaLogging</tt> class is not needed, and the class itself has been
 * structured as a singleton so there will only be one instance of the class at
 * any given time.
 */
public class SoaLogging {

	private static SoaLogging INSTANCE = null;
	private static Logger logger;

	private SoaLogging() {
		logger = LogManager.getRootLogger();
	}

	private SoaLogging(String lcf) {
		Configurator.initialize("SoA", lcf);
	}

	/**
	 * Returns the instance of the Soa Logging object. Recommended to use
	 * <tt>getLogger</tt> after calling <tt>initializeLogging</tt> rather than
	 * calling this method.
	 *
	 * @return SoaLogging instance
	 */
	public static SoaLogging getInstance() {
		initializeLogging();
		return INSTANCE;
	}

	/**
	 * Initializes the logger object if it has not yet been created.
	 */
	public static void initializeLogging() {
		if (INSTANCE == null) {
			INSTANCE = new SoaLogging();
		}
	}

	public static void initializeLoggingFromLcf(String lcf) {
		if (INSTANCE == null) {
			INSTANCE = new SoaLogging(lcf);
		}
	}

	/**
	 * Returns the logger object for a given class, for use with static classes.
	 *
	 * @param clazz Class for which to get the logger for
	 * @return Logger for the provided class
	 */
	public static Logger getLoggerForClass(Class clazz) {
		return LogManager.getLogger(clazz);
	}

	/**
	 * Returns a logger for the provided object
	 *
	 * @param o Reference to the class doing the logging - typically specified as "this"
	 * @return Logger for the provided object
	 */
	public static Logger getLogger(Object o) {
		return LogManager.getLogger(o);
	}
}
