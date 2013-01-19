package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAPICallWidgetViewImpl extends LayoutContainer implements SynapseAPICallWidgetView {

	private Presenter presenter;
	
	@Inject
	public SynapseAPICallWidgetViewImpl() {
	}
	
	@Override
	public void configure(Map<String, List<String>> columnData,
			String columnNames, String displayColumnNames, String rendererNames) {
		this.removeAll();
		if (columnData.size() > 0) {
			//define the column names
			String[] columnNamesArray;
			if (columnNames != null && columnNames.length() > 0) {
				columnNamesArray = columnNames.split(",");
			} else {
				int i = 0;
				columnNamesArray = new String[columnData.keySet().size()];
				for (Iterator<String> iterator = columnData.keySet().iterator(); iterator.hasNext();) {
					String columnName = iterator.next();
					columnNamesArray[i] = columnName;
					i++;
				}
			}
			String[] displayColumnNamesArray = columnNamesArray;
			if (displayColumnNames != null)
				displayColumnNamesArray = displayColumnNames.split(",");;
			
			StringBuilder builder = new StringBuilder();
			builder.append("<table>");
			//headers
			builder.append("<tr>");
			for (int i = 0; i < columnNamesArray.length; i++) {
				String columnName = displayColumnNamesArray[i];
				builder.append("<th>"+columnName+"</th>");
			}
			builder.append("</tr>");
			if (columnNamesArray.length > 0) {
				//find the row count (there has to be at least one key/value mapping)
				List<String> data = columnData.get(columnNamesArray[0]);
				if (data != null && data.size() > 0) {
					int rowCount = data.size();
					//now write out every row
					for (int i = 0; i < rowCount; i++) {
						builder.append("<tr>");
						for (int j = 0; j < columnNamesArray.length; j++) {
							String colName = columnNamesArray[j];
							List<String> colData = columnData.get(colName);
							String value = colData.get(i);
							builder.append("<td>"+value+"</td>");
						}
						builder.append("</tr>");
					}
				}
			}
			builder.append("</table>");
			add(new HTMLPanel(builder.toString()));
		}
		this.layout(true);
	}	
	
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	
	/*
	 * Private Methods
	 */

}
