package org.sagebionetworks.web.server.markdownparser;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.ServerMarkdownUtils;

/**
 * (?=pattern) is a positive look-ahead assertion - the pattern must succeed
 * (?<=pattern) is a positive look-behind assertion - the pattern must succeed
 *
 */
public class MarkdownRegExConstants {

	/**
	 * Beginning of the line, and the blockquote or whitespace characters zero or more times
	 */
	public static final String PREFIX_GROUP = "(^[> \t\n\f\r]*)";
	
	/**
	 * Recognizes example input:
	 * \`
	 */
	public static final String BACKTICK_ESCAPED_REGEX = "\\\\`";
	
	/**
	 * Recognized example input:
	 * > This is in
	 * > a blockquote.
	 * 
	 * Beginning of the line, optional whitespace, blockquote character '>', whitespace, then the text
	 */
	public static final String BLOCK_QUOTE_REGEX = "(^\\s*(>(\\s?))(.*))";
	
	/**
	 * Recognizes example input:
	 * __This__ and **that** are bold
	 */
	public static final String BOLD_REGEX = "(\\*\\*|__)(?=\\S*)(.+?[*_]*)(?<=\\S*)\\1";
	
	/**
	 * Recognizes example input:
	 * ${bookmarktarget?bookmarkID=subject1} Subject 1
	 */
	public static final String BOOKMARK_TARGET_REGEX = "\\$\\{bookmarktarget\\?bookmarkID=(.+?)\\}";
	
	/**
	 * Recognized example input:
	 * ``` <optional language>
	 * This is a code block
	 * ```
	 */
	public static final String FENCE_CODE_BLOCK_REGEX = PREFIX_GROUP + "[`]{3}\\s*([a-zA-Z_0-9-]*)\\s*$";
	
	/**
	 * Recognized example input:
	 * $$
	 * This is a math block
	 * $$
	 */
	public static final String FENCE_MATH_BLOCK_REGEX = PREFIX_GROUP + "[$]{2}\\s*$";
	
	/**
	 * Recognized example input:
	 * A math span look like $this$
	 */
	public static final String MATH_SPAN_REGEX = "(?<!\\\\)(\\$+)([^{].+?)(?<!\\$)\\1(?!\\$)";
	

	/**
	 * Recognized example input:
	 * ``` or <pre><code or </code></pre>
	 */
	public static final String HTML_FENCE_CODE_BLOCK_REGEX = FENCE_CODE_BLOCK_REGEX + "|" + ServerMarkdownUtils.START_PRE_CODE + "|" + ServerMarkdownUtils.END_PRE_CODE;
	
	/**
	 * Recognized example input:
	 * A code span look like `this`
	 */
	public static final String CODE_SPAN_REGEX = "(?<!\\\\)(`+)(.+?)(?<!`)\\1(?!`)";
	
	/**
	 * Recognized example input:
	 * ### A h3 Heading
	 */
	public static final String HEADING_REGEX = PREFIX_GROUP + "(#{1,6})\\s*(.*)";

	/**
	 * Recognized example input:
	 * -----
	 */
	public static final String HR_REGEX1 = "^[-]{3,}$";
	
	/**
	 * Recognized example input:
	 * *****
	 */
	public static final String HR_REGEX2 = "^[*]{3,}$";
	
	
	/**
	 * Recognized example input:
	 * Column 1 | Column 2 | Column 3
	 */
	public static final String TABLE_REGEX = "(.+[|]{1}.*)+";

	/**
	 * Recognized example input:
	 * - , --, ---, -:, :-. -:- etc.
	 */
	public static final String TABLE_HEADER_BORDER_REGEX = "([\\s-:|]+[|]{1}[\\s-:|]*)+";

	/**
	 * Recognized example input:
	 * {| class="border"
	 * or
	 * {|
	 */
	public static final String TABLE_START_REGEX = "(\\{\\|\\s*class=\"(.*?)\"\\s*)|(\\{\\|\\s*)";
	
