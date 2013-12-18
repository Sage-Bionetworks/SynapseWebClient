package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.UnorderedListPanel;
import org.sagebionetworks.web.client.widget.ListCreatorViewWidget;
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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
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
		setupEditorToolbar(columns);
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


	private void setupEditorToolbar(final List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		buttonToolbar.clear();

		Button showColumnsBtn = DisplayUtils.createIconButton(DisplayConstants.COLUMN_DETAILS, ButtonType.DEFAULT, "glyphicon-th-list");
		showColumnsBtn.addStyleName("margin-right-5");
		showColumnsBtn.addClickHandler(new ClickHandler() {			
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
		
		buttonToolbar.add(showColumnsBtn);		
		buttonToolbar.add(addRowBtn);
	}
	
	private void buildColumnsEditor(List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		FlowPanel parent = new FlowPanel();
		parent.addStyleName("panel-group");
		String accordionId = "accordion-" + ++sequence;
		parent.getElement().setId(accordionId);
		
		// add header
		parent.add(new HTML("<h4>" + DisplayConstants.COLUMN_DETAILS + "</h4>"));
		
		for(int i=0; i<columns.size(); i++) {
			org.sagebionetworks.repo.model.table.ColumnModel col = columns.get(i);
			FlowPanel panel = new FlowPanel();
			panel.addStyleName("panel panel-default");
			String colContentId = "contentId" + ++sequence;

			FlowPanel columnEntry = new FlowPanel();
			columnEntry.addStyleName("panel-heading row");			
			String expandLinkStyleOpen = "<a data-toggle=\"collapse\" data-parent=\"#" + accordionId + "\" href=\"#" + colContentId + "\" class=\"link\">";
			
			FlowPanel left = new FlowPanel();			
			left.addStyleName("col-xs-7 col-sm-9 col-md-10");
			left.add(new HTML("<h4>" + expandLinkStyleOpen + SafeHtmlUtils.fromString(col.getName()).asString() + "</a></h4>"));
			FlowPanel right = new FlowPanel();
			right.addStyleName("col-xs-5 col-sm-3 col-md-2 text-align-right largeIconButton");
			Anchor moveUp = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-up margin-right-5\"></span>"));
			Anchor moveDown = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-arrow-down margin-right-5\"></span>"));
			Anchor delete = new Anchor(SafeHtmlUtils.fromSafeConstant("<span class=\"glyphicon glyphicon-remove\"></span>"));
			if(i!=0) right.add(moveUp);
			if(i!=columns.size()-1) right.add(moveDown);
			right.add(delete);
			
			columnEntry.add(left);
			columnEntry.add(right);
			panel.add(columnEntry);
			
			FlowPanel columnContent = new FlowPanel();
			columnContent.addStyleName("panel-collapse collapse");
			columnContent.getElement().setId(colContentId);
			FlowPanel columnContentBody = new FlowPanel();
			columnContentBody.addStyleName("panel-body");		
			columnContentBody.add(createColumnView(col));
			columnContent.add(columnContentBody);
			panel.add(columnContent);		
			parent.add(panel);
		}

		// Add Column
		final FlowPanel addColumnPanel = new FlowPanel();
		addColumnPanel.addStyleName("well margin-top-15");
		addColumnPanel.setVisible(false);
		org.sagebionetworks.repo.model.table.ColumnModel newColumn = new org.sagebionetworks.repo.model.table.ColumnModel();
		addColumnPanel.add(new HTML("<h4>" + DisplayConstants.ADD_COLUMN + "</h4>"));
		addColumnPanel.add(createColumnEditor(newColumn));

		Button addColumnBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_COLUMN, ButtonType.DEFAULT, "glyphicon-plus");
		addColumnBtn.addStyleName("margin-top-15");	
		addColumnBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(addColumnPanel.isVisible()) addColumnPanel.setVisible(false);
				else addColumnPanel.setVisible(true);
			}
		});
		parent.add(addColumnBtn);
		parent.add(addColumnPanel);
		
		columnEditorPanel.setWidget(parent);
	}

	private Widget createColumnView(org.sagebionetworks.repo.model.table.ColumnModel col) {
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.NAME + "</span>: ").appendEscaped(col.getName()).appendHtmlConstant("<br/>")
		.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.TYPE + "</span>: ").appendEscaped(ColumnUtils.getColumnDisplayName(col.getColumnType())).appendHtmlConstant("<br/>");
		if(col.getDefaultValue() != null) 
			shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.DEFAULT_VALUE + "</span>: ").appendEscaped(col.getDefaultValue()).appendHtmlConstant("<br/>");
		if(col.getEnumValues() != null && col.getEnumValues().size() > 0) {
			shb.appendHtmlConstant("<span class=\"boldText\">" + DisplayConstants.RESTRICTED_VALUES + "</span>: ");
			String values = "";
			for(String val : col.getEnumValues()) values += val + ", ";
			values = values.substring(0, values.length()-2); // chop last comma
			shb.appendEscaped(values).appendHtmlConstant("<br/>");
		}		
		return new HTML(shb.toSafeHtml());
	}
	
	private Widget createColumnEditor(final org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel form = new FlowPanel();
		form.addStyleName("margin-top-15");
		final TextBox name = new TextBox();
		if(col.getName() != null) name.setValue(SafeHtmlUtils.fromString(col.getName()).asString());
		name.addStyleName("form-control");
		HTML inputLabel = new HTML("Name: ");
		form.add(inputLabel);
		form.add(name);
		
		// Column Type
		inputLabel = new HTML("Column Type: ");
		inputLabel.addStyleName("margin-top-15");
		form.add(inputLabel);		
		form.add(createColumnTypeRadio(col));

		// Enum Values
		inputLabel = new HTML("Restricted Values: ");
		inputLabel.addStyleName("margin-top-15");
		form.add(inputLabel);	
		final ListCreatorViewWidget list = new ListCreatorViewWidget(DisplayConstants.ADD_VALUE, true);
		list.append("some stuff");
		list.append(Arrays.asList(new String[] {"even", "more"}));		
		form.add(createRestrictedValues(col, list));
		
		// Default Value		
		inputLabel = new HTML("Default Value: ");
		inputLabel.addStyleName("margin-top-15");
		form.add(inputLabel);
		form.add(createDefaultValueRadio(col));

		
		// Save/Cancel
		// TODO : hide buttons unless a change has been made
		FlowPanel buttons = new FlowPanel();
		buttons.addStyleName("margin-top-15");
		Button save = DisplayUtils.createButton(DisplayConstants.SAVE_BUTTON_LABEL, ButtonType.PRIMARY);
		save.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
//				if(name.getValue() == null || name.getValue().length() == 0) {
//					name.addStyleName("has-error");					
//				}
				List<String> restrictedValues = list.getValues();
				if(restrictedValues.size() > 0) col.setEnumValues(restrictedValues);
				presenter.createColumn(col);
			}
		});
		buttons.add(save);
		form.add(buttons);
		
		return form;
		
	}

	private Widget createRestrictedValues(org.sagebionetworks.repo.model.table.ColumnModel col, ListCreatorViewWidget list) {
		FlowPanel row = new FlowPanel();
		row.addStyleName("row");
		FlowPanel left = new FlowPanel();
		left.addStyleName("col-sm-6");
		FlowPanel right = new FlowPanel();
		right.addStyleName("col-sm-6");
		row.add(left);
		row.add(right);		
		left.add(list);		
		return row;
	}



	private Widget createDefaultValueRadio(
			org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel row = new FlowPanel();		
		FlowPanel defaultValueRadio = new FlowPanel();
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

	private Widget createColumnTypeRadio(
			org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel columnTypeRadio = new FlowPanel();
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
			if(col.getColumnType() != null && col.getColumnType() == type) btn.addStyleName("active");
			groupBtns.add(btn);
			columnTypeRadio.add(btn);

		}
		return columnTypeRadio;
	}
	
	private static native void enableBootstrapButtonPlugin() /*-{
		$wnd.jQuery('.btn').button();
	}-*/;
	
	


}
