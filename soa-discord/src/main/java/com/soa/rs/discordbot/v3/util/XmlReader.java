package com.soa.rs.discordbot.v3.util;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.soa.rs.discordbot.v3.jaxb.DiscordConfiguration;
//import com.soa.rs.discordbot.jaxb.TrackedInformation;

import org.xml.sax.SAXException;

/**
 * The XmlReader reads in a XML configuration file and marshalls it into a
 * <tt>DiscordConfiguration</tt> object, for use in initial startup of the bot.
 */
public class XmlReader {

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
		JAXBContext jaxbContext = JAXBContext.newInstance("com.soa.rs.discordbot.v3.jaxb");

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/discordConfiguration.xsd"));

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(schema);
		config = (DiscordConfiguration) jaxbUnmarshaller.unmarshal(file);

		// config = (DiscordConfiguration) jaxbElement.getValue();

		return config;
	}

//	public TrackedInformation loadTrackedConfiguration(String filename)
//			throws JAXBException, SAXException, IOException {
//		TrackedInformation info = null;
//
//		File file = new File(filename);
//		if (!file.exists()) {
//			throw new IOException("File does not exist");
//		}
//		JAXBContext jaxbContext = JAXBContext.newInstance("com.soa.rs.discordbot.jaxb");
//
//		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/userTracking.xsd"));
//
//		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//		jaxbUnmarshaller.setSchema(schema);
//		info = (TrackedInformation) jaxbUnmarshaller.unmarshal(file);
//
//		// info = (TrackedInformation) jaxbElement.getValue();
//
//		return info;
//	}
}
