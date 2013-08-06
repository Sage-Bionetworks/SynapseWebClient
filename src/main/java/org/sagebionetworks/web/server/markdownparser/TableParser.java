package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class TableParser extends BasicMarkdownElementParser {
	Pattern start = Pattern.compile(MarkdownRegExConstants.TABLE_START_REGEX);
	Pattern p = Pattern.compile(MarkdownRegExConstants.TABLE_REGEX, Pattern.DOTALL);;
	Pattern end = Pattern.compile(MarkdownRegExConstants.TABLE_END_REGEX);
	boolean hasTags;
	boolean isInTable;
	boolean isTableStart;
	boolean isTableEnd;
	int tableCount;
	
	@Override
	public void reset() {
		hasTags = false;
		isInTable = false;
		isTableStart = false;
		isTableEnd = false;
		tableCount = 0;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String markdown = line.getMarkdown();
		
		boolean isTableMatch = p.matcher(markdown).matches();
		
		Matcher startMatcher = start.matcher(markdown);
		isTableStart = startMatcher.matches();
		
		isTableEnd = end.matcher(markdown).matches();
		
		StringBuilder builder = new StringBuilder();
		if(isTableStart) {
			hasTags = true;
			//get class styles and start table
			String styles = startMatcher.group(2);
			builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable");
			if(styles == null) {
				builder.append("\">");
			} else {
				builder.append(" " + styles + "\">");
			}
		}
		
		if(isTableMatch) {
			//are we starting a table?
			if (!isInTable) {
				isInTable = true;
				//create table if not already done by the css-applying tags
				if(!hasTags) {
					builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable\">");
				}
				//Create the header
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
			} else {
				//another row
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
			//not a table line. if table does not have surrounding tags, must be ending table
			if(isInTable && !hasTags) {
				line.prependElement("</tbody></table>");
				isInTable = false;
				builder.append(line.getMarkdown());
			}
			
			//if we are not in a table at all, just append original markdown
			if(!isInTable && !hasTags) {
				builder.append(line.getMarkdown());
			}
			
		}
		
		//if we see a tag in a tag-surrounded table, end the table
		if(isTableEnd && isInTable){
			isInTable = false;
			//reset to false since following tables may not have tags
			hasTags = false;
			//add end table
			builder.append("</tbody></table>");
			
		}
		line.updateMarkdown(builder.toString());
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInTable || hasTags;
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