package com.soa.rs.triviacreator.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;

/**
 * The TriviaFileReader reads in a XML configuration file and marshalls it into
 * a <tt>TriviaConfiguration</tt> object. This is then used either to load and
 * edit an existing configuration for the TriviaCreator application, or by the
 * Soa Discord Bot to initialize a trivia session.
 */

public class TriviaFileReader {

	/**
	 * Load in the configuration from the provided filename
	 * 
	 * @param filename
	 *            path to the XML file
	 * @return The Trivia configuration to be used
	 * @throws JAXBException
	 *             If the file cannot be successfully loaded due to an error.
	 * @throws SAXException
	 *             If the file cannot be schema validated.
	 */
	public TriviaConfiguration loadTriviaConfigFile(String filename) throws JAXBException, SAXException {
		File file = new File(filename);
		return loadTriviaConfigFile(file);
	}

	public TriviaConfiguration loadTriviaConfigFile(File file) throws JAXBException, SAXException {
		TriviaConfiguration config = null;
		JAXBContext jaxbContext = JAXBContext.newInstance("com.soa.rs.triviacreator.jaxb");

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/trivia.xsd"));

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(schema);

		JAXBElement<?> jaxbElement = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(file);
		config = (TriviaConfiguration) jaxbElement.getValue();

		return config;
	}

	/**
	 * Loads a configuration from a URL. It is expected this will be used to obtain
	 * the Trivia Configuration uploaded to a Discord PM channel.
	 * 
	 * @param url
	 *            The URL of the attachment
	 * @return The Trivia configuration to be used
	 * @throws JAXBException
	 *             If the file cannot be successfully loaded due to an error.
	 * @throws IOException
	 *             If there is an error reading to the stream.
	 * @throws SAXException
	 *             If the file cannot be schema validated.
	 */
	public TriviaConfiguration loadTriviaConfigFromURL(URL url) throws JAXBException, IOException, SAXException {
		TriviaConfiguration config = null;
		JAXBContext jaxbContext = JAXBContext.newInstance("com.soa.rs.triviacreator.jaxb");

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/trivia.xsd"));

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		jaxbUnmarshaller.setSchema(schema);

		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
		connection.setReadTimeout(10 * 1000);
		connection.connect();

		InputStream stream = connection.getInputStream();
		JAXBElement<?> jaxbElement = (JAXBElement<?>) jaxbUnmarshaller.unmarshal(stream);

		config = (TriviaConfiguration) jaxbElement.getValue();

		stream.close();

		return config;
	}
}
