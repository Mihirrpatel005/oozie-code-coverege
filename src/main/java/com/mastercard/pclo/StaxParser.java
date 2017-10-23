package com.mastercard.pclo;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mastercard.pclo.model.IntervalList;

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class StaxParser {

	@Value("${outputDir}")
	public String outputDir;

	private long LineNumber;

	private String pathAttribute;

	private Document doc;

	private Node coverage;

	private List<IntervalList> IntervalList;

	private void setParameter(String pathAttribute, long maxLineNumber, List<IntervalList> allIntervalList) {

		this.LineNumber = maxLineNumber;
		this.pathAttribute = pathAttribute;
		this.IntervalList = allIntervalList;
	}

	public void xmlGenerate(String pathAttribute, long maxlinenumber, List<IntervalList> allIntervalList) {

		try {
			setParameter(pathAttribute, maxlinenumber, allIntervalList);

			if (isFileExist(new File(outputDir))) {

				doc = getDocumentInstance(outputDir);
				coverage = doc.getFirstChild();
				NodeList nodeList = doc.getElementsByTagName("file");

				for (int i = 0; i < nodeList.getLength(); i++) {

					Node nodeFile = nodeList.item(i);
					Node nodePath = nodeFile.getAttributes().getNamedItem("path");

					if (nodePath.getTextContent().equalsIgnoreCase(pathAttribute)) {
						coverage.removeChild(nodeFile);
						break;
					}

				}
				createFileNode(coverage, doc);
			} else {

				doc = createRootElement();
				coverage = doc.getFirstChild();

				createFileNode(coverage, doc);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createFileNode(Node coverage, Document doc) throws TransformerException {

		Element fileNode = createFileElement(coverage, doc);
		doc = createLineToCoverElement(fileNode, doc);
		xmlWriter(doc);

	}

	private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder;
	}

	private Document getDocumentInstance(String fileName) {

		Document doc = null;
		try {
			doc = getDocumentBuilder().parse(fileName);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.out.println(" Error while parsing file" + e);
		}
		return doc;
	}

	private Document createRootElement() {

		Document doc = null;
		try {
			doc = getDocumentBuilder().newDocument();

			Element rootElement = doc.createElement("coverage");
			doc.appendChild(rootElement);

			Attr version = doc.createAttribute("version");
			version.setValue("1");
			rootElement.setAttributeNode(version);

			xmlWriter(doc);

		} catch (ParserConfigurationException e) {
			System.out.println("Exception while creating Instance of new DocumentBuilder \n" + e);
		} catch (TransformerException e) {
			System.out.println("Exception while writing root element in file \n" + e);
		}
		return doc;

	}

	private Element createFileElement(Node coverage, Document doc) {

		Element fileNode = doc.createElement("file");
		coverage.appendChild(fileNode);

		Attr attr = doc.createAttribute("path");
		attr.setValue(pathAttribute);
		fileNode.setAttributeNode(attr);

		return fileNode;
	}

	private Document createLineToCoverElement(Element fileNode, Document doc) {
		long begin = 1;
		for (int i = 0; i < IntervalList.size(); i++) {

			long start = IntervalList.get(i).getStart();
			long end = IntervalList.get(i).getEnd();
			if (i != 0) {
				begin = IntervalList.get(i - 1).getEnd() + 1;
			}
			if (start > begin) {
				while (start > begin) {
					createAttribute(fileNode, begin, doc, "false");
					begin++;
				}
			}
			for (; start <= end; start++) {
				createAttribute(fileNode, start, doc, "true");
			}
		}
		return doc;
	}

	private Document createAttribute(Element fileNode, long lineNumber, Document doc, String covered) {

		Element lineToCover = doc.createElement("lineToCover");

		Attr coveredAttr = doc.createAttribute("covered");
		coveredAttr.setValue(covered);

		Attr lineNumberAttr = doc.createAttribute("lineNumber");
		lineNumberAttr.setValue(Long.toString(lineNumber));

		lineToCover.setAttributeNode(coveredAttr);
		lineToCover.setAttributeNode(lineNumberAttr);

		fileNode.appendChild(lineToCover);

		return doc;

	}

	private void xmlWriter(Document doc) throws TransformerException {

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(outputDir));
		transformer.transform(source, result);

		// Output to console for testing
		// StreamResult consoleResult = new StreamResult(System.out);
		// transformer.transform(source, consoleResult);

	}

	private boolean isFileExist(File file) throws IOException {

		if (!file.exists()) {
			return false;
		}
		return true;
	}
}