package org.sagebionetworks.web.server.markdownparser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class TableParser extends BasicMarkdownElementParser {
	Pattern start = Pattern.compile(MarkdownRegExConstants.TABLE_START_REGEX);
	Pattern p = Pattern.compile(MarkdownRegExConstants.TABLE_REGEX, Pattern.DOTALL);;
	Pattern end = Pattern.compile(MarkdownRegExConstants.TABLE_END_REGEX);
	Pattern headerBorder = Pattern.compile(MarkdownRegExConstants.TABLE_HEADER_BORDER_REGEX);
	boolean hasTags; 			//Are we creating a fenced table versus a table with just column separated by pipes
	boolean shortStyle;			//Does this table need to limit its height
	boolean isInTable;			//Have we started/are we in the middle of creating a table
	boolean isTableStart;		//Is this the opening fence of the table
	boolean isTableEnd;			//Is this the closing end of the table
	boolean readFirstRow;		//Have we read the first row/should we check if this is a header row
	boolean hasHandledFirstRow;
	int tableCount;				//Table ID
	ArrayList<String> firstRowData;	//Stores row/cells data
	
	@Override
	public void reset() {
		hasTags = false;
		shortStyle = false;
		isInTable = false;
		isTableStart = false;
		isTableEnd = false;
		readFirstRow = false;
		hasHandledFirstRow = false;
		tableCount = 0;
		firstRowData = new ArrayList<String>();
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
			//Get class styles and start table
			String styles = startMatcher.group(2);
			if(styles != null && styles.contains("short")) {
				//Wrap table in a container that will limit height and maintain good format
				builder.append("<div class=\"markdowntableWrap\">");
				shortStyle = true;
			}
			builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable");
			if(styles == null) {
				builder.append("\">");
			} else {
				builder.append(" " + styles + "\">");
			}
		} else if(isTableEnd) {
			//End table and reset state for future tables
			builder.append("</tbody></table>");
			if(shortStyle) {
				builder.append("</div>");
			}
			resetTableState();
		} else {
			//If we are not in a fenced table, check if this is a normal table
			if(!hasTags) { 
				isInTable = p.matcher(markdown).matches();
			}
		
			if(isInTable) {
				if(!readFirstRow) {
					if(firstRowData.isEmpty()) {
						//This is the first time you've entered the table
						if(!hasTags) {
							//Create table if not already done when tags were found
							builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable\">");
						}
						//Store the first row's cells
						firstRowData = getRowData(markdown);
						readFirstRow = true;
						tableCount++;
					}
				} else {
					if(!hasHandledFirstRow) {
						//We've read the first row and need to check if it's a header row
						createFirstRow(markdown, builder);
						hasHandledFirstRow = true;
					} else {
						//Create a normal row for every row after the first
						createTableRow(builder, markdown);
					}
				}
			} else {
				//Not a table line
				if(!firstRowData.isEmpty()) {
					//We were creating a table; finish the table		
					if(!hasHandledFirstRow) {
						//The first row must not be a header because no border syntax was found before the end of the table
						line.prependElement("<tr>");
						for (int j = 0; j < firstRowData.size(); j++) {
							line.prependElement("<td>");
							line.prependElement(firstRowData.get(j));
							line.prependElement("</td>");
						}
						line.prependElement("</tr>\n");
					}
					//End table and reset state for future tables
					line.prependElement("</tbody></table>");
					if(shortStyle) {
						line.prependElement("</div>");
					}
					builder.append(line.getMarkdown());
					resetTableState();
				} else {
					//We are not in a table at all, just append the original markdown
					builder.append(line.getMarkdown());
				}
			}
		}
		line.updateMarkdown(builder.toString());
	}
	
	private void createFirstRow(String markdown, StringBuilder builder) {
		if(isHeaderBorder(markdown)) {
			//If current markdown is a header row border, make the header with stored data
			builder.append("<thead>");
			builder.append("<tr>");
			for (int j = 0; j < firstRowData.size(); j++) {
				builder.append("<th>");
				builder.append(firstRowData.get(j));
				builder.append("</th>");
			}
			builder.append("</tr>");
			builder.append("</thead>");
			builder.append("<tbody>");
		} else {
			//Create a normal row with the stored data
			builder.append("<tbody>");
			builder.append("<tr>");
			for (int j = 0; j < firstRowData.size(); j++) {
				builder.append("<td>");
				builder.append(firstRowData.get(j));
				builder.append("</td>");
			}
			builder.append("</tr>\n");
			//Create a normal row for the current row since it's not a border 
			createTableRow(builder, markdown);
		}
	}
	
	private void resetTableState() {
		isInTable = false;
		hasTags = false;
		shortStyle = false;
		readFirstRow = false;
		hasHandledFirstRow = false;
		firstRowData.clear();
	}

	private ArrayList<String> getRowData(String markdown) {
		ArrayList<String> rowData = new ArrayList<String>();
		String[] cells = markdown.split("\\|");
		for (int j = 0; j < cells.length; j++) {
			rowData.add(cells[j]);
		}
		return rowData;
	}
	
	private void createTableRow(StringBuilder builder, String markdown) {
		String[] cells = markdown.split("\\|");
		builder.append("<tr>");
		for (int j = 0; j < cells.length; j++) {
			builder.append("<td>");
			builder.append(cells[j]);
			builder.append("</td>");
		}
		builder.append("</tr>\n");
	}
	
	private boolean isHeaderBorder(String markdown) {
		Matcher m;
		String[] cells = markdown.split("\\|");
		for (int j = 0; j < cells.length; j++) {
			m = headerBorder.matcher(cells[j]);
			if(!m.matches()) {
				return false;
			}
		}
		return true;
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