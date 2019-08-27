package com.soa.rs.discordbot.v3.api.command;

import java.util.HashMap;
import java.util.Map;

import com.soa.rs.discordbot.v3.api.annotation.Command;
import com.soa.rs.discordbot.v3.api.annotation.MessageRcv;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class CommandInitializer {

	private static final Map<String, AbstractCommand> COMMANDS_MAP = new HashMap<>();
	private static final Map<String, MsgRcvd> ANY_MESSAGE_MAP = new HashMap<>();

	public static boolean init() {
		final Reflections reflections = new Reflections("com.soa.rs.discordbot.v3", new SubTypesScanner(),
				new TypeAnnotationsScanner());
		for (Class<?> cmdClass : reflections.getTypesAnnotatedWith(Command.class)) {
			initializeCommands(cmdClass);
		}

		for (Class<?> anyMsgClass : reflections.getTypesAnnotatedWith(MessageRcv.class)) {
			initializeAnyMessageReceivedEvents(anyMsgClass);

		}
		return true;
	}

	private static void initializeCommands(Class<?> cmdClass) {
		if (!AbstractCommand.class.isAssignableFrom(cmdClass)) {
			SoaLogging.getLoggerForClass(CommandInitializer.class).error("Found class [" + cmdClass.getSimpleName()
					+ "] that is annotated with @Command but does not extend the AbstractCommand class, skipping...");
			return;
		}
		try {
			final AbstractCommand cmd = (AbstractCommand) cmdClass.getConstructor().newInstance();

			cmd.initialize();

			if (cmd.isEnabled()) {

				for (String name : cmd.getTriggers()) {
					if (COMMANDS_MAP.putIfAbsent(name.toLowerCase(), cmd) != null) {
						SoaLogging.getLoggerForClass(CommandInitializer.class).error("Command name collision");
					} else {
						SoaLogging.getLoggerForClass(CommandInitializer.class)
								.debug("Added command for class [" + cmdClass.getSimpleName() + "] with trigger: [" + name + "]");
					}
				}
			} else {
				SoaLogging.getLoggerForClass(CommandInitializer.class)
						.debug("Command [" + cmdClass.getSimpleName() + "] was set to disabled, skipping...");
			}
		} catch (Exception e) {
			SoaLogging.getLoggerForClass(CommandInitializer.class).error("Error initializing command: " + cmdClass.getSimpleName());
		}
	}

	private static void initializeAnyMessageReceivedEvents(Class<?> anyMsgClass) {
		if (!MsgRcvd.class.isAssignableFrom(anyMsgClass)) {
			SoaLogging.getLoggerForClass(CommandInitializer.class).error("Found class [" + anyMsgClass.getSimpleName()
					+ "] that is annotated with @MessageRcv but does not extend the MsgRcvd class, skipping...");
			return;
		}
		try {
			final MsgRcvd msg = (MsgRcvd) anyMsgClass.getConstructor().newInstance();

			msg.initialize();

			if (msg.isEnabled()) {
				for (String trigger : msg.getTriggers()) {
					if (ANY_MESSAGE_MAP.putIfAbsent(trigger.toLowerCase(), msg) != null) {
						SoaLogging.getLoggerForClass(CommandInitializer.class).error("Command name collision");
					} else {
						SoaLogging.getLoggerForClass(CommandInitializer.class).debug("Added event for class [" + anyMsgClass.getSimpleName() + "] with trigger [" + trigger + "]");
					}
				}
			} else {
				SoaLogging.getLoggerForClass(CommandInitializer.class)
						.debug("Event [" + anyMsgClass.getSimpleName() + "] was set to disabled, skipping...");
			}
		} catch (Exception e) {
			SoaLogging.getLoggerForClass(CommandInitializer.class).error("Error initializing command: " + anyMsgClass.getSimpleName());
		}
	}

	public static Map<String, AbstractCommand> getCommands() {
		return COMMANDS_MAP;
	}

	public static AbstractCommand getCommand(String name) {
		return COMMANDS_MAP.get(name);
	}

	public static Map<String, MsgRcvd> getAnyMessageMap() {
		return ANY_MESSAGE_MAP;
	}

	public static MsgRcvd getMsgRcvd(String key)
	{
		return ANY_MESSAGE_MAP.get(key);
	}
}
