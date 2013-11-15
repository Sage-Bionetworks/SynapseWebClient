package org.sagebionetworks.web.unitserver;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.server.SynapseMarkdownProcessor;

public class SynapseMarkdownProcessorTest {

	SynapseMarkdownProcessor processor = SynapseMarkdownProcessor.getInstance();
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters() throws IOException{
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		String actualResult = processor.markdown2Html(testString, false, "");
		assertTrue(actualResult.contains("&amp;"));
		assertTrue(actualResult.contains("==&gt;"));
		assertTrue(actualResult.contains("&amp;&quot;"));
		assertTrue(actualResult.contains("==&gt;"));
		assertTrue(actualResult.contains("&quot;&gt;"));
		assertTrue(actualResult.contains("&lt;'"));
		assertTrue(actualResult.contains("="));
	}
	
	@Test
	public void testWhitespacePreservation() throws IOException{
		String codeBlock = " spaces  and\nnewline    -  test\n  preservation in preformatted  code blocks";
		String testString = "```\n"+codeBlock+"\n```";
		String actualResult = processor.markdown2Html(testString, false, "");
		//it should contain the code block, exactly as written
		assertTrue(actualResult.contains(codeBlock));
	}
	
	@Test
	public void testRemoveAllHTML() throws IOException{
		String testString = "<table><tr><td>this is a test</td><td>column 2</td></tr></table><iframe width=\"420\" height=\"315\" src=\"http://www.youtube.com/embed/AOjaQ7Vl7SM\" frameborder=\"0\" allowfullscreen></iframe><embed>";
		String actualResult = processor.markdown2Html(testString, false, "");
		assertTrue(!actualResult.contains("<table>"));
		assertTrue(!actualResult.contains("<iframe>"));
		assertTrue(!actualResult.contains("<embed>"));
	}
	
	@Test
	public void testRAssign() throws IOException{
		//testing R assignment operator (html stripping should not alter)
		String testString = "DemoClinicalOnlyModel <- setRefClass(Class  = \"CINModel\",...";
		String actualResult = processor.markdown2Html(testString, false, "");
		//there should be no space between the less than and the dash:
		assertTrue(actualResult.contains("&lt;-"));
	}
	
	@Test
	public void testTableSupport() throws IOException{
		String testString = 
				"|             |          Grouping           ||\nFirst Header  | Second Header | Third Header |\n ------------ | :-----------: | -----------: |\nContent       |          *Long Cell*        ||\nContent       |   **Cell**    |         Cell |\n";
		
		String actualResult = processor.markdown2Html(testString, false, "");
		assertTrue(actualResult.contains("<table"));
		assertTrue(actualResult.contains("<tr>"));
		assertTrue(actualResult.contains("<td>"));
	}
	
	@Test
	public void testListAndHeaderInBlockquote() throws IOException{
		//complicated integration test of all parsers
		String testString = 
			"> * Item 1\n" +
			"> * Item 2\n" +
			">   1. #### SubItem 2a\n" +
			">   2. SubItem 2b\n" +
			"> ``` r\n" +
			"> Then a code block!\n" +
			"> ```";
		String actualResult = processor.markdown2Html(testString, false, "");
		String expectedResult = "<blockquote><ul><li><p>Item 1</p></li>";
		String expectedResult2 = "<li><p> Item 2</p><ol start=\"1\"><li><p> </p><h4 id=\"synapseheading0\" level=\"h4\" toc-style=\"toc-indent0\">SubItem 2a</h4></li><li><p> SubItem 2b</p></li></ol></li></ul>";
		String expectedResult3 = "<pre><code class=\"r\"> Then a code block! </code></pre>";
		String expectedResult4 = "</blockquote>"; 
		assertTrue(actualResult.contains(expectedResult));
		assertTrue(actualResult.contains(expectedResult2));
		assertTrue(actualResult.contains(expectedResult3));
		assertTrue(actualResult.contains(expectedResult4));
		
		String testString2 =
			"1. First\n" +
			"2. Second\n" +
			"\n" +
			"> * Item 1\n" +
			"> * Item 2\n" +
			">   1. #### SubItem 2a\n" +
			">   2. SubItem 2b\n" +
			"> ``` r\n" +
			"> Then a code block!\n" +
			"> ```";

		String expectedResult5 = "<ol start=\"1\"><li><p>First</p></li><li><p>Second</p></li></ol><br />";
		String expectedResult6 = "<blockquote> <ul><li><p>Item 1</p></li>";
		String expectedResult7 = "<li><p>Item 2</p><ol start=\"1\"><li><p></p><h4 id=\"synapseheading0\" level=\"h4\" toc-style=\"toc-indent0\">SubItem 2a</h4></li><li><p>SubItem 2b</p></li></ol></li></ul>";
		String expectedResult8 = "<pre><code class=\"r\"> Then a code block! </code></pre><br />";
		String expectedResult9 = "</blockquote>";
		
		String actualResult2 = processor.markdown2Html(testString2, false, "");
		assertTrue(actualResult2.contains(expectedResult5));
		assertTrue(actualResult2.contains(expectedResult6));
		assertTrue(actualResult2.contains(expectedResult7));
		assertTrue(actualResult2.contains(expectedResult8));
		assertTrue(actualResult2.contains(expectedResult9));
	}
	
	@Test
	public void testTableThenHR() throws IOException{
		//complicated integration test of all parsers
		String testString = "Tau | MAPT | MCF7,BT20\nVASP | VASP | MCF7,BT20\nXIAP | XIAP | MCF7,BT20\n--------------------------------\n## Additional Data Details";
		String actualResult = processor.markdown2Html(testString, false, "");
		assertTrue(actualResult.contains("<hr"));
		assertFalse(actualResult.contains("<del"));
	}
	
	@Test
	public void testMarkdownInFencedCode() throws IOException{
		String markdown1 = "**bold**";
		String markdown2 = "_italicized_ ";
		String testString = "```java\nString s = \"should not be "+markdown1+"\";\n```\n"+markdown2;
		String actualResult = processor.markdown2Html(testString, false, "");
		//verify that it still contains raw markdown in the code block, but not raw markdown from outside the code block
		assertTrue(actualResult.contains(markdown1));
		assertFalse(actualResult.contains(markdown2));
	}
	
	@Test
	public void testHtmlStripping() throws IOException{
		String testString = "Configure s3cmd by executing\n`python s3cmd --configure s3://&lt;your_bucket_name&gt;`";
		String actualResult = processor.markdown2Html(testString, false, "");
		assertTrue(actualResult.contains("&lt;"));
		assertTrue(actualResult.contains("&gt;"));
		
	}
	
	@Test
	public void testSpaces() throws IOException{
		String testString = "The hi in t**hi**s is bold. No spaces in H~2~O.";
		String result = processor.markdown2Html(testString, false, "");
		assertTrue(result.contains("<strong>hi</strong>"));
		assertTrue(result.contains("No spaces in H<sub>2</sub>O."));
	}
}
