package com.soa.rs.discordbot.cfg;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.soa.rs.discordbot.jaxb.DiscordConfiguration;

/**
 * The ConfigReader reads in a XML configuration file and marshalls it into a
 * <tt>DiscordConfiguration</tt> object, for use in initial startup of the bot.
 */
public class ConfigReader {

	/**
	 * Load in the configuration from the provided filename
	 * 
	 * @param filename
	 *            path to the XML file
	 * @return configuration
	 * @throws JAXBException
	 * @throws SAXException
	 */
	public DiscordConfiguration loadAppConfig(String filename) throws JAXBException, SAXException {
		DiscordConfiguration config = null;

		File file = new File(filename);
		JAXBContext jaxbContext = JAXBContext.newInstance(DiscordConfiguration.class);

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/discordConfiguration.xsd"));

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(schema);
		config = (DiscordConfiguration) jaxbUnmarshaller.unmarshal(file);

		return config;
	}
}
