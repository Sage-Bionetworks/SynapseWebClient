package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.shared.TableObject;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableWidgetViewImpl extends Composite implements SynapseTableWidgetView {
	public interface Binder extends UiBinder<Widget, SynapseTableWidgetViewImpl> {}

	private static int sequence = 0;
	
	@UiField
	HTMLPanel queryPanel;
	@UiField 
	HTMLPanel tablePanel;
	@UiField
	SimplePanel tableContainer;	
	@UiField
	TextBox queryField;
	@UiField
	SimplePanel queryButtonContainer;
	@UiField
	HTMLPanel buttonToolbar;
	@UiField
	SimplePanel columnEditorPanel;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private List<ColumnConfig> columnConfigs;
	private ListStore<BaseModelData> store;
	private Grid<BaseModelData> grid;
	private RowEditor<BaseModelData> rowEditor;
	private boolean columnEditorBuilt = false;
	private List<org.sagebionetworks.repo.model.table.ColumnModel> columns;
	
	@Inject
	public SynapseTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle) {
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
	}

	@Override
	public void configure(TableObject table, List<org.sagebionetworks.repo.model.table.ColumnModel> columns, String queryString, boolean canEdit) {
		this.columns = columns;
		
		// clear out old view
		columnEditorBuilt = false;
		
		// build view
		store = new ListStore<BaseModelData>();
		setupQuery(queryString);		
		buildColumns(columns);
		setupTable();		
		setupBottomToolbar(columns);
		queryPanel.setVisible(true);		
		if(canEdit) {
			buttonToolbar.setVisible(true);
		}
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
	}
	
	
	/*
	 * Private Methods
	 */	
	private void setupQuery(String queryString) {
		// setup query
		Button queryBtn = DisplayUtils.createButton(DisplayConstants.QUERY);
		queryBtn.addStyleName("btn-block");
		queryBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.query(queryField.getValue());
			}
		});
		queryButtonContainer.setWidget(queryBtn);
		queryField.setValue(queryString);
	}
	
	private void buildColumns(List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		columnConfigs = new ArrayList<ColumnConfig>();  	
		for(org.sagebionetworks.repo.model.table.ColumnModel col : columns) {
			columnConfigs.add(ColumnUtils.getColumnConfig(col));
		}			  	  
	}

	private void setupTable() {
		// setup table	  
	    ColumnModel cm = new ColumnModel(columnConfigs);  	 	   
	    
	    LayoutContainer lc = new LayoutContainer();  
	    lc.setLayout(new FitLayout());  
	  
	    rowEditor = new RowEditor<BaseModelData>();  
	    grid = new Grid<BaseModelData>(store, cm);  
	    if(columns != null && columns.size() > 0) grid.setAutoExpandColumn(columns.get(0).getName());  
	    grid.setBorders(true);  
	    //grid.addPlugin(checkColumn);  
	    grid.addPlugin(rowEditor);  	    
	    grid.setHeight(250);

	    lc.add(grid);  	 	   
	    
	    // store commit/cancel
//	    store.rejectChanges();  
//	    store.commitChanges();  
	    
	    tableContainer.setWidget(lc);
	}


	private void setupBottomToolbar(final List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		buttonToolbar.clear();

		Button editColumnsBtn = DisplayUtils.createIconButton(DisplayConstants.EDIT_COLUMNS, ButtonType.DEFAULT, "glyphicon-pencil");
		editColumnsBtn.addStyleName("margin-right-5");
		editColumnsBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(!columnEditorBuilt) buildColumnsEditor(columns);
				columnEditorPanel.setVisible( columnEditorPanel.isVisible() ? false : true ); 
			}
		});
		
		Button addRowBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_ROW, ButtonType.DEFAULT, "glyphicon-plus");
		addRowBtn.addStyleName("margin-right-5");
		addRowBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
		    	BaseModelData row = new BaseModelData();  
		    	// fill default values
		    	for(org.sagebionetworks.repo.model.table.ColumnModel columnModel : columns) {		    		
		    		if(columnModel.getDefaultValue() != null) {
		    			Object value = null;
		    			if(columnModel.getColumnType() == ColumnType.LONG) {
		    				value = new Long(columnModel.getDefaultValue());
		    			} else if(columnModel.getColumnType() == ColumnType.DOUBLE) {
		    				value = new Double(columnModel.getDefaultValue()); 
		    			} else if(columnModel.getColumnType() == ColumnType.BOOLEAN) {
		    				value = columnModel.getDefaultValue().toLowerCase(); 
		    			} else {
		    				value = columnModel.getDefaultValue();
		    			}
		    			row.set(columnModel.getName(), value);
		    		}
		    	}
		    	
		    	// add row
		        rowEditor.stopEditing(false);  
		        store.insert(row, store.getCount());  
		        rowEditor.startEditing(store.indexOf(row), true);  
			}
		});
		
		buttonToolbar.add(editColumnsBtn);		
		buttonToolbar.add(addRowBtn);
	}
	
	private void buildColumnsEditor(List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		HTMLPanel parent = new HTMLPanel("");
		parent.addStyleName("panel-group");
		String accordionId = "accordion-" + ++sequence;
		parent.getElement().setId(accordionId);
		
		// add header
		parent.add(new HTML("<h4>" + DisplayConstants.TABLE_COLUMNS + "</h4>"));
		
		for(int i=0; i<columns.size(); i++) {
			org.sagebionetworks.repo.model.table.ColumnModel col = columns.get(i);
			HTMLPanel panel = new HTMLPanel("");
			panel.addStyleName("panel panel-default");
			String colContentId = "contentId" + ++sequence;

			HTMLPanel columnEntry = new HTMLPanel("");
			columnEntry.addStyleName("panel-heading row");			
			String expandLinkPart = "<a data-toggle=\"collapse\" data-parent=\"#" + accordionId + "\" href=\"#" + colContentId + "\"";
			String expandLinkOpen = expandLinkPart + ">";
			String expandLinkStyleOpen = expandLinkPart + " class=\"link\">";
			
			HTMLPanel left = new HTMLPanel("");			
			left.addStyleName("col-xs-7 col-sm-9 col-md-10");
			left.add(new HTML("<h4>" + expandLinkStyleOpen + SafeHtmlUtils.fromString(col.getName()).asString() + "</a></h4>"));
			HTMLPanel right = new HTMLPanel("");
			right.addStyleName("col-xs-5 col-sm-3 col-md-2 text-align-right largeIconButton");
			Anchor moveUp = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-up margin-right-5\"></span>"));
			Anchor moveDown = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-down margin-right-5\"></span>"));
			InlineHTML editLink = new InlineHTML(expandLinkOpen + "<span class=\"glyphicon glyphicon-pencil margin-right-5\"></span></a>");
			editLink.addStyleName("display-inline");
			Anchor delete = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-remove\"></span>"));
			right.add(editLink);
			if(i!=0) right.add(moveUp);
			if(i!=columns.size()-1) right.add(moveDown);
			right.add(delete);
			
			columnEntry.add(left);
			columnEntry.add(right);
			panel.add(columnEntry);
			
			HTMLPanel columnContent = new HTMLPanel("");
			columnContent.addStyleName("panel-collapse collapse");
			columnContent.getElement().setId(colContentId);
			HTMLPanel columnContentBody = new HTMLPanel("");
			columnContentBody.addStyleName("panel-body");		
			columnContentBody.add(createColumnEditor(col));
			columnContent.add(columnContentBody);
			panel.add(columnContent);		
			parent.add(panel);
		}
		
		Button addColumnBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_COLUMN, ButtonType.DEFAULT, "glyphicon-plus");
		addColumnBtn.addStyleName("margin-top-15");
		parent.add(addColumnBtn);
		
		columnEditorPanel.setWidget(parent);
	}

	private Widget createColumnEditor(
			org.sagebionetworks.repo.model.table.ColumnModel col) {
		HTMLPanel form = new HTMLPanel("");
		final TextBox name = new TextBox();
		name.setValue(SafeHtmlUtils.fromString(col.getName()).asString());
		name.addStyleName("form-control");
		HTML inputLabel = new HTML("Name: ");
		form.add(inputLabel);
		form.add(name);
		
		// Column Type
		inputLabel = new HTML("Column Type: ");
		inputLabel.addStyleName("margin-top-15");
		form.add(inputLabel);
		HTMLPanel columnTypeRadio = createColumnTypeRadio(col);		
		form.add(columnTypeRadio);

		// Default Value		
		inputLabel = new HTML("Default Value: ");
		inputLabel.addStyleName("margin-top-15");
		form.add(inputLabel);
		HTMLPanel row = createDefaultValueRadio(col);
		form.add(row);

		
		// Save/Cancel
		// TODO : hide buttons unless a change has been made
		HTMLPanel buttons = new HTMLPanel("");
		buttons.addStyleName("margin-top-15");
		Button save = DisplayUtils.createButton(DisplayConstants.SAVE_BUTTON_LABEL, ButtonType.PRIMARY);		
		Button cancel = DisplayUtils.createButton(DisplayConstants.BUTTON_CANCEL, ButtonType.LINK);
		buttons.add(save);
		buttons.add(cancel);
		form.add(buttons);
		
		return form;
		
	}

	private HTMLPanel createDefaultValueRadio(
			org.sagebionetworks.repo.model.table.ColumnModel col) {
		HTMLPanel row = new HTMLPanel("");		
		HTMLPanel defaultValueRadio = new HTMLPanel("");
		defaultValueRadio.addStyleName("btn-group");
		 						
		final Button onBtn = DisplayUtils.createButton(DisplayConstants.ON_CAP);
		final Button offBtn = DisplayUtils.createButton(DisplayConstants.OFF);
		final TextBox defaultValueBox = new TextBox();
		onBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				offBtn.removeStyleName("active");
				onBtn.addStyleName("active");
				defaultValueBox.setVisible(true);
				showInfo("selected", "on");
			}
		});
		offBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				onBtn.removeStyleName("active");
				offBtn.addStyleName("active");
				defaultValueBox.setVisible(false);
				showInfo("selected", "off");
			}
		});
		if(col.getDefaultValue() != null) {
			onBtn.addStyleName("active");
			defaultValueBox.setVisible(true);
		} else {
			offBtn.addStyleName("active");
			defaultValueBox.setVisible(false);
		}
		
		defaultValueRadio.add(onBtn);
		defaultValueRadio.add(offBtn);			
		
		
		// TODO : choose appropriate input type for default value (string, enum, date, etc)
		defaultValueBox.addStyleName("form-control display-inline margin-top-5");
		defaultValueBox.setWidth("300px");
		defaultValueBox.getElement().setAttribute("placeholder", "Default Value");
		defaultValueBox.setValue(col.getDefaultValue());
		
		row.add(defaultValueRadio);
		row.add(defaultValueBox);
		return row;
	}

	private HTMLPanel createColumnTypeRadio(
			org.sagebionetworks.repo.model.table.ColumnModel col) {
		HTMLPanel columnTypeRadio = new HTMLPanel("");
		columnTypeRadio.addStyleName("btn-group");
		final List<Button> groupBtns = new ArrayList<Button>(); 
		for(final ColumnType type : ColumnType.values()) {			
			String radioLabel = ColumnUtils.getColumnDisplayName(type);
			final Button btn = DisplayUtils.createButton(radioLabel);
			btn.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					for(Button gBtn : groupBtns) {
						gBtn.removeStyleName("active");
					}
					btn.addStyleName("active");
					showInfo("selected", type + "");
				}
			});
			if(col.getColumnType() == type) btn.addStyleName("active");
			groupBtns.add(btn);
			columnTypeRadio.add(btn);

		}
		return columnTypeRadio;
	}
	
	private static native void enableBootstrapButtonPlugin() /*-{
		$wnd.jQuery('.btn').button();
	}-*/;
	
	


}
