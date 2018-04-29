package com.soa.rs.triviacreator.util;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class ConfigFileTypeValidator {

	public static PanelType getPanelTypeForFile(File file) throws Exception {
		Source xmlFile = new StreamSource(file);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		if (validateSchema(xmlFile, schemaFactory, "/xsd/trivia.xsd"))
			return PanelType.TRIVIA_CREATE;
		else if (validateSchema(xmlFile, schemaFactory, "/xsd/triviaAnswers.xsd"))
			return PanelType.TRIVIA_ANSWER;
		else
			throw new Exception("Invalid file");
	}

	private static boolean validateSchema(Source xmlFile, SchemaFactory schemaFactory, String xsdPath) {
		boolean retval;
		try {
			Schema schema = schemaFactory.newSchema(ConfigFileTypeValidator.class.getResource(xsdPath));

			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			retval = true;
		} catch (Exception e) {
			retval = false;
//			e.printStackTrace();

		}
		return retval;
	}
}
