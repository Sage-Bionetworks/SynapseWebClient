package org.sagebionetworks.web.unitserver.markdownparser;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.server.markdownparser.ImageParser;
import org.sagebionetworks.web.server.markdownparser.MarkdownElements;

public class ImageParserTest {
	ImageParser parser;
	
	@Before
	public void setup(){
		parser = new ImageParser();
	}
	
	@Test
	public void test(){
		String url = "http://test.com/a.png";
		String altText = "An Image";
		String line = "![" + altText+ "](" + url + ")";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getMarkdown();
		String encodedUrl = WidgetEncodingUtil.encodeValue(url);
		assertTrue(result.contains("${image?alt=An Image&fileName=" + encodedUrl + "&fromWeb=true}"));
	}
}
