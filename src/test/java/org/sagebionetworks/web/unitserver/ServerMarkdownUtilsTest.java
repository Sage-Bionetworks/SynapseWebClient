package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.pegdown.PegDownProcessor;
import org.sagebionetworks.web.client.MarkdownUtils;
import org.sagebionetworks.web.server.ServerConstants;
import org.sagebionetworks.web.server.ServerMarkdownUtils;

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
	public void testDetectUrlLinks(){
		String testString = "<html> <head></head> <body> http://mytest.com http:// http <a href=\"#Synapse:syn555\">syn555 http://somewhereelse.org</a> syn</body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span> <a target=\"_blank\" class=\"link auto-detected-url\" href=\"http://mytest.com\">http://mytest.com</a> http:// http </span>\n  <a href=\"#Synapse:syn555\"><span>syn555 <a target=\"_blank\" class=\"link auto-detected-url\" href=\"http://somewhereelse.org\">http://somewhereelse.org</a></span></a>\n  <span> syn</span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.addUrlLinks(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testYouTubeVideos(){
		String testString = "<html> <head></head> <body> {youtube=60MQ3AG1c8o} </body></html>";
		String expectedResult = "<html> \n <head></head> \n <body>\n  <span><iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/60MQ3AG1c8o\" frameborder=\"0\" allowfullscreen=\"\"></iframe></span>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.addYouTubeVideos(htmlDoc);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testFixCSSClass(){
		String testString = "<ul><li>Abacus<ul><li>answer</li></ul></li><li>Bubbles<ol><li>bunk</li><li>bupkis<ul><li>BELITTLER</li></ul></li><li>burper</li></ol></li><li>Cunning</li></ul><blockquote> <p>Email-style angle brackets are used for blockquotes.</p></blockquote> <p><code>&lt;code&gt;</code> spans are delimited by backticks.</p><p>An <a href=\"http://url.com/\" title=\"Title\">example</a></p>";
		String expectedResult = "<html>\n <head></head>\n <body>\n  <ul class=\" myclass\">\n   <li>Abacus\n    <ul class=\" myclass\">\n     <li>answer</li>\n    </ul></li>\n   <li>Bubbles\n    <ol class=\" myclass\">\n     <li>bunk</li>\n     <li>bupkis\n      <ul class=\" myclass\">\n       <li>BELITTLER</li>\n      </ul></li>\n     <li>burper</li>\n    </ol></li>\n   <li>Cunning</li>\n  </ul>\n  <blockquote class=\" myclass\"> \n   <p>Email-style angle brackets are used for blockquotes.</p>\n  </blockquote> \n  <p><code>&lt;code&gt;</code> spans are delimited by backticks.</p>\n  <p>An <a href=\"http://url.com/\" title=\"Title\" class=\" myclass\">example</a></p>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.applyCssClass(htmlDoc, "myclass");
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
		String actualResult = ServerMarkdownUtils.markdown2Html(attachmentMd, "http://mySynapse/attachment", false, new PegDownProcessor(ServerConstants.MARKDOWN_OPTIONS));
		assertTrue(actualResult.contains("<img src=\"http://mySynapse/attachment?entityId=entityId123&amp;tokenId=tokenId123/previewTokenId&amp;waitForUrl=true\" alt=\"my attachment image\""));
	}
	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, new PegDownProcessor(ServerConstants.MARKDOWN_OPTIONS));
		assertTrue(actualResult.contains("&amp; ==&gt; &amp;&quot; ==&gt; &quot;&gt; ==&gt; &gt;&lt; ==&gt; &lt;Õ ="));
	}
	
	@Test
	public void testRemoveAllHTML(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "<table><tr><td>this is a test</td><td>column 2</td></tr></table><iframe width=\"420\" height=\"315\" src=\"http://www.youtube.com/embed/AOjaQ7Vl7SM\" frameborder=\"0\" allowfullscreen></iframe><embed>";
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, new PegDownProcessor(ServerConstants.MARKDOWN_OPTIONS));
		assertTrue(!actualResult.contains("<table>"));
		assertTrue(!actualResult.contains("<iframe>"));
		assertTrue(!actualResult.contains("<embed>"));
	}
	
	@Test
	public void testTableSupport(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = 
				"|             |          Grouping           ||\nFirst Header  | Second Header | Third Header |\n ------------ | :-----------: | -----------: |\nContent       |          *Long Cell*        ||\nContent       |   **Cell**    |         Cell |\n";
		
		PegDownProcessor processor = new PegDownProcessor(ServerConstants.MARKDOWN_OPTIONS);
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", false, processor);
		assertTrue(actualResult.contains("<table>"));
		assertTrue(actualResult.contains("<tr>"));
		assertTrue(actualResult.contains("<td>"));
	}

	@Test
	public void testAddWidgetDivs(){
		String testString = "<p>Line of widgets: <ul><li>{Widget:a widget name}</li><li>{Widget:a second widget name}</li></ul></p>";
		String expectedResult = "<html>\n <head></head>\n <body>\n  <p>Line of widgets: </p>\n  <ul>\n   <li>\n    <div>\n     <div id=\"a widget name\"></div>\n    </div></li>\n   <li>\n    <div>\n     <div id=\"a second widget name\"></div>\n    </div></li>\n  </ul>\n </body>\n</html>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.addWidgets(htmlDoc, false);
		String actualResult = htmlDoc.html();
		assertEquals(expectedResult, actualResult);
	}
}
