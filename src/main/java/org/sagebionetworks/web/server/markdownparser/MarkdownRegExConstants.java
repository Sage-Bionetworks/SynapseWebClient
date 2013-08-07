package org.sagebionetworks.web.server.markdownparser;

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
	 * Recognized example input:
	 * > This is in
	 * > a blockquote.
	 * 
	 * Beginning of the line, optional whitespace, blockquote character '>', whitespace, then the text
	 */
	public static final String BLOCK_QUOTE_REGEX = "(^\\s*>\\s?(.*))";
	
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
	public static final String TABLE_REGEX = ".*[|]{1}.+[|]{1}.*";

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
	public static final String LINK_DOI = "(?<=\\s|^)(doi:([a-zA-Z_0-9./]+))";
	
	/**
	 * Recognized example input:
	 * syn12345
	 */
	public static final String LINK_SYNAPSE = "(?<=\\s|^)(syn\\d+)";
	
	/**
	 * Recognized example input:
	 * http://www.example.com/
	 */
	//from http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java 
	public static final String LINK_URL = "(?<=\\s|^)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
	
	/**
	 * Recognized example input:
	 * 1. First Item
	 */
	public static final String ORDERED_LIST_REGEX = "(^[>]*)(\\s+)(?:\\d+[.])(\\s+)(.+)";
	
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
	 * Recognized example input:
	 * * First Item
	 */
	public static final String UNORDERED_LIST_REGEX = "(^[>]*)(\\s*)(?:[-+*])(\\s+)(.+)";
	
	
	public static final String NEWLINE_REGEX = "([\n])";
	public static final String SPACE_REGEX = "([ ])";
	public static final String LT_REGEX = "(&lt;)";
	public static final String GT_REGEX = "(&gt;)";
}
