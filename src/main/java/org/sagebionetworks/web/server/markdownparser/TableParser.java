package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class TableParser extends BasicMarkdownElementParser {
	Pattern start = Pattern.compile(MarkdownRegExConstants.TABLE_START_REGEX);
	Pattern p = Pattern.compile(MarkdownRegExConstants.TABLE_REGEX, Pattern.DOTALL);;
	Pattern end = Pattern.compile(MarkdownRegExConstants.TABLE_END_REGEX);
	boolean hasTags; 		//Are we creating a fenced table versus a table with just column separated by pipes
	boolean isInTable;		//Have we started/are we in the middle of creating a table
	boolean isTableStart;	//Is this the opening fence of the table
	boolean isTableEnd;		//Is this the closing end of the table
	boolean headColMade;	//To determine what type of row to create
	boolean isTableMatch;	//Is the line a part of a table
	int tableCount;			//Table ID
	
	@Override
	public void reset() {
		hasTags = false;
		isInTable = false;
		isTableStart = false;
		isTableEnd = false;
		headColMade = false;
		isTableMatch = false;
		tableCount = 0;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String markdown = line.getMarkdown();
		
		Matcher startMatcher = start.matcher(markdown);
		isTableStart = startMatcher.matches();
		isTableEnd = end.matcher(markdown).matches();
		StringBuilder builder = new StringBuilder();
		
		if(isTableStart) {
			hasTags = true;
			isInTable = true;
			isTableMatch = true;
			//get class styles and start table
			String styles = startMatcher.group(2);
			builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable");
			if(styles == null) {
				builder.append("\">");
			} else {
				builder.append(" " + styles + "\">");
			}
		} else if(isTableEnd) {
			//we've reached the end; reset to false
			isInTable = false;
			hasTags = false;
			isTableMatch = false;
			headColMade = false;
			builder.append("</tbody></table>");
		} else {
			//If we are not in a fenced table, check if this is a normal table
			if(!hasTags) { 
				isTableMatch = p.matcher(markdown).matches();
				if(isTableMatch) {
					//We've begun to create a table
					isInTable = true;
				}
			}
		
			if(isTableMatch) {
				//Create header if not already made
				if(!headColMade) {
					if(!hasTags) {
						builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable\">");
					}
					builder.append("<thead>");
					builder.append("<tr>");
					String[] cells = markdown.split("\\|");
					for (int j = 0; j < cells.length; j++) {
						builder.append("<th>");
						builder.append(cells[j]);
						builder.append("</th>");
					}
					builder.append("</tr>");
					builder.append("</thead>");
					builder.append("<tbody>");
					tableCount++;
					headColMade = true;
				} else {
					builder.append("<tr>");
					String[] cells = markdown.split("\\|");
					for (int j = 0; j < cells.length; j++) {
						builder.append("<td>");
						builder.append(cells[j]);
						builder.append("</td>");
					}
					builder.append("</tr>\n");
				}
			} else {
				//Not a table line; if we're in a table without surrounding tags, we must have reached the end
				if(!hasTags && isInTable) {
					//we reached the end; reset to false;
					isInTable = false;
					headColMade = false;
					line.prependElement("</tbody></table>");
					builder.append(line.getMarkdown());
				}
				
				//if we are not in a table at all, just append the original markdown
				if(!isInTable && !hasTags) {
					builder.append(line.getMarkdown());
				}
			}
		}
		line.updateMarkdown(builder.toString());
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInTable;
	}

	@Override
	public boolean isBlockElement() {
		return true;
	}

	@Override
	public boolean isInputSingleLine() {
		return false;
	}

}