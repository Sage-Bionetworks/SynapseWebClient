package org.sagebionetworks.web.server.markdownparser;

import org.jsoup.nodes.Document;
import java.util.List;

/**
 * Class used to detect markdown elements (processing markdown to html).
 * If using a regular expression to detect, it's pattern should be compiled in init().
 * 
 * @author jayhodgson
 *
 */
public interface MarkdownElementParser {
	
	/**
	 * Called before document processing begins.  State should be cleared.
	 * @param simpleParsers TODO
	 */
	void reset(List<MarkdownElementParser> simpleParsers);

	/**
	 * Called on every line of the markdown document.
	 * @param line
	 * @return
	 */
	void processLine(MarkdownElements line);
	
	/**
	 * If there are any final modifications to the output html that the parser needs to make, it should perform it here (efficiently)
	 * @param html
	 */
	void completeParse(StringBuilder html);
	
	/**
	 * If there are any final modifications to the output html that the parser needs to make, it should perform it here (efficiently)
	 * @param html
	 */
	void completeParse(Document doc);
	
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
	
	/**
	 * True if element input is always represented on a single line
	 * @return
	 */
	boolean isInputSingleLine();
	
	/**
	 * True if it is the special parser that protects widget syntax
	 * @return
	 */
	boolean isSynapseMarkdownWidgetParser();
	
	void setIsPreview(boolean isPreview);
}
