package org.sagebionetworks.web.server.markdownparser;

public class MarkdownRegExConstants {

	public static final String PREFIX_GROUP = "(^[> \t\n\f\r]*)";
	
	public static final String BLOCK_QUOTE_REGEX = "(^\\s*>\\s?(.+))";
	public static final String BOLD_REGEX = "(\\*\\*|__)(?=\\S)(.+?[*_]*)(?<=\\S)\\1";
	//exactly three '`', optionally followed by the language class to use
	public static final String FENCE_CODE_BLOCK_REGEX = PREFIX_GROUP + "[`]{3}\\s*([a-zA-Z_0-9-]*)\\s*$";
	public static final String CODE_SPAN_REGEX = "(?<!\\\\)(`+)(.+?)(?<!`)\\1(?!`)";
	public static final String HEADING_REGEX = PREFIX_GROUP + "(#{1,6})\\s*(.*)";
	public static final String HR_REGEX1 = "^[-]{3,}$";
	public static final String HR_REGEX2 = "^[*]{3,}$";
	public static final String TABLE_REGEX = ".*[|]{1}.+[|]{1}.*";
	public static final String IMAGE_REGEX = "!\\[(.*)\\]\\((.*)\\)";
	public static final String ITALICS_REGEX = "(\\*|_)(?=\\S)(.+?)(?<=\\S)\\1";
	public static final String LINK_REGEX =	"(\\[(.*?)\\]\\([ \\t]*<?(.*?)>?\\))";
	public static final String ORDERED_LIST_REGEX = "(^[>]*)(\\s*)((?:\\d+[.]))(.+)";
	public static final String UNORDERED_LIST_REGEX = "(^[>]*)(\\s*)((?:[-+*]))(.+)";
	
	

}
