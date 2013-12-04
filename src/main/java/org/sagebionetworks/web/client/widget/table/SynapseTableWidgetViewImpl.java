package org.sagebionetworks.web.client.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.shared.TableObject;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.RowEditor;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableWidgetViewImpl extends Composite implements SynapseTableWidgetView {
	public interface Binder extends UiBinder<Widget, SynapseTableWidgetViewImpl> {}
	
	@UiField
	HTMLPanel queryPanel;
	@UiField
	SimplePanel tablePanel;
	@UiField
	TextBox queryField;
	@UiField
	SimplePanel queryButtonContainer;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	
	@Inject
	public SynapseTableWidgetViewImpl(final Binder uiBinder, SageImageBundle sageImageBundle,
			IconsImageBundle iconsImageBundle) {
		initWidget(uiBinder.createAndBindUi(this));

		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		
		setupQuery();		
		setupTable();
	}


	@Override
	public void configure(TableObject table, String queryString) {
		// build view
		queryPanel.setVisible(true);
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
	private void setupTable() {
		// setup table
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
		  
	    ColumnConfig column = new ColumnConfig();  
	    column.setId("name");  
	    column.setHeader("Common Name");  
	    column.setWidth(220);  
	  
	    TextField<String> text = new TextField<String>();  
	    text.setAllowBlank(false);  
	    column.setEditor(new CellEditor(text));  
	    configs.add(column);  
	  
	    final SimpleComboBox<String> combo = new SimpleComboBox<String>();  
	    combo.setForceSelection(true);  
	    combo.setTriggerAction(TriggerAction.ALL);  
	    combo.add("Shade");  
	    combo.add("Mostly Shady");  
	    combo.add("Sun or Shade");  
	    combo.add("Mostly Sunny");  
	    combo.add("Sunny");  
	  
	    CellEditor editor = new CellEditor(combo) {  
	      @Override  
	      public Object preProcessValue(Object value) {  
	        if (value == null) {  
	          return value;  
	        }  
	        return combo.findModel(value.toString());  
	      }  
	  
	      @Override  
	      public Object postProcessValue(Object value) {  
	        if (value == null) {  
	          return value;  
	        }  
	        return ((ModelData) value).get("value");  
	      }  
	    };  
	  
	    column = new ColumnConfig();  
	    column.setId("light");  
	    column.setHeader("Light");  
	    column.setWidth(130);  
	    column.setEditor(editor);  
	    configs.add(column);  
	  
	    column = new ColumnConfig();  
	    column.setId("price");  
	    column.setHeader("Price");  
	    column.setAlignment(HorizontalAlignment.RIGHT);  
	    column.setWidth(70);  
	    column.setNumberFormat(NumberFormat.getCurrencyFormat());  
	    column.setEditor(new CellEditor(new NumberField()));  
	  
	    configs.add(column);  
	  
	    DateField dateField = new DateField();  
	    dateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("MM/dd/y"));  
	  
	    column = new ColumnConfig();  
	    column.setId("available");  
	    column.setHeader("Available");  
	    column.setWidth(95);  
	    column.setEditor(new CellEditor(dateField));  
	    column.setDateTimeFormat(DateTimeFormat.getFormat("MMM dd yyyy"));  
	    configs.add(column);  
	  
	    CheckColumnConfig checkColumn = new CheckColumnConfig("indoor", "Indoor?", 55);  
	    CellEditor checkBoxEditor = new CellEditor(new CheckBox());  
	    checkColumn.setEditor(checkBoxEditor);  
	    configs.add(checkColumn);  
	  
	    final ListStore<BaseModelData> store = new ListStore<BaseModelData>();
	    BaseModelData data1 = new BaseModelData();
	    data1.set("name", "example name");
	    store.add(data1);  
	  
	    ColumnModel cm = new ColumnModel(configs);  
	  
	    LayoutContainer cp = new LayoutContainer();  
	    cp.setLayout(new FitLayout());  
	  
	    final RowEditor<BaseModelData> re = new RowEditor<BaseModelData>();  
	    final Grid<BaseModelData> grid = new Grid<BaseModelData>(store, cm);  
	    grid.setAutoExpandColumn("name");  
	    grid.setBorders(true);  
	    grid.addPlugin(checkColumn);  
	    grid.addPlugin(re);  	    
	    grid.setHeight(250);

	    cp.add(grid);  
	  
	    ToolBar toolBar = new ToolBar();  
	    com.extjs.gxt.ui.client.widget.button.Button add = new com.extjs.gxt.ui.client.widget.button.Button("Add Plant");  
	    add.addSelectionListener(new SelectionListener<ButtonEvent>() {  
	  
	      @Override  
	      public void componentSelected(ButtonEvent ce) {  
	    	BaseModelData plant = new BaseModelData();  
	        plant.set("name", "New Plant 1");  
	        plant.set("light", "Mostly Shady");  
	        plant.set("price", 0);  
	        plant.set("available", new DateWrapper().clearTime().asDate());  
	        plant.set("indoor", false);  
	  
	        re.stopEditing(false);  
	        store.insert(plant, 0);  
	        re.startEditing(store.indexOf(plant), true);  
	  
	      }  
	  
	    });  
	    toolBar.add(add);	    
	    cp.add(toolBar);  
  
	    cp.add(new com.extjs.gxt.ui.client.widget.button.Button("Reset", new SelectionListener<ButtonEvent>() {  
	  
	      @Override  
	      public void componentSelected(ButtonEvent ce) {  
	        store.rejectChanges();  
	      }  
	    }));  
	  
	    cp.add(new com.extjs.gxt.ui.client.widget.button.Button("Save", new SelectionListener<ButtonEvent>() {  
	  
	      @Override  
	      public void componentSelected(ButtonEvent ce) {  
	        store.commitChanges();  
	      }  
	    }));
	    
	    tablePanel.setWidget(cp);
	}

	private void setupQuery() {
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
	}

}
