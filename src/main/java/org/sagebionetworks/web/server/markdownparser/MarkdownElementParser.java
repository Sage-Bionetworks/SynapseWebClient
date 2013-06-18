package org.sagebionetworks.web.server.markdownparser;

/**
 * Class used to detect markdown elements (processing markdown to html).
 * If using a regular expression to detect, it's pattern should be compiled in init().
 * 
 * @author jayhodgson
 *
 */
public interface MarkdownElementParser {
	
	void init();
	
	/**
	 * Called before document processing begins.  State should be cleared.
	 */
	void reset();

	/**
	 * Called on every line of the markdown document.
	 * @param line
	 * @return
	 */
	String processLine(String line);
	
	/**
	 * If there are any final modifications to the output html that the parser needs to make, it should perform it here (efficiently)
	 * @param html
	 */
	void completeParse(StringBuilder html);
}
