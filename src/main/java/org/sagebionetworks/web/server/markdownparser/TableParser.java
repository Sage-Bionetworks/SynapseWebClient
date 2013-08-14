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
	boolean isInTable;			//Have we started/are we in the middle of creating a table
	boolean isTableStart;		//Is this the opening fence of the table
	boolean isTableEnd;			//Is this the closing end of the table
	boolean readFirstRow;		//Have we read the first row/should we check if this is a header row
	int tableCount;				//Table ID
	ArrayList<String> rowData;	//Stores row/cells data
	
	@Override
	public void reset() {
		hasTags = false;
		isInTable = false;
		isTableStart = false;
		isTableEnd = false;
		readFirstRow = false;
		tableCount = 0;
		rowData = new ArrayList<String>();
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
			builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable");
			if(styles == null) {
				builder.append("\">");
			} else {
				builder.append(" " + styles + "\">");
			}
		} else if(isTableEnd) {
			//Add last row to the table if it's not a header row border
			String rowDataString = createRowDataString();
			if(!isHeaderBorder(rowDataString)) {
				createTableRow(builder);
			}
			builder.append("</tbody></table>");
			//Reset to false for future tables
			isInTable = false;
			hasTags = false;
			readFirstRow = false;
			rowData.clear();
		} else {
			//If we are not in a fenced table, check if this is a normal table
			if(!hasTags) { 
				isInTable = p.matcher(markdown).matches();
			}
		
			if(isInTable) {
				if(!readFirstRow) {
					if(rowData.isEmpty()) {
						//This is the first time you've entered the table
						if(!hasTags) {
							//Create table if not already done when tags were found
							builder.append("<table id=\""+WidgetConstants.MARKDOWN_TABLE_ID_PREFIX+tableCount+"\" class=\"tablesorter markdowntable\">");
						}
						//Store the first row's cells
						storeRowData(markdown);
						readFirstRow = true;
						tableCount++;
					} else {
						//We have stored data from reading the previous row
						//Create a row for this data unless it is the header row's border
						String rowDataString = createRowDataString();
						if(!isHeaderBorder(rowDataString)) {
							createTableRow(builder);
						}
					}
				} else {
					//We've read the first row and need to check if it's a header row
					if(isHeaderBorder(markdown)) {
						//If this line is a header row border, make the header with the stored data
						builder.append("<thead>");
						builder.append("<tr>");
						for (int j = 0; j < rowData.size(); j++) {
							builder.append("<th>");
							builder.append(rowData.get(j));
							builder.append("</th>");
						}
						builder.append("</tr>");
						builder.append("</thead>");
						builder.append("<tbody>");
						rowData.clear();
					} else {
						//Create a normal row with the stored data
						builder.append("<tbody>");
						createTableRow(builder);
					}
					readFirstRow = false;
				}
				
				//Store this row's data 
				storeRowData(markdown);
			} else {
				//Not a table line
				if(!rowData.isEmpty()) {
					//If we are creating a table, add the last row of it's not a header row border
					String rowDataString = createRowDataString();
					if(!isHeaderBorder(rowDataString)) {
						line.prependElement("<tr>");
						for (int j = 0; j < rowData.size(); j++) {
							line.prependElement("<td>");
							line.prependElement(rowData.get(j));
							line.prependElement("</td>");
						}
						line.prependElement("</tr>\n");
						line.prependElement("</tbody></table>");
						builder.append(line.getMarkdown());
					}
					
					//Reset to false for future tables
					isInTable = false;
					readFirstRow = false;
					rowData.clear();
				} else {
					//We are not in a table at all, just append the original markdown
					builder.append(line.getMarkdown());
				}
			}
		}
		line.updateMarkdown(builder.toString());
	}

	private String createRowDataString() {
		StringBuilder rowDataString = new StringBuilder();
		for(int i = 0; i < rowData.size() - 1; i++) {
			rowDataString.append(rowData.get(i) + "|");
		}
		rowDataString.append(rowData.get(rowData.size()-1));
		return rowDataString.toString();
	}
	
	private void storeRowData(String markdown) {
		rowData.clear();
		String[] cells = markdown.split("\\|");
		for (int j = 0; j < cells.length; j++) {
			rowData.add(cells[j]);
		}
	}
	
	private void createTableRow(StringBuilder builder) {
		builder.append("<tr>");
		for (int j = 0; j < rowData.size(); j++) {
			builder.append("<td>");
			builder.append(rowData.get(j));
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