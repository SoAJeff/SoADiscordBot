package com.soa.rs.discordbot.v3.api.interaction;

import java.util.HashMap;
import java.util.Map;

import com.soa.rs.discordbot.v3.api.annotation.Interaction;
import com.soa.rs.discordbot.v3.api.command.AbstractCommand;
import com.soa.rs.discordbot.v3.cfg.DiscordCfgFactory;
import com.soa.rs.discordbot.v3.util.SoaLogging;

import org.reflections.Reflections;

public class InteractionInitializer {

	private static final Map<String, AbstractCommand> COMMAND_MAP = new HashMap<>();

	public static boolean init()
	{
		final Reflections reflections = new Reflections("com.soa.rs.discordbot.v3");

		for(Class<?> clazz : reflections.getTypesAnnotatedWith(Interaction.class))
		{
			initializeInteractions(clazz);
		}
		return true;
	}

	private static void initializeInteractions(Class<?> clazz)
	{
		if (!AbstractCommand.class.isAssignableFrom(clazz)) {
			SoaLogging.getLoggerForClass(InteractionInitializer.class).error("Found class [" + clazz.getSimpleName()
					+ "] that is annotated with @Interaction but does not extend the AbstractCommand class, skipping...");
			return;
		}

		try
		{
			final AbstractCommand cmd = (AbstractCommand) clazz.getConstructor().newInstance();

			cmd.initialize();

			if(cmd.isEnabled())
			{
				Interaction interactionAnnotation = clazz.getAnnotation(Interaction.class);
				if(interactionAnnotation.trigger() != null)
				{
					if(COMMAND_MAP.putIfAbsent(interactionAnnotation.trigger(), cmd) != null)
					{
						SoaLogging.getLoggerForClass(InteractionInitializer.class).error("Command name collision");
					} else {
						SoaLogging.getLoggerForClass(InteractionInitializer.class)
								.debug("Added Interaction for class [" + clazz.getSimpleName() + "] with trigger: [" + interactionAnnotation.trigger() + "]");
						//Use the trigger to create the name of the json file with the arguments, and set that for use.
						DiscordCfgFactory.getInstance().addCommandFilename(interactionAnnotation.trigger() + ".json");
					}
				}
			}
		}
		catch (Exception e) {
			SoaLogging.getLoggerForClass(InteractionInitializer.class).error("Error initializing command: " + clazz.getSimpleName(), e);
		}
	}

	public static Map<String, AbstractCommand> getInteractions() {
		return COMMAND_MAP;
	}

	public static AbstractCommand getInteraction(String name) {
		return COMMAND_MAP.get(name);
	}

}
