package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;
import org.sagebionetworks.web.server.markdownparser.MathParser;

public class MathParserTest {
	MathParser parser;
	String testEquation = "\\begin{aligned}\n" +
			"\\nabla \\times \\vec{\\mathbf{B}} -\\, \\frac1c\\, \\frac{\\partial\\vec{\\mathbf{E}}}{\\partial t} & = \\frac{4\\pi}{c}\\vec{\\mathbf{j}} \\\\   \\nabla \\cdot \\vec{\\mathbf{E}} & = 4 \\pi \\rho \\\\\n" +
			"\\nabla \\times \\vec{\\mathbf{E}}\\, +\\, \\frac1c\\, \\frac{\\partial\\vec{\\mathbf{B}}}{\\partial t} & = \\vec{\\mathbf{0}} \\\\\n" +
			"\\nabla \\cdot \\vec{\\mathbf{B}} & = 0 \\end{aligned}";
	
	@Before
	public void setup(){
		parser = new MathParser();
	}
	
	@Test
	public void testMathBlock(){
		String line = "$$";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertFalse(result.contains(ServerMarkdownUtils.END_CONTAINER));
		
		assertTrue(parser.isInMarkdownElement());
		
		//feed test equation
		for (String l : testEquation.split("\n")) {
			elements = new MarkdownElements(l);
			parser.processLine(elements);
			result = elements.getHtml();
			assertFalse(result.contains(ServerMarkdownUtils.START_CONTAINER));
			assertTrue(result.contains(l));
			assertFalse(result.contains(ServerMarkdownUtils.END_CONTAINER));
			assertTrue(parser.isInMarkdownElement());
		}
		
		//last line
		line =  "$$";
		elements = new MarkdownElements(line);
		parser.processLine(elements);
		result = elements.getHtml();
		assertFalse(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertTrue(result.contains(ServerMarkdownUtils.END_CONTAINER));
		assertFalse(parser.isInMarkdownElement());
	}
	
	@Test
	public void testMathSpan(){
		String equation = "\\[\\left( \\sum_{k=1}^n a_k b_k \\right)^2 \\leq \\left( \\sum_{k=1}^n a_k^2 \\right) \\left( \\sum_{k=1}^n b_k^2 \\right)\\]";
		String line = "Contains this inline math $"+equation+"$ equation";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		//should contain both start and end.  It will not contain the equation (protected from other parsers)
		assertTrue(result.contains(ServerMarkdownUtils.START_CONTAINER));
		assertTrue(result.contains(ServerMarkdownUtils.END_CONTAINER));
		assertFalse(parser.isInMarkdownElement());
		assertFalse(result.contains(equation));
		
		//verify that equation is in the final output
		Document doc = Jsoup.parse(result);
		parser.completeParse(doc);
		assertTrue(doc.html().contains(equation));
	}

}
