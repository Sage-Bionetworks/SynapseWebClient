package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class TableParser implements MarkdownElementParser {
	Pattern p;
	boolean isInTable;
	int tableCount;
	@Override
	public void init() {
		p = Pattern.compile(ServerMarkdownUtils.TABLE_REGEX);
	}

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
				builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter\">");
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
				builder.append("</tr>");
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
	public void completeParse(StringBuilder html) {
	}

}
