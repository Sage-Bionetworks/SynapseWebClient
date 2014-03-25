package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.ListCreatorViewWidget;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CompleteTableWidgetViewImpl extends Composite implements CompleteTableWidgetView {
	public interface Binder extends UiBinder<Widget, CompleteTableWidgetViewImpl> {}

	private static int sequence = 0;
	
	@UiField 
	HTMLPanel tablePanel;
	@UiField
	SimplePanel tableContainer;	
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
	FlowPanel addColumnPanel;
	List<ColumnDetailsPanel> columnPanelOrder;
	PortalGinInjector ginInjector;
	
	@Inject
	public CompleteTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.ginInjector = ginInjector;
	}

	@Override
	public void configure(TableEntity table, List<org.sagebionetworks.repo.model.table.ColumnModel> columns, String queryString, boolean canEdit) {
		this.columns = columns;
		
		//columns = getTestColumns(); // TODO : remove
		
		// clear out old view
		columnEditorBuilt = false;
		
		// build view
		store = new ListStore<BaseModelData>();

		setupTableEditorToolbar(columns);
		if(canEdit) {
			buttonToolbar.setVisible(true);
		}
 
		setupTable(table, columns, queryString, canEdit);		
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
	
	/* ================
	 * Table Methods
	 * ================
	 */	
	
	/**
	 * Sets up the Table view
	 * @param columns
	 * @param table 
	 * @param canEdit 
	 */
	private void setupTable(TableEntity tableEntity, List<ColumnModel> columns, String queryString, boolean canEdit) {
		SimpleTableWidget table = ginInjector.getSimpleTableWidget();		
		table.configure(tableEntity.getId(), columns, queryString, canEdit);
		tableContainer.setWidget(table.asWidget());				
	}

	/**
	 * Sets up the top level editing toolbar
	 * @param columns
	 */
	private void setupTableEditorToolbar(final List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		buttonToolbar.clear();

		Button showColumnsBtn = DisplayUtils.createIconButton(DisplayConstants.COLUMN_DETAILS, ButtonType.DEFAULT, "glyphicon-th-list");
		showColumnsBtn.addStyleName("margin-right-5");
		showColumnsBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(!columnEditorBuilt) columnEditorPanel.setWidget(buildColumnsEditor(columns));
				columnEditorPanel.setVisible( columnEditorPanel.isVisible() ? false : true ); 
			}
		});
		
		Button addRowBtn = DisplayUtils.createIconButton(DisplayConstants.ADD_ROW, ButtonType.DEFAULT, "glyphicon-plus");
		addRowBtn.addStyleName("margin-right-5");
		addRowBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
		    	addRow(columns);  
			}
		});
		
		buttonToolbar.add(showColumnsBtn);		
		buttonToolbar.add(addRowBtn);
	}
	
	/**
	 * Method that adds an empty row to the table
	 * @param columns
	 */
	private void addRow(final List<ColumnModel> columns) {
//		BaseModelData row = new BaseModelData();  
//    	// fill default values
//    	for(org.sagebionetworks.repo.model.table.ColumnModel columnModel : columns) {		    		
//    		if(columnModel.getDefaultValue() != null) {
//    			Object value = null;
//    			if(columnModel.getColumnType() == ColumnType.LONG) {
//    				value = new Long(columnModel.getDefaultValue());
//    			} else if(columnModel.getColumnType() == ColumnType.DOUBLE) {
//    				value = new Double(columnModel.getDefaultValue()); 
//    			} else if(columnModel.getColumnType() == ColumnType.BOOLEAN) {
//    				value = columnModel.getDefaultValue().toLowerCase(); 
//    			} else {
//    				value = columnModel.getDefaultValue();
//    			}
//    			row.set(columnModel.getName(), value);
//    		}
//    	}
//    	
//    	// add row
//        rowEditor.stopEditing(false);  
//        store.insert(row, store.getCount());  
//        rowEditor.startEditing(store.indexOf(row), true);
        
        // TODO: update presenter with new row. or wait for save on row editor. Depends on table impl
	}

	
	
	/* ================
	 * Column Methods
	 * ================
	 */	

	/**
	 * Builds a widget for the column editor/view panel
	 * @param columns
	 * @return
	 */
	private Widget buildColumnsEditor(List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		FlowPanel parent = new FlowPanel();
		parent.addStyleName("panel-group");
		String accordionId = "accordion-" + ++sequence;
		parent.getElement().setId(accordionId);
		
		// add header
		parent.add(new HTML("<h4>" + DisplayConstants.COLUMN_DETAILS + "</h4>"));
		
		final FlowPanel allColumnsPanel = new FlowPanel();
		columnPanelOrder = new ArrayList<ColumnDetailsPanel>();
		for(int i=0; i<columns.size(); i++) {
			final org.sagebionetworks.repo.model.table.ColumnModel col = columns.get(i);			
			final ColumnDetailsPanel columnPanel = new ColumnDetailsPanel(accordionId, col, "contentId" + ++sequence);
			
			columnPanel.getMoveUp().addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					// swap columns
					int formerIdx = columnPanelOrder.indexOf(columnPanel);
					swapColumns(allColumnsPanel, columnPanel, formerIdx, formerIdx-1);
				}

			});
			columnPanel.getMoveDown().addClickHandler(new ClickHandler() {				
				@Override
				public void onClick(ClickEvent event) {
					// swap columns
					int formerIdx = columnPanelOrder.indexOf(columnPanel);
					swapColumns(allColumnsPanel, columnPanel, formerIdx, formerIdx+1);
				}

			});
			columnPanel.getDelete().addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					MessageBox.confirm("Confirm", DisplayConstants.CONFIRM_DELETE_COLUMN + col.getName(), new Listener<MessageBoxEvent>() {
						public void handleEvent(MessageBoxEvent ce) {							
							com.extjs.gxt.ui.client.widget.button.Button btn = ce.getButtonClicked();	
							if (btn.getText().equals("Yes")) {
								columnPanel.addStyleName("fade");
								// allow for fade before removal
								Timer t = new Timer() {								
									@Override
									public void run() {
										allColumnsPanel.remove(columnPanel);
										columnPanelOrder.remove(columnPanel);
										
										// update table entity
										presenter.updateColumnOrder(extractColumns());
										
										// update ends, if needed
										int size = columnPanelOrder.size();
										if(size > 0) {
											setArrowVisibility(0, size, columnPanelOrder.get(0).getMoveUp(), columnPanelOrder.get(0).getMoveDown());
											setArrowVisibility(size-1, size, columnPanelOrder.get(size-1).getMoveUp(), columnPanelOrder.get(size-1).getMoveDown());
										}
									}
								};
								t.schedule(250);
							}
						}
					});
				}
			});
			if(i==0) columnPanel.getMoveUp().setVisible(false);
			if(i==columns.size()-1) columnPanel.getMoveDown().setVisible(false); 
			
			columnPanelOrder.add(columnPanel);
			allColumnsPanel.add(columnPanel);
		}
		parent.add(allColumnsPanel);

		// Add Column
		addColumnPanel = new FlowPanel();
		addColumnPanel.addStyleName("well margin-top-15");		
		refreshAddColumnPanel();

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
		
		return parent;
	}
		
	/**
	 * Create an editor widget for a column
	 * @param col
	 * @return
	 */
	private Widget createColumnEditor(final org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel form = new FlowPanel();
		form.addStyleName("margin-top-15");		
		
		// Column Name	
		FlowPanel formGroup = new FlowPanel();		
		formGroup.addStyleName("form-group");
		HTML inputLabel = new InlineHTML(DisplayConstants.COLUMN_NAME + ": ");
		inputLabel.addStyleName("boldText");
		final TextBox name = new TextBox();
		if(col.getName() != null) name.setValue(SafeHtmlUtils.fromString(col.getName()).asString());
		name.addStyleName("form-control");
		DisplayUtils.setPlaceholder(name, DisplayConstants.COLUMN_NAME);
		final InlineHTML columnNameError = DisplayUtils.createFormHelpText(DisplayConstants.COLUMN_NAME + " " + DisplayConstants.REQUIRED);
		columnNameError.addStyleName("text-danger-imp");
		columnNameError.setVisible(false);
		formGroup.add(inputLabel);
		formGroup.add(name);
		formGroup.add(columnNameError);
		form.add(formGroup);
		
		// Column Type
		inputLabel = new HTML(DisplayConstants.COLUMN_TYPE + ": ");
		inputLabel.addStyleName("margin-top-15 boldText");
		final InlineHTML columnTypeError = DisplayUtils.createFormHelpText(DisplayConstants.COLUMN_TYPE + " " + DisplayConstants.REQUIRED);
		columnTypeError.addStyleName("text-danger-imp");
		columnTypeError.setVisible(false);
		form.add(inputLabel);		
		form.add(createColumnTypeRadio(col));		
		form.add(columnTypeError);
						
		// Default Value	
		inputLabel = new HTML(DisplayConstants.DEFAULT_VALUE + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText");
		form.add(inputLabel);
		form.add(createDefaultValueRadio(col));

		// Enum Values
		inputLabel = new HTML(DisplayConstants.RESTRICT_VALUES + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText");
		form.add(inputLabel);	
		final ListCreatorViewWidget list = new ListCreatorViewWidget(DisplayConstants.ADD_VALUE, true);
		form.add(createRestrictedValues(col, list));
		
		// Create column
		Button save = DisplayUtils.createButton(DisplayConstants.CREATE_COLUMN, ButtonType.PRIMARY);
		save.addStyleName("margin-top-15");
		save.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				if(name.getValue() == null || name.getValue().length() == 0) {
					columnNameError.setVisible(true);
					return;
				} else {
					columnNameError.setVisible(false);
				}
				if(col.getColumnType() == null) {
					columnTypeError.setVisible(true);
					return;
				} else {
					columnTypeError.setVisible(false);
				}
				// import values into col. Type and default are set automatically
				col.setName(name.getValue());				
				List<String> restrictedValues = list.getValues();
				if(restrictedValues.size() > 0) col.setEnumValues(restrictedValues);
				presenter.createColumn(col);
				
				refreshAddColumnPanel();
			}
		});
		form.add(save);
		
		return form;		
	}

	/**
	 * Create a radio input widget for column type. Initializes to the given col, and modifies the given col.
	 * @param col
	 * @return
	 */
	private Widget createColumnTypeRadio(final org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel columnTypeRadio = new FlowPanel();
		columnTypeRadio.addStyleName("btn-group");
		final List<Button> groupBtns = new ArrayList<Button>(); 
		for(final ColumnType type : ColumnType.values()) {			
			String radioLabel = TableViewUtils.getColumnDisplayName(type);
			final Button btn = DisplayUtils.createButton(radioLabel);
			btn.addClickHandler(new ClickHandler() {			
				@Override
				public void onClick(ClickEvent event) {
					for(Button gBtn : groupBtns) {
						gBtn.removeStyleName("active");
					}
					btn.addStyleName("active");
					col.setColumnType(type);
				}
			});
			if(col.getColumnType() != null && col.getColumnType() == type) btn.addStyleName("active");
			groupBtns.add(btn);
			columnTypeRadio.add(btn);
		}
		return columnTypeRadio;
	}
	
	/**
	 * Create a default value input with on/off switch. Initializes to the given col, and modifiees the given col.
	 * @param col
	 * @return
	 */
	private Widget createDefaultValueRadio(
			final org.sagebionetworks.repo.model.table.ColumnModel col) {
		FlowPanel row = new FlowPanel();		
		FlowPanel defaultValueRadio = new FlowPanel();
		defaultValueRadio.addStyleName("btn-group");
		 						
		final Button onBtn = DisplayUtils.createButton(DisplayConstants.ON_CAP);
		final Button offBtn = DisplayUtils.createButton(DisplayConstants.OFF);
		final TextBox defaultValueBox = new TextBox();
		defaultValueBox.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				col.setDefaultValue(defaultValueBox.getValue());
			}
		});
		DisplayUtils.setPlaceholder(defaultValueBox, DisplayConstants.DEFAULT_VALUE);
		onBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				offBtn.removeStyleName("active");
				onBtn.addStyleName("active");
				defaultValueBox.setVisible(true);
			}
		});
		offBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				onBtn.removeStyleName("active");
				offBtn.addStyleName("active");
				defaultValueBox.setVisible(false);
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

	/**
	 * Create the restricted values list
	 * @param col
	 * @param list
	 * @return
	 */
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

	/**
	 * Clears, hides and rebuilds the add column panel.
	 */
	private void refreshAddColumnPanel() {
		addColumnPanel.clear();
		addColumnPanel.setVisible(false);
		org.sagebionetworks.repo.model.table.ColumnModel newColumn = new org.sagebionetworks.repo.model.table.ColumnModel();
		addColumnPanel.add(new HTML("<h4>" + DisplayConstants.ADD_COLUMN + "</h4>"));
		addColumnPanel.add(createColumnEditor(newColumn));
	}

	private static void setArrowVisibility(int idx, int size, Anchor moveUp, Anchor moveDown) {
		if(idx == 0) moveUp.setVisible(false);
		else moveUp.setVisible(true);
		if(idx == size-1) moveDown.setVisible(false);
		else moveDown.setVisible(true);
	}

	private void swapColumns(final FlowPanel allColumnsPanel, final ColumnDetailsPanel thisColumn,
			final int formerIdx, final int newIdx) {
		final ColumnDetailsPanel displacedColumn = columnPanelOrder.get(newIdx);
		// fade out		
		thisColumn.addStyleName("fade");
		Timer t1 = new Timer() {			
			@Override
			public void run() {
				// swap columns
				columnPanelOrder.set(newIdx, thisColumn);				
				columnPanelOrder.set(formerIdx, displacedColumn);
				allColumnsPanel.remove(thisColumn);
				allColumnsPanel.insert(thisColumn, newIdx);
				setArrowVisibility(newIdx, columnPanelOrder.size(), thisColumn.getMoveUp(), thisColumn.getMoveDown());
				setArrowVisibility(formerIdx, columnPanelOrder.size(), displacedColumn.getMoveUp(), displacedColumn.getMoveDown());

				// fade in
				Timer t2 = new Timer() {					
					@Override
					public void run() {
						thisColumn.addStyleName("in");
						
						// cleanup
						Timer t3 = new Timer() {			
							@Override
							public void run() {
								thisColumn.removeStyleName("fade");
								thisColumn.removeStyleName("in");
							}
						};
						t3.schedule(250);

					}
				};
				t2.schedule(250);
			}
		};
		t1.schedule(250);		
	}

	private List<String> extractColumns() {
		List<String> columns = new ArrayList<String>();
		for(ColumnDetailsPanel colD : columnPanelOrder) {
			columns.add(colD.getCol().getId());
		}		
		return columns;
	}

	/*
	 * Temp
	 */
	private List<ColumnModel> getTestColumns() {
		List<ColumnModel> columns = new ArrayList<ColumnModel>();

		ColumnModel model;

		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("175");
		model.setName("cellline");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("Drug1");
		model.setName("Drug1");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_Conc");
		model.setName("Drug1_Conc");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_InhibitionMean");
		model.setName("Drug1_InhibitionMean");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug1_InhibitionStdev");
		model.setName("Drug1_InhibitionStdev");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.STRING);
		model.setId("Drug2");
		model.setName("Drug2");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_Conc");
		model.setName("Drug2_Conc");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_InhibitionMean");
		model.setName("Drug2_InhibitionMean");
		columns.add(model);
		
		model = new ColumnModel();
		model.setColumnType(ColumnType.DOUBLE);
		model.setId("Drug2_InhibitionStdev");
		model.setName("Drug2_InhibitionStdev");
		columns.add(model);
		
		
//		// first name
//		ColumnModel model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("FirstName");
//		model.setName("First Name");
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("LastName");
//		model.setName("Last Name");
//		columns.add(model);
//
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.FILEHANDLEID);
//		model.setId("Plot");
//		model.setName("Plot");
//		columns.add(model);		
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("Category");
//		model.setName("Category");
//		model.setEnumValues(Arrays.asList(new String[] {"One", "Two", "Three"}));
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.STRING);
//		model.setId("Address");
//		model.setName("Address");
//		columns.add(model);
//		
////		model = new ColumnModel();
////		model.setColumnType(ColumnType.DATE);
////		model.setId("Birthday");
////		model.setName("Birthday");
////		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.BOOLEAN);
//		model.setId("IsAlive");
//		model.setName("IsAlive");
//		columns.add(model);
//		
//		model = new ColumnModel();
//		model.setColumnType(ColumnType.DOUBLE);
//		model.setId("BMI");
//		model.setName("BMI");
//		columns.add(model);

		
		return columns;
	}


}
