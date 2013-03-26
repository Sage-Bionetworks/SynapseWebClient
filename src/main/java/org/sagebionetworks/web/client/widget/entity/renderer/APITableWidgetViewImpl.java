package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;

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
	public void configure(Map<String,List<String>> columnData, String[] columnNames, APITableInitializedColumnRenderer[] renderers, APITableConfig tableConfig) {
		removeAll();
		if (columnData.size() > 0) {
			String elementId = HTMLPanel.createUniqueId();
			StringBuilder builder = new StringBuilder();
			boolean isCssStyled = tableConfig.getCssStyleName() != null &&  tableConfig.getCssStyleName().length() > 0;
			//if it's css styled, then wrap it in a span so that the style is as specific as the markdown css style (and should "win")
			if (isCssStyled)
				builder.append("<span class=\"" + tableConfig.getCssStyleName() + "\">");
			
			builder.append("<table id=\""+elementId+"\"");
			
			//do not apply sorter if paging (service needs to be involved for a true column sort)
			if (!tableConfig.isPaging()) {
				builder.append(" class=\"tablesorter\"");
			}
			if (tableConfig.getTableWidth() != null)
				builder.append(" style=\"width:"+tableConfig.getTableWidth()+"\"");
			builder.append(">");
				
			//headers
			builder.append("<thead><tr>");
			if (tableConfig.isShowRowNumber())
				builder.append("<th>"+tableConfig.getRowNumberColName()+"</th>");	//row number
			
			//for each renderer, ask for it's list of columns that are being output
			for (int i = 0; i < renderers.length; i++) {
				List<String> rendererColumnNames = renderers[i].getColumnNames();
				for (Iterator<String> iterator = rendererColumnNames.iterator(); iterator.hasNext();) {
					String columnName = iterator.next();
					builder.append("<th>"+columnName+"</th>");					
				}
			}
			builder.append("</tr></thead><tbody>");
			if (columnNames.length > 0) {
				//find the row count (there has to be at least one key/value mapping)
				List<String> data = columnData.get(columnNames[0]);
				if (data != null && data.size() > 0) {
					int rowCount = data.size();
					//now write out every row
					for (int i = 0; i < rowCount; i++) {
						builder.append("<tr>");
						if (tableConfig.isShowRowNumber())
							builder.append("<td>"+(i+tableConfig.getOffset()+1)+"</td>"); //row number
						
						//column data (each renderer has a list of columns that it will output.  ask for each value
						for (int j = 0; j < renderers.length; j++) {
							List<String> rendererColumnNames = renderers[j].getColumnNames();
							for (Iterator<String> iterator = rendererColumnNames.iterator(); iterator.hasNext();) {
								String columnName = iterator.next();
								String value = renderers[j].getColumnData().get(columnName).get(i);
								builder.append("<td>"+value+"</td>");
							}
						}
						
						builder.append("</tr>");
					}
				}
			}
			builder.append("</tbody></table>");
			if (isCssStyled)
				builder.append("</span>");

			add(new HTMLPanel(builder.toString()));
			synapseJSNIUtils.tablesorter(elementId);
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
