package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.evaluation.model.Evaluation;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationListViewImpl extends LayoutContainer implements EvaluationListView {
	
	private static final String RULES_KEY = "rules";
	private static final String RECEIPT_KEY = "receipt";
	private static final String NAME_KEY = "name";
	private static final String ID_KEY = "idKey";
	

	final CheckBoxSelectionModel<BaseModelData> sm = new CheckBoxSelectionModel<BaseModelData>();
	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public EvaluationListViewImpl(IconsImageBundle iconsImageBundle) {
		gridStore = new ListStore<BaseModelData>();
		this.iconsImageBundle = iconsImageBundle;
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
	    
	    column = new ColumnConfig();  
	    column.setId(RULES_KEY);  
	    column.setHeader("Rules");
	    column.setWidth(40);
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
			model.set(RULES_KEY, data.getSubmissionInstructionsMessage());
			model.set(RECEIPT_KEY, data.getSubmissionReceiptMessage());
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
				
				if (property.equals(RULES_KEY)) {
					if (value != null && value.length() > 0) {
						//show link that pops up challenge specific submission rules
						final Dialog d = new Dialog();
						d.setBodyStyle("padding:5px;");
						//render like html coming from markdown
						d.addStyleName("markdown");
						d.add(new HTML(value));
						d.setAutoHeight(true);
						d.setWidth(500);
						d.setPlain(true);
						d.setModal(false);
						d.setHeading("Submission Rules");
						d.setLayout(new FitLayout());			    
					    d.setButtons(Dialog.CLOSE);
					    d.setButtonAlign(HorizontalAlignment.RIGHT);
	
					    
					    Image rulesButton = new Image(iconsImageBundle.informationBalloon16());
			        	rulesButton.addStyleName("imageButton");
					 	
			        	rulesButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								d.show();
							}
						});
			        	return rulesButton;
					}
					else {
						return new HTML();
					}
				} else {
					SafeHtmlBuilder builder = new SafeHtmlBuilder();
					builder.appendHtmlConstant("<div style='font-weight: normal;color:black; overflow:hidden; text-overflow:ellipsis; width:auto;'>");
					builder.appendEscaped(value);
					builder.appendHtmlConstant("</div>");
					Html html = new Html(builder.toSafeHtml().asString());
				    
					return html;
				}
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

	public List<Evaluation> getSelectedEvaluations() {
		List<Evaluation> selectedEvaluations = new ArrayList<Evaluation>();
		for (BaseModelData model : sm.getSelectedItems()) {
			selectedEvaluations.add(presenter.getEvaluation((String)model.get(ID_KEY)));
		}
		return selectedEvaluations;
	}
}