	/**
	 * Recognized example input:
	 * |}
	 */
	public static final String TABLE_END_REGEX = "\\s*\\|\\}\\s*";
	
	/**
	 * Recognized example input:
	 * ![An Example](http://www.example.com/a.png)
	 */
	public static final String IMAGE_REGEX = "!\\[(.*)\\]\\((.*)\\)";
	
	/**
	 * Recognized example input:
	 * No starting whitespace or
	 * 		Starting whitespace
	 */
	public static final String INDENTED_REGEX = PREFIX_GROUP + "(.*)";
	
	/**
	 * Recognized example input:
	 * *This* and _that_ will be italicized
	 */
	public static final String ITALICS_REGEX = "(\\*|_)(?=\\S*)(.+?)(?<=\\S*)\\1";
	
	/**
	 * Recognized example input:
	 * [An Example](http://www.example.com/)
	 */
	public static final String LINK_REGEX =	"(\\[(.*?)\\]\\([ \\t]*<?(.*?)>?\\))";
	
	/**
	 * Recognized example input:
	 * doi:10.1234
	 */
	public static final String LINK_DOI = "(?<=[\\W&&[^:]]|^)(doi:([a-zA-Z_0-9./]+))";
	
	/**
	 * Recognized example input:
	 * syn12345
	 */
	public static final String LINK_SYNAPSE = "(?<=[\\W&&[^:]]|^)(syn\\d+)";
	
	/**
	 * Recognized example input:
	 * http: or ftp: or file:
	 */
	public static final String LINK_URL_PROTOCOL = "(https?|ftp|file):";
	
	/**
	 * from http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java 
	 * Recognized example input:
	 * http://www.example.com/
	 */
	public static final String LINK_URL = "(?<=[\\W&&[^\"]]|^)(" + LINK_URL_PROTOCOL + "//[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	
	/**
	 * Recognized example input:
	 * 1. First Item
	 */
	public static final String ORDERED_LIST_REGEX = PREFIX_GROUP + "(\\d+)[.](\\s+)(.+)";
	
	/**
	 * Recognized example input:
	 * ${reference?text=So H et al&inlineWidget=true}
	 */
	public static final String REFERENCE_REGEX = "\\$\\{reference\\?(text=|inlineWidget=)(.+?)&(text=|inlineWidget=)(.+?)\\}";
	
	/**
	 * Recognized example input:
	 * --striked out text--
	 */
	public static final String STRIKE_OUT_REGEX = "--(?=\\S*)(.+?)(?<=\\S*)--";
	
	/**
	 * Recognized example input:
	 * ~sub~ is a subscript
	 */
	public static final String SUBSCRIPT_REGEX = "~(?=\\S*)(.+?)(?<=\\S*)~";
	
	/**
	 * Recognized example input:
	 * ^sup^ is a superscript
	 */
	public static final String SUPERSCRIPT_REGEX = "(\\^)(?=\\S*)(.+?)(?<=\\S*)(\\^)";
	
	/**
	 * Recognizes example input:
	 * ${contentType?widgetParams=values}
	 */
	public static final String SYNAPSE_MARKDOWN_WIDGET_REGEX = "(" + WidgetConstants.WIDGET_START_MARKDOWN_ESCAPED + "([^\\}]*)" + WidgetConstants.WIDGET_END_MARKDOWN_ESCAPED + ")"; 
	
	/**
	 * Recognizes example input:
	 * \~
	 */
	public static final String TILDE_ESCAPED_REGEX = "\\\\~";
	
	/**
	 * Recognizes example input:
	 * \_
	 */
	public static final String UNDERSCORE_ESCAPED_REGEX = "\\\\_";
	
	/**
	 * Recognized example input:
	 * * First Item
	 */
	public static final String UNORDERED_LIST_REGEX = PREFIX_GROUP + "([-+*])(\\s+)(.+)";
	
	
	public static final String NEWLINE_REGEX = "([\n])";
	public static final String SPACE_REGEX = "([ ])";
	public static final String LT_REGEX = "(&lt;)";
	public static final String GT_REGEX = "(&gt;)";
}
