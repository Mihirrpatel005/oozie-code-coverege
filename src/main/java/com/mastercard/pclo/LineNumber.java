package com.mastercard.pclo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mastercard.pclo.model.IntervalList;
import com.mastercard.pclo.model.MergeInterval;

@Service
public class LineNumber {

	static HashMap<String, IntervalList> lineNumberByAction = new HashMap<String, IntervalList>();
	
	static List<String> actionList = new ArrayList<String>();
	
	static List<IntervalList> allIntervalList = new ArrayList<IntervalList>();
	
	static long maxlinenumber;
	
	@Autowired
	private StaxParser staxparser;

	public void oozieCoverageInput(String fileName, List<String> executedActions) throws IOException, SAXException {

		long start, end;
		actionList.clear();
		lineNumberByAction.clear();
		allIntervalList.clear();
		actionList = executedActions;

		InputStream inputStream = new FileInputStream(fileName);
		// new ByteArrayInputStream(fileName.getBytes());//

		Document document = new LineNumber().readXML(inputStream, "workflow-app");

		for (Entry<String, IntervalList> s : lineNumberByAction.entrySet()) {
			start = s.getValue().getStart();
			end = s.getValue().getEnd();
			System.out.println(">>>" + s.getKey() + "  >>> " + start + "  >>>" + end);
			allIntervalList.add(new IntervalList(start, end));

		}
		Collections.sort(allIntervalList);

		System.out.println("==========================================");
		for (IntervalList list1 : allIntervalList) {
			System.out.println(list1.getStart() + " :: " + list1.getEnd());
		}
		System.out.println("==========================================");

		allIntervalList = new MergeInterval().mergeInterval(allIntervalList);

		// setIntervalList.setIntervalList(allIntervalList);
		staxparser.xmlGenerate(fileName, maxlinenumber, allIntervalList);

	}

	public Document readXML(InputStream is, final String lineNumAttribName) throws IOException, SAXException {
		final Document doc;
		SAXParser parser;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Can't create SAX parser / DOM builder.", e);
		}

		final Stack<Element> elementStack = new Stack<Element>();
		final StringBuilder textBuffer = new StringBuilder();
		DefaultHandler handler = new DefaultHandler() {
			private Locator locator;

			@Override
			public void setDocumentLocator(Locator locator) {
				this.locator = locator;
				// Save the locator, so that it can be used later for line
				// tracking when traversing nodes.
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {

				addTextIfNeeded();
				Element el = doc.createElement(qName);

				for (int i = 0; i < attributes.getLength(); i++) {
					lineNumberByAction.put(qName, new IntervalList(locator.getLineNumber(), 0, attributes.getValue(i)));
					el.setAttribute(attributes.getQName(i), attributes.getValue(i));
				}

				el.setAttribute(lineNumAttribName, String.valueOf(locator.getLineNumber()));
				elementStack.push(el);
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				addTextIfNeeded();
				Element closedEl = elementStack.pop();

				if (lineNumberByAction.containsKey(qName)) {

					if (maxlinenumber < locator.getLineNumber())
						maxlinenumber = locator.getLineNumber();

					String tagName = lineNumberByAction.get(qName).getTagName();
					String tagNameType = tagName + " / " + qName;

					if (tagNameType.charAt(0) != '$' && actionList.contains(tagName)) {
						lineNumberByAction.put(tagNameType, new IntervalList(lineNumberByAction.get(qName).getStart(),
								locator.getLineNumber(), lineNumberByAction.get(qName).getTagName()));
					}
					lineNumberByAction.remove(qName);

				}

				if (elementStack.isEmpty()) { // Is this the root element?
					doc.appendChild(closedEl);
				} else {
					Element parentEl = elementStack.peek();
					parentEl.appendChild(closedEl);
				}
			}

			@Override
			public void characters(char ch[], int start, int length) throws SAXException {
				textBuffer.append(ch, start, length);
			}

			// Outputs text accumulated under the current node
			private void addTextIfNeeded() {
				if (textBuffer.length() > 0) {
					Element el = elementStack.peek();
					Node textNode = doc.createTextNode(textBuffer.toString());
					el.appendChild((org.w3c.dom.Node) textNode);
					textBuffer.delete(0, textBuffer.length());
				}
			}
		};
		parser.parse(is, handler);

		return doc;
	}

}
