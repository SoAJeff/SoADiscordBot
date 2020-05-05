# Spirits of Arianwyn Discord Bot
Version: 3.0:  [![Build Status](https://travis-ci.org/SoAJeff/SoADiscordBot.svg?branch=master)](https://travis-ci.org/SoAJeff/SoADiscordBot)

A (mostly) Reactive Discord Bot written using the [Discord4J](https://github.com/Discord4J/Discord4J) v3 Java library for use by the Spirits of Arianwyn RuneScape clan's Discord Server.

Version 3 of the SoA Discord Bot is a complete rewrite to accomodate the new Project Reactor framework used within Discord4J.  The new bot is within the soa-discord module. 

## Building the Bot
To build the bot it is recommended to have:
- Java 8+.  Discord4J supports Java 8 and above.  You can download the JDK from [Oracle's Website](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Apache Maven.  The bot is currently structured to build with Maven.  You can get Maven from [Apache's Website](https://maven.apache.org/)

The bot can be built by running the following command:
```
mvn clean install
```

The build creates a shaded Jar with all dependencies included within to make executing the bot easier.

## More Information
For more information, please refer to the thread within Elvish Lounge found on the [SoA Forums](https://forums.soa-rs.com).
