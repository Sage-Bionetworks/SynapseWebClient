package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.sagebionetworks.web.server.DoiAutoLinkDetector;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.server.SynapseAutoLinkDetector;
import org.sagebionetworks.web.server.UrlAutoLinkDetector;

import eu.henkelmann.actuarius.ActuariusTransformer;

public class ServerMarkdownUtilsTest {

	@Test
	public void testDetectEntityLinks(){
		String testString = "<html> <head></head> <body> synapse123 SYn1234\nsyn567 syntax syn3 <a href=\"http://somewhere.else\">link text that has the synapse id syn555 embedded in it.</a> syn</body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span> synapse123 <a class=\"link\" href=\"#!Synapse:SYn1234\">SYn1234</a> <a class=\"link\" href=\"#!Synapse:syn567\">syn567</a> syntax <a class=\"link\" href=\"#!Synapse:syn3\">syn3</a></span>\n  <a href=\"http://somewhere.else\">link text that has the synapse id syn555 embedded in it.</a>\n  <span> syn</span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		SynapseAutoLinkDetector.getInstance().createLinks(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testDetectDoiLinks(){
		String testString = "<html> <head></head> <body>doi:10.5072/fk2.syn12345 not:a:doi: doi:10.1016/j.compcom.2005.12.006\nDoil doing <a href=\"http://somewhere.else\">link text that has a doi:10.5072/fk2.syn12345 in it.</a> should not be touched doi:</body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span><a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/10.5072/fk2.syn12345\">doi:10.5072/fk2.syn12345</a> not:a:doi: <a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/10.1016/j.compcom.2005.12.006\">doi:10.1016/j.compcom.2005.12.006</a> Doil doing </span>\n  <a href=\"http://somewhere.else\">link text that has a doi:10.5072/fk2.syn12345 in it.</a>\n  <span> should not be touched doi:</span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		DoiAutoLinkDetector.getInstance().createLinks(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testUrlLinks(){
		String testString = "<html> <head></head> <body>http://test1.com https://test2.org ftp://test.com/test3 HTtp://test4.org http3://notalink.com <a href=\"http://somewhere.else\">link text that has a link http://link.com in it.</a> should not be touched http://</body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span><a target=\"_blank\" class=\"link\" href=\"http://test1.com\">http://test1.com</a> <a target=\"_blank\" class=\"link\" href=\"https://test2.org\">https://test2.org</a> <a target=\"_blank\" class=\"link\" href=\"ftp://test.com/test3\">ftp://test.com/test3</a> <a target=\"_blank\" class=\"link\" href=\"HTtp://test4.org\">HTtp://test4.org</a> http3://notalink.com </span>\n  <a href=\"http://somewhere.else\">link text that has a link http://link.com in it.</a>\n  <span> should not be touched http://</span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		UrlAutoLinkDetector.getInstance().createLinks(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}

	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters() throws IOException{
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, false, new ActuariusTransformer());
		assertTrue(actualResult.contains("&amp; ==&gt; &amp;&quot; ==&gt; &quot;&gt; ==&gt; &gt; &lt; ==&gt; &lt;' ="));
	}
	
