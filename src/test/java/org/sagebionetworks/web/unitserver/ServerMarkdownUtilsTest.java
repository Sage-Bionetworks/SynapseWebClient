package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.pegdown.PegDownProcessor;
import org.sagebionetworks.web.client.MarkdownUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class ServerMarkdownUtilsTest {

	@Test
	public void testDetectEntityLinks(){
		String testString = "<html> <head></head> <body> synapse123 SYn1234\nsyn567 syntax syn3 <a href=\"http://somewhere.else\">link text that has the synapse id syn555 embedded in it.</a> syn</body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span> synapse123 <a target=\"_blank\" class=\"link auto-detected-synapse-link\" href=\"#Synapse:syn1234\">SYn1234</a> <a target=\"_blank\" class=\"link auto-detected-synapse-link\" href=\"#Synapse:syn567\">syn567</a> syntax <a target=\"_blank\" class=\"link auto-detected-synapse-link\" href=\"#Synapse:syn3\">syn3</a></span>\n  <a href=\"http://somewhere.else\">link text that has the synapse id syn555 embedded in it.</a>\n  <span> syn</span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.addSynapseLinks(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testImageAttachmentLinks(){
		String entityId = "entityId123";
		String tokenId = "tokenId123";
		String previewTokenId = "previewTokenId123";
		String attachmentName = "my attachment image";
		String attachmentMd = MarkdownUtils.getAttachmentLinkMarkdown(attachmentName, entityId, tokenId, previewTokenId, attachmentName);
		String actualResult = ServerMarkdownUtils.markdown2Html(attachmentMd, "http://mySynapse/attachment", false, new PegDownProcessor(WebConstants.MARKDOWN_OPTIONS));
		assertTrue(actualResult.contains("<img src=\"http://mySynapse/attachment?entityId=entityId123&amp;tokenId=tokenId123/previewTokenId&amp;waitForUrl=true\" alt=\"my attachment image\""));
	}
	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, new PegDownProcessor(WebConstants.MARKDOWN_OPTIONS));
		assertTrue(actualResult.contains("&amp; ==&gt; &amp;&quot; ==&gt; &quot;&gt; ==&gt; &gt;&lt; ==&gt; &lt;"));
	}
	
	@Test
	public void testRemoveAllHTML(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "<table><tr><td>this is a test</td><td>column 2</td></tr></table><iframe width=\"420\" height=\"315\" src=\"http://www.youtube.com/embed/AOjaQ7Vl7SM\" frameborder=\"0\" allowfullscreen></iframe><embed>";
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, new PegDownProcessor(WebConstants.MARKDOWN_OPTIONS));
		assertTrue(!actualResult.contains("<table>"));
		assertTrue(!actualResult.contains("<iframe>"));
		assertTrue(!actualResult.contains("<embed>"));
	}
	
	@Test
	public void testTableSupport(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = 
				"|             |          Grouping           ||\nFirst Header  | Second Header | Third Header |\n ------------ | :-----------: | -----------: |\nContent       |          *Long Cell*        ||\nContent       |   **Cell**    |         Cell |\n";
		
		PegDownProcessor processor = new PegDownProcessor(WebConstants.MARKDOWN_OPTIONS);
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, processor);
		assertTrue(actualResult.contains("<table>"));
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
}
