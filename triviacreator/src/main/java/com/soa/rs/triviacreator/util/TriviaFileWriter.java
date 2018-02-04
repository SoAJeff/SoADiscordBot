package com.soa.rs.triviacreator.util;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.soa.rs.triviacreator.jaxb.ObjectFactory;
import com.soa.rs.triviacreator.jaxb.TriviaConfiguration;

/**
 * The TriviaFileWriter writes out a Trivia configuration XML file that is
 * configured by the user using the TriviaCreator application.
 */
public class TriviaFileWriter {

	private final ObjectFactory objectFactory = new ObjectFactory();

	/**
	 * Writes out the configuration file.
	 * 
	 * @param cfg
	 *            The created TriviaConfiguration object to be marshalled to XML
	 * @param filename
	 *            The path to where the XML file will be stored
	 * @throws JAXBException
	 *             If they file cannot be written due to an error
	 * @throws SAXException
	 *             If Schema validation fails
	 */
	public void writeTriviaConfigFile(TriviaConfiguration cfg, String filename) throws JAXBException, SAXException {
		File file = new File(filename);
		writeTriviaConfigFile(cfg, file);
	}

	public void writeTriviaConfigFile(TriviaConfiguration cfg, File file) throws JAXBException, SAXException {
		JAXBContext jaxbContext = JAXBContext.newInstance("com.soa.rs.triviacreator.jaxb");

		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(this.getClass().getResource("/xsd/trivia.xsd"));

		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setSchema(schema);

		JAXBElement<TriviaConfiguration> element = objectFactory.createTriviaConfiguration(cfg);

		jaxbMarshaller.marshal(element, file);

	}
}
