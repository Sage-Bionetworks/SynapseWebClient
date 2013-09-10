package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidgetViewImpl extends LayoutContainer implements APITableWidgetView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public APITableWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void clear() {
		removeAll(true);
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
			
			//apply tablesorter style
			builder.append(" class=\"inline-block scroll-x tablesorter\"");
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
							Map<String, List<String>> outputColumnData = renderers[j].getColumnData();
		
							for (Iterator<String> iterator = rendererColumnNames.iterator(); iterator.hasNext();) {
								String columnName = iterator.next();
								String value;
								//Check that this renderer has initialized data for this column
								if(outputColumnData.get(columnName) == null) {
									value = "";
								} else {
									value = outputColumnData.get(columnName).get(i);
								}
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
			layout(true);
			//do not apply sorter if paging (service needs to be involved for a true column sort)
			if(!tableConfig.isPaging()) {
				synapseJSNIUtils.tablesorter(elementId);
			}
		}
	}	
	
	@Override
	public void configurePager(int start, int end, int total) {
		UnorderedListPanel panel = new UnorderedListPanel();
		panel.setStyleName("pager");
		Label label = new Label(start + "-" + end + " of " + total);
		
		Anchor prev = new Anchor();
		prev.setHTML("Previous");
		prev.addStyleName("link");
		prev.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageBack();
			}
		});
		
		Anchor next = new Anchor();
		next.setHTML("Next");
		next.addStyleName("link");
		next.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.pageForward();
			}
		});
		
		if (start == 1) {
			panel.add(prev, "disabled");
		} else {
			panel.add(prev);
			DisplayUtils.addTooltip(this.synapseJSNIUtils, prev, DisplayConstants.PAGE_BACK, TOOLTIP_POSITION.BOTTOM);
		}
		panel.add(label, "pagerLabel");
		if(end == total) {
			panel.add(next, "disabled");
		} else {
			panel.add(next);
			DisplayUtils.addTooltip(this.synapseJSNIUtils, next, DisplayConstants.PAGE_BACK, TOOLTIP_POSITION.BOTTOM);
		}
		add(panel);
		layout(true);
	}
	
	@Override
	public void showError(String message) {
		removeAll();
		String errorMessage = DisplayUtils.getIconHtml(iconsImageBundle.error16()) + message;
		add(new HTMLPanel(DisplayUtils.getMarkdownAPITableWarningHtml(errorMessage)));	
		layout(true);
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	/*
	 * Private Methods
	 */

}
