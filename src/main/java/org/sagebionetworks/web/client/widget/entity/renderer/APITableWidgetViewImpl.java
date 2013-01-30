package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidgetViewImpl extends FlowPanel implements APITableWidgetView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public APITableWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void configure(java.util.Map<String,java.util.List<String>> columnData, String[] columnNames, String[] displayColumnNames, APITableInitializedColumnRenderer[] renderers, String tableWidth, boolean showRowNumbers, String rowNumberColName, String cssStyleName, int offset) {
		removeAll();
		if (columnData.size() > 0) {
			StringBuilder builder = new StringBuilder();
			boolean isCssStyled = cssStyleName != null &&  cssStyleName.length() > 0;
			//if it's css styled, then wrap it in a span so that the style is as specific as the markdown css style (and should "win")
			if (isCssStyled)
				builder.append("<span class=\"" + cssStyleName + "\">");
			builder.append("<table");
			if (tableWidth != null)
				builder.append(" style=\"width:"+tableWidth+"\"");
			builder.append(">");
				
			//headers
			builder.append("<tr>");
			if (showRowNumbers)
				builder.append("<th>"+rowNumberColName+"</th>");	//row number
			for (int i = 0; i < displayColumnNames.length; i++) {
				String columnName = displayColumnNames[i];
				//create renderer columns
				int rendererColCount = renderers[i].getColumnCount();
				for (int j = 0; j < rendererColCount; j++) {
					String rendererColumnName = renderers[i].getRenderedColumnName(j);
					String outputColName = rendererColumnName == null ? columnName : rendererColumnName;
					builder.append("<th>"+outputColName+"</th>");	
				}
			}
			builder.append("</tr>");
			if (columnNames.length > 0) {
				//find the row count (there has to be at least one key/value mapping)
				List<String> data = columnData.get(columnNames[0]);
				if (data != null && data.size() > 0) {
					int rowCount = data.size();
					//now write out every row
					for (int i = 0; i < rowCount; i++) {
						builder.append("<tr>");
						if (showRowNumbers)
							builder.append("<td>"+(i+offset+1)+"</td>"); //row number
						for (int j = 0; j < columnNames.length; j++) {
							String colName = columnNames[j];
							List<String> colData = columnData.get(colName);
							if (colData != null) {
								String value = colData.get(i);
								int rendererColCount = renderers[j].getColumnCount();
								//now render each column
								for (int k = 0; k < rendererColCount; k++) {
									builder.append("<td>"+renderers[j].render(value,k)+"</td>");	
								}
							}
							else
								builder.append("<td></td>");
						}
						builder.append("</tr>");
					}
				}
			}
			builder.append("</table>");
			if (isCssStyled)
				builder.append("</span>");

			add(new HTMLPanel(builder.toString()));
		}
	}	
	
	@Override
	public void configurePager(int start, int end, int total) {
		FlowPanel hp = new FlowPanel();
		hp.addStyleName("margin-bottom-40");
		hp.addStyleName("margin-top-10");
		Image pageBack = new Image(iconsImageBundle.NavigateLeft16());
		pageBack.addStyleName("imageButton");
		pageBack.addStyleName("left");
		pageBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageBack();
			}
		});
		DisplayUtils.addTooltip(this.synapseJSNIUtils, pageBack, DisplayConstants.PAGE_BACK, TOOLTIP_POSITION.BOTTOM);
	 	
		Image pageForward = new Image(iconsImageBundle.NavigateRight16());
		pageForward.addStyleName("imageButton");
		pageForward.addStyleName("left");
		pageForward.addStyleName("margin-left-5");
		pageForward.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageForward();
			}
		});
		DisplayUtils.addTooltip(this.synapseJSNIUtils, pageForward, DisplayConstants.PAGE_NEXT, TOOLTIP_POSITION.BOTTOM);
	 	
		Label label = new Label(start + "-" + end + " of " + total);
		label.addStyleName("left");
		label.addStyleName("margin-left-5");
		
		if (start != 1)
			hp.add(pageBack);
		hp.add(label);
		if (end != total)
			hp.add(pageForward);
//		pageBack.setEnabled(start > 1);
//		pageForward.setEnabled(end < total);
		add(hp);
	}
	
	@Override
	public void showError(String message) {
		removeAll();
		add(new HTMLPanel(DisplayUtils.getIconHtml(iconsImageBundle.error16()) + message));	
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	private void removeAll() {
		while(this.getWidgetCount() > 0)
			this.remove(0);
	}
	
	/*
	 * Private Methods
	 */

}
