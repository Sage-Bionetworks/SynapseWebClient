package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationListViewImpl extends LayoutContainer implements EvaluationListView {
	
	private static final String NAME_KEY = "name";
	private static final String ID_KEY = "idKey";
	

	final CheckBoxSelectionModel<BaseModelData> sm = new CheckBoxSelectionModel<BaseModelData>();
	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	private Presenter presenter;
	
	@Inject
	public EvaluationListViewImpl() {
		gridStore = new ListStore<BaseModelData>();
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setBorders(true);

	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
	    GridCellRenderer<BaseModelData> valueRenderer = createValueRenderer();
	    
	    sm.setSelectionMode(SelectionMode.MULTI);
	    configs.add(sm.getColumn());
	    
	    ColumnConfig column = new ColumnConfig();  
	    column.setId(NAME_KEY);  
	    column.setHeader("Name");  	    	   
	    column.setRowHeader(false);
	    column.setRenderer(valueRenderer);
	    configs.add(column);
	      	     	 
	    columnModel = new ColumnModel(configs);  
	  
	    grid = new Grid<BaseModelData>(gridStore, columnModel);
	    grid.setSelectionModel(sm);
	    grid.setStyleAttribute("borderTop", "none");
	    grid.setAutoHeight(false);
		grid.setHeight(100);
		grid.setAutoWidth(true);
		grid.setAutoExpandColumn(NAME_KEY);  
	    grid.setBorders(true);
		grid.setStripeRows(false);
		grid.setColumnLines(false);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(false);
		grid.setShadow(false);		
		
		this.add(grid);
	}
	
	@Override
	public void configure(List<Evaluation> list) {		
		gridStore.removeAll();
		if(list == null || list.size() == 0){
			addNoAttachmentRow();
		} else {
			populateStore(list);			
		}

		
		if(isRendered())
			grid.reconfigure(gridStore, columnModel);
		this.layout(true);		
	}

	private void addNoAttachmentRow() {
		BaseModelData model = new BaseModelData();
		model.set(NAME_KEY, DisplayConstants.TEXT_NO_ATTACHMENTS);
		gridStore.add(model);		
	}
	
	private void populateStore(List<Evaluation> list) {		
		for(Evaluation data: list){
			BaseModelData model = new BaseModelData();
			model.set(NAME_KEY, SafeHtmlUtils.fromString(data.getName()).asString());
			model.set(ID_KEY, data.getId());
			gridStore.add(model);
		}
	}

	public GridCellRenderer<BaseModelData> createValueRenderer() {
		GridCellRenderer<BaseModelData> valueRenderer = new GridCellRenderer<BaseModelData>() {

			@Override
			public Object render(BaseModelData model, String property,
					ColumnData config, final int rowIndex, int colIndex,
					ListStore<BaseModelData> store, Grid<BaseModelData> grid) {
				
				
				String value = model.get(property);
				if (value == null) {
					value = "";
				} 
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				builder.appendHtmlConstant("<div style='font-weight: normal;color:black; overflow:hidden; text-overflow:ellipsis; width:auto;'>");
				builder.appendEscaped(value);
				builder.appendHtmlConstant("</div>");
				Html html = new Html(builder.toSafeHtml().asString());
			    
				return html;
			}

		};
		return valueRenderer;
	}

	@Override
	public Widget asWidget() {
		if(isRendered()) {
			grid.reconfigure(gridStore, columnModel);
			this.layout(true);
		}
		return this;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	public List<String> getSelectedEvaluationIds() {
		List<String> selectedIds = new ArrayList<String>();
		for (BaseModelData model : sm.getSelectedItems()) {
			selectedIds.add((String)model.get(ID_KEY));
		}
		return selectedIds;
	}
}
