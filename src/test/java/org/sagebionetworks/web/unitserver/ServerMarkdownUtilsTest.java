package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.sagebionetworks.web.client.MarkdownUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;

import com.petebevin.markdown.MarkdownProcessor;

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
		String actualResult = ServerMarkdownUtils.markdown2Html(attachmentMd, "http://mySynapse/attachment", new MarkdownProcessor());
		String expectedResult = "<div class=\"markdown\"><html>\n <head></head>\n <body>\n  <p><img src=\"http://mySynapse/attachment?entityId=entityId123&amp;tokenId=tokenId123/previewTokenId&amp;waitForUrl=true\" alt=\"my attachment image\" title=\"my attachment image\" /></p> \n </body>\n</html></div>";
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters(){
		//testing html control character conversion (leaving this up to the markdown library, so it has to work!)
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		String expectedResult = "<div class=\"markdown\"><html>\n <head></head>\n <body>\n  <p>&amp; ==&gt; &amp;&quot; ==&gt; &quot;&gt; ==&gt; &gt;&lt; ==&gt; &lt;' =</p> \n </body>\n</html></div>";
		
		String actualResult = ServerMarkdownUtils.markdown2Html(testString, "http://mySynapse/attachment", new MarkdownProcessor());
		assertEquals(expectedResult, actualResult);
	}
}
