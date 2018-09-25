package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.TableState;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.entity.ElementWrapper;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.table.TimedRetryWidget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidgetViewImpl extends FlowPanel implements APITableWidgetView {

	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	HTMLPanel panel;
	
	@Inject
	public APITableWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	@Override
	public void configure(Map<String,List<String>> columnData, String[] columnNames, APITableInitializedColumnRenderer[] renderers, APITableConfig tableConfig) {
		clear();
		String elementId = HTMLPanel.createUniqueId();
		StringBuilder builder = new StringBuilder();
		boolean isCssStyled = tableConfig.getCssStyleName() != null &&  tableConfig.getCssStyleName().length() > 0;
		//if it's css styled, then wrap it in a span so that the style is as specific as the markdown css style (and should "win")
		if (isCssStyled)
			builder.append("<span class=\"" + tableConfig.getCssStyleName() + "\">");
		
		builder.append("<table id=\""+elementId+"\"");
		
		//apply tablesorter style
		builder.append(" class=\"margin-bottom-0-imp noBackground inline-block scroll-x tablesorter markdowntable\"");
		builder.append(">");
		//headers
		builder.append("<thead><tr>");
		if (tableConfig.isShowRowNumber())
			builder.append("<th>"+tableConfig.getRowNumberColName()+"</th>");	//row number
		Map<ClickHandler, List<String>> clickHandler2ElementsMap = new HashMap<ClickHandler, List<String>>();
		//for each renderer, ask for it's list of columns that are being output
		for (int i = 0; i < renderers.length; i++) {
			List<String> rendererColumnNames = renderers[i].getColumnNames();
			final APITableColumnConfig columnConfig = tableConfig.getColumnConfigs().get(i);
			
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.columnConfigClicked(columnConfig);
				}
			};
			List<String> headerElements = new ArrayList<String>();
			for (Iterator<String> iterator = rendererColumnNames.iterator(); iterator.hasNext();) {
				String columnName = iterator.next();
				String id = elementId + "-header-"+i+"-"+columnName;
				headerElements.add(id);
				builder.append("<th class=\"imageButton\" id=\""+id+"\" anchortext=\""+columnName+"\"></th>");
			}
			clickHandler2ElementsMap.put(clickHandler, headerElements);
		}
		builder.append("</tr></thead><tbody>");
		if (columnData != null && columnData.size() > 0 && columnNames.length > 0) {
			//find the row count (there has to be at least one key/value mapping)
			List<String> data = columnData.get(columnNames[0]);
			if (data != null && data.size() > 0) {
				int rowCount = data.size();
				//now write out every row
				for (int i = 0; i < rowCount; i++) {
					builder.append("<tr>");
					if (tableConfig.isShowRowNumber())
						builder.append("<td class=\"padding-right-15\">"+(i+tableConfig.getOffset()+1)+"</td>"); //row number
					
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
							builder.append("<td class=\"padding-right-15\">"+value+"</td>");
						}
					}
					
					builder.append("</tr>");
				}
			}
		} else {
			builder.append("<tr>");
			for (int j = 0; j < renderers.length; j++) {
				builder.append("<td>&nbsp;</td>");
			}
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		if (isCssStyled)
			builder.append("</span>");

		panel = new HTMLPanel(builder.toString());
		add(panel);
		
		for (ClickHandler clickHandler : clickHandler2ElementsMap.keySet()) {
			List<String> elementIds = clickHandler2ElementsMap.get(clickHandler);
			for (String id : elementIds) {
				Element e = panel.getElementById(id);
				String anchorText = e.getAttribute("anchorText");
				HTML a = new HTML(SafeHtmlUtils.htmlEscape(anchorText));
				if (tableConfig.isPaging()) {
					a.addClickHandler(clickHandler);
				} 
				panel.add(a, id);
			}
		}

		//do not apply sorter if paging (service needs to be involved for a true column sort)
		if(!tableConfig.isPaging()) {
			synapseJSNIUtils.loadTableSorters();
		}
	}
	
	public List<ElementWrapper> findDivs(String prefix) {
		List<ElementWrapper> wrappedElements = new ArrayList<ElementWrapper>();
		JsArray<JavaScriptObject> elements = _findDivs(prefix);
		if (elements != null) {
			for (int i = 0; i < elements.length(); i++) {
				DivElement div = (DivElement) elements.get(i);
				wrappedElements.add(new ElementWrapper(div));
			}
		}
		return wrappedElements;
	}
	
	
	@Override
	public List<ElementWrapper> findUserBadgeDivs() {
		return findDivs(APITableColumnRendererUserId.USER_WIDGET_DIV_PREFIX);
	}
	
	@Override
	public List<ElementWrapper> findCancelRequestDivs() {
		return findDivs(APITableColumnRendererCancelControl.CANCEL_REQUEST_WIDGET_DIV_PREFIX);
	}
	
	@Override
	public void addWidget(IsWidget widget, String divID) {
		panel.add(widget.asWidget(), divID);
	}
	
	/**
	 * Return all divs on the page that have an id that begins with the given prefix
	 * @param prefix
	 * @return
	 */
	private static native JsArray<JavaScriptObject> _findDivs(String prefix) /*-{
		try {
			return $wnd.jQuery('div[id^='+prefix+']');
		} catch (err) {
			console.error(err);
		}
	}-*/;

	@Override
	public void configurePager(int start, int end, int total) {
		UnorderedListPanel panel = new UnorderedListPanel();
		panel.setStyleName("pager padding-left-5-imp inline-block margin-top-5");
		Label label = new Label(start + "-" + end + " of " + total);
		label.addStyleName("inline-block margin-left-5 margin-right-5");
		
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
		}
		panel.add(label, "pagerLabel");
		if(end == total) {
			panel.add(next, "disabled");
		} else {
			panel.add(next);
		}
		add(panel);
	}
	
	@Override
	public void showError(IsWidget synAlert) {
		clear();
		add(synAlert);	
	}
	
	@Override
	public void showTableUnavailable() {
		clear();
		FlowPanel unavailableContainer = new FlowPanel();
		unavailableContainer.addStyleName("jumbotron");
		unavailableContainer.add(new HTML("<h2>" + DisplayConstants.TABLE_UNAVAILABLE + "</h2><p><strong>"+ TableState.PROCESSING +"</strong>: "+ DisplayConstants.TABLE_PROCESSING_DESCRIPTION +"</p>"));
		
		TimedRetryWidget tryAgain = new TimedRetryWidget();
		tryAgain.configure(10, new Callback() {
			
			@Override
			public void invoke() {
				presenter.refreshData();
			}
		});
		unavailableContainer.add(tryAgain);
		
		add(unavailableContainer);
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	/*
	 * Private Methods
	 */

}
