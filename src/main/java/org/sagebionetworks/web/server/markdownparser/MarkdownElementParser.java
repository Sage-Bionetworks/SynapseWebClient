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
	
	/**
	 * Return true if this parser is still open (has not yet received the signal to close)
	 * @return
	 */
	boolean isInMarkdownElement();
	
	/**
	 * True if the output of this parser is already a block element (signals processor not add a newline if we're inside of this type of element)
	 * @return
	 */
	boolean isBlockElement();
}
