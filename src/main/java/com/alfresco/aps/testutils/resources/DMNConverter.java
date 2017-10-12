package com.alfresco.aps.testutils.resources;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.activiti.dmn.model.DmnDefinition;
import com.activiti.dmn.xml.converter.DmnXMLConverter;
import com.activiti.editor.dmn.converter.DmnJsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Temporary workaround to create dmn xml from json until https://issues.alfresco.com/jira/browse/ACTIVITI-994 is fixed
 */
public class DMNConverter {

	public static void convertJsonModelToDMNXml(String dmnFilePath, String outputPath)
			throws JsonProcessingException, IOException {
		DmnJsonConverter dmnJsonConverter = new DmnJsonConverter();
		DmnXMLConverter dmnXMLConverter = new DmnXMLConverter();
		DmnDefinition dmnDefinition = null;
		ObjectMapper objectMapper = new ObjectMapper();

		InputStream is = new FileInputStream(dmnFilePath);
		ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(is).get("editorJson");
		is.close();

		dmnDefinition = dmnJsonConverter.convertToDmn(editorJsonNode, 1L, 1, new Date());
		byte[] xmlBytes = dmnXMLConverter.convertToXML(dmnDefinition, "UTF-8");

		FileOutputStream fos = new FileOutputStream(outputPath);
		fos.write(xmlBytes);
		fos.close();
	}
}