	@Test
	public void testWhitespacePreservation() throws IOException{
		String codeBlock = " spaces  and\nnewline    -  test\n  preservation in preformatted  code blocks";
		String testString = "```\n"+codeBlock+"\n```";
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, false, new ActuariusTransformer());
		//it should contain the code block, exactly as written
		assertTrue(actualResult.contains(codeBlock));
	}
	
	@Test
	public void testFixNewlines() throws IOException{
		String testString = "should have line break\n```\nthis is code so it should not\nhave any html line breaks\n```\nagain";
		String actualResult = ServerMarkdownUtils.fixNewLines(testString);
		assertTrue(actualResult.contains("should have line break<br />"));
		assertTrue(actualResult.contains("this is code so it should not\n"));
		assertTrue(actualResult.contains("again<br />"));
	}
	
	@Test
	public void testRemoveAllHTML() throws IOException{
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "<table><tr><td>this is a test</td><td>column 2</td></tr></table><iframe width=\"420\" height=\"315\" src=\"http://www.youtube.com/embed/AOjaQ7Vl7SM\" frameborder=\"0\" allowfullscreen></iframe><embed>";
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, false, new ActuariusTransformer());
		assertTrue(!actualResult.contains("<table>"));
		assertTrue(!actualResult.contains("<iframe>"));
		assertTrue(!actualResult.contains("<embed>"));
	}
	
	@Test
	public void testRAssign() throws IOException{
		//testing R assignment operator (html stripping should not alter)
		String testString = "DemoClinicalOnlyModel <- setRefClass(Class  = \"CINModel\",...";
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, false, new ActuariusTransformer());
		//there should be no space between the less than and the dash:
		assertTrue(actualResult.contains("&lt;-"));
	}
	
	@Test
	public void testTableSupport() throws IOException{
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = 
				"|             |          Grouping           ||\nFirst Header  | Second Header | Third Header |\n ------------ | :-----------: | -----------: |\nContent       |          *Long Cell*        ||\nContent       |   **Cell**    |         Cell |\n";
		
		ActuariusTransformer processor = new ActuariusTransformer();
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, false, processor);
		assertTrue(actualResult.contains("<table"));
		assertTrue(actualResult.contains("<tr>"));
		assertTrue(actualResult.contains("<td>"));
	}
	
	@Test
	public void testAddWidgetDivs(){
		String testString = "<p>Line of widgets: <ul><li>${type1:aWidgetParam=1}</li><li>${type2:aWidgetParam=2}</li></ul></p>";
		String expectedResult = "<html>\n <head></head>\n <body>\n  <p>Line of widgets: </p>\n  <ul>\n   <li>\n    <div>\n     <div id=\"widget_0\" widgetparams=\"type1:aWidgetParam=1\"></div>\n    </div></li>\n   <li>\n    <div>\n     <div id=\"widget_1\" widgetparams=\"type2:aWidgetParam=2\"></div>\n    </div></li>\n  </ul>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.addWidgets(htmlDoc, false);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testResolveHorizontalRules(){
		//dash test
		String testString = "--------\n-----\n---\n-\n - -  - -   -  \n-----  foo\nother content ------ in line should not match\n------\n- - - - -";
		String expectedResult = "<hr>\n<hr>\n<hr>\n-\n<hr>\n-----  foo\nother content ------ in line should not match\n<hr>\n<hr>\n";
		String result = ServerMarkdownUtils.resolveHorizontalRules(testString);
		assertEquals(expectedResult, result);
		
		//asterisk test
		testString = "********\n*****\n***\n*\n * *  * *   *  \n*****  foo\nother content ****** in line should not match\n******\n* * * * *";
		expectedResult = "<hr>\n<hr>\n<hr>\n*\n<hr>\n*****  foo\nother content ****** in line should not match\n<hr>\n<hr>\n";
		result = ServerMarkdownUtils.resolveHorizontalRules(testString);
		assertEquals(expectedResult, result);
	}
	
	@Test
	public void testResolveTables(){
		String testString = "${image?fileName=bill%5Fgates%2Egif}  | Second Header | Third Header\nContent Cell1a  | Content Cell2a  | Content Cell3a\nContent Cell1b  | Content Cell2b   Content Cell3b";
		String result = ServerMarkdownUtils.resolveTables(testString);
		assertTrue(result.contains("<table"));
		
		testString = "|Content Cell1a  | Content Cell2a  | Content Cell3a|\n|Content Cell1b  | Content Cell2b   Content Cell3b|\n\nMore text below";
		result = ServerMarkdownUtils.resolveTables(testString);
		assertTrue(result.contains("<table"));
		assertTrue(result.contains("More text below"));
	}
	
	@Test
	public void testAssignIdsToHeadings(){
		//should assign h4 as toc-indent0 and id of synapseheading1
		String testString = "<h6>smallest header</h6><h4 >top header</h4> <p><em>content</em><br></p> <h5>sub header 1</h5>";
		String expectedResult = "<html>\n <head></head>\n <body>\n  <h6 id=\"synapseheading0\" level=\"h6\" toc-style=\"toc-indent2\">smallest header</h6>\n  <h4 id=\"synapseheading1\" level=\"h4\" toc-style=\"toc-indent0\">top header</h4> \n  <p><em>content</em><br /></p> \n  <h5 id=\"synapseheading2\" level=\"h5\" toc-style=\"toc-indent1\">sub header 1</h5>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.assignIdsToHeadings(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
}
