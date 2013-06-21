package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class TableParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.TABLE_REGEX, Pattern.DOTALL);;
	boolean isInTable;
	int tableCount;
	
	@Override
	public void reset() {
		isInTable = false;
		tableCount = 0;
	}

	@Override
	public String processLine(String line) {
		boolean isTableMatch = p.matcher(line).matches();
		StringBuilder builder = new StringBuilder();
		if (isTableMatch) {
			//are we starting a table?
			if (!isInTable) {
				isInTable = true;
				//start table
				builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable\">");
				//this line is the header
				builder.append("<thead>");
				builder.append("<tr>");
				String[] cells = line.split("\\|");
				for (int j = 0; j < cells.length; j++) {
					builder.append("<th>");
					builder.append(cells[j]);
					builder.append("</th>");
				}
				builder.append("</tr>");
				builder.append("</thead>");
				builder.append("<tbody>");
				
				tableCount++;
			}
			else {
				//another row
				builder.append("<tr>");
				String[] cells = line.split("\\|");
				for (int j = 0; j < cells.length; j++) {
					builder.append("<td>");
					builder.append(cells[j]);
					builder.append("</td>");
				}
				builder.append("</tr>\n");
			}
		}
		else {
			//not a table line.  are we ending a table?
			if (isInTable) {
				//add end table
				builder.append("</tbody>");
				builder.append("</table>");
				isInTable = false;
			}
			builder.append(line);
		}
		return builder.toString();
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
