package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
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
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
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
	FlowPanel addColumnPanel;
	List<ColumnDetailsPanel> columnPanelOrder;
	
	@Inject
	public SynapseTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle) {
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
	}

	@Override
	public void configure(TableEntity table, List<org.sagebionetworks.repo.model.table.ColumnModel> columns, String queryString, boolean canEdit) {
		this.columns = columns;
		
		// clear out old view
		columnEditorBuilt = false;
		
		// build view
		store = new ListStore<BaseModelData>();
		setupQuery(queryString);		
		buildColumns(columns);
		setupTable();		
		setupTableEditorToolbar(columns);
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
	
	/* ================
	 * Table Methods
	 * ================
	 */	
	
	/**
	 * Sets up the Table view
	 */
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
	
	/**
	 * Fills ColumnConfigs for each column model
	 * @param columns
	 */
	private void buildColumns(List<org.sagebionetworks.repo.model.table.ColumnModel> columns) {
		columnConfigs = new ArrayList<ColumnConfig>();  	
		for(org.sagebionetworks.repo.model.table.ColumnModel col : columns) {
			columnConfigs.add(ColumnUtils.getColumnConfig(col));
		}			  	  
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
							presenter.updateColumnOrder(extractColumns());
							com.extjs.gxt.ui.client.widget.button.Button btn = ce.getButtonClicked();	
							if (btn.getText().equals("Yes")) {
								columnPanel.addStyleName("fade");
								// allow for fade before removal
								Timer t = new Timer() {								
									@Override
									public void run() {
										allColumnsPanel.remove(columnPanel);
										columnPanelOrder.remove(columnPanel);
										// presenter.update
										
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
			if(i==columnPanelOrder.size()-1) columnPanel.getMoveDown().setVisible(false); 
			
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
		

		// Display Name
		inputLabel = new HTML(DisplayConstants.DISPLAY_NAME + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText");
		final TextBox displayName = new TextBox();
		// TODO : fill in display name if available from model in future
		displayName.addStyleName("form-control");
		DisplayUtils.setPlaceholder(displayName, DisplayConstants.DISPLAY_NAME);
		form.add(inputLabel);
		form.add(displayName);
				
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
			String radioLabel = ColumnUtils.getColumnDisplayName(type);
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

	
}
