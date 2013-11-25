package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.server.markdownparser.MarkdownExtractor;

public class ServerMarkdownUtilsTest {
	
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
	
	@Test
	public void testInsertExtractedContent(){
		String containerId1="containerId1";
		String containerId2="containerId2";
		String containerId2Content="These are the droids we are looking for.";
		MarkdownExtractor extractor = new MarkdownExtractor();
		extractor.putContainerIdToContent(containerId1, "<span id=\""+containerId2+"\"></span>");
		String testString = "<p>Line of widgets: <span id=\""+containerId1+"\"></span></p>";
		Document htmlDoc = Jsoup.parse(testString);
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, htmlDoc, true);
		
		//containerId1 content should have been extracted
		assertTrue(extractor.getContainerIds().isEmpty());
		
		extractor.putContainerIdToContent(containerId2, containerId2Content);
		
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, htmlDoc, true);
		//html should now contain the container Id 2 content
		assertTrue(extractor.getContainerIds().isEmpty());
		assertTrue(htmlDoc.html().contains(containerId2Content));
		
		//can run more than once. showing with a container id element that is not present in the html
		String containerIdElementNotFound="containerIdElementNotFound";
		extractor.putContainerIdToContent(containerIdElementNotFound, "<h1>Html never inserted</h1>");
		String beforeHtml = htmlDoc.html();
		Set beforeSet = new HashSet();
		beforeSet.addAll(extractor.getContainerIds());
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, htmlDoc, true);
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, htmlDoc, true);
		
		assertEquals(beforeHtml, htmlDoc.html());
		assertEquals(beforeSet, extractor.getContainerIds());
	}
	
	@Test
	public void testGetStartLink(){
		String startLink;
		//verify it's the current window if either the client host string or the href is null or blank
		startLink = ServerMarkdownUtils.getStartLink(null, "http://www.jayhodgson.com");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		startLink = ServerMarkdownUtils.getStartLink("", "http://www.jayhodgson.com");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", null);
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", "");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		
		//verify current window if href starts with the client host string
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", "https://www.synapse.org/#!Challenges:DREAM");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		//ignore the case they supply for the link
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", "https://WWW.SYNAPSE.ORG/#!Challenges:DREAM");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		
		
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", "#!Challenges:DREAM");
		assertEquals(ServerMarkdownUtils.START_LINK_CURRENT_WINDOW, startLink);
		
		//new window if different
		startLink = ServerMarkdownUtils.getStartLink("https://www.synapse.org", "https://www.jayhodgson.com/#!Challenges:DREAM");
		assertEquals(ServerMarkdownUtils.START_LINK_NEW_WINDOW, startLink);
	}
	
	
}
