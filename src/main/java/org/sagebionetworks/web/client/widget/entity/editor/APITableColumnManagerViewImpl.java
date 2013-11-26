package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableColumnManagerViewImpl extends LayoutContainer implements APITableColumnManagerView {
	
	private static final int COLUMN_WIDTH_PX = 210;
	private static final String CONFIG_COL_KEY = "config";
	private static final String DATA_TOKEN_KEY = "dataKey";
	private static final String DATA_NAME_KEY = "dataName";

	Grid<BaseModelData> grid;
	ListStore<BaseModelData> gridStore;
	ColumnModel columnModel;
	private Presenter presenter;
	private IconsImageBundle iconsImageBundle;
	private boolean isEmpty;
	private Map<Integer, APITableColumnConfig> token2ColumnConfig;
	
	
	@Inject
	public APITableColumnManagerViewImpl(IconsImageBundle iconsImageBundle) {
		this.iconsImageBundle = iconsImageBundle;
		gridStore = new ListStore<BaseModelData>();
	}
	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		this.setBorders(true);
		
		FlowPanel mainPanel = new FlowPanel();
		//header and add button
		LayoutContainer c = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
        c.setLayout(layout);
        c.add(new Html("<h5>Column Configuration</h5>"), new HBoxLayoutData(new Margins(0, 5, 0, 0)));
        Anchor addBtn = new Anchor();
        addBtn.setHTML(DisplayUtils.getIconHtml(iconsImageBundle.add16()));
        addBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//pop up new dialog to gather column information
				showAddColumnDialog();
			}
		});
        c.add(addBtn, new HBoxLayoutData(new Margins(0)));
		
        mainPanel.add(c);
	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
	    GridCellRenderer<BaseModelData> valueRenderer = createValueRenderer();
	    
	    ColumnConfig column = new ColumnConfig();  
	    column.setId(CONFIG_COL_KEY);  
	    column.setHeader("Column Configuration");  	    	   
	    column.setRowHeader(false);
	    column.setWidth(COLUMN_WIDTH_PX);
		column.setRenderer(valueRenderer);
	    configs.add(column);  
	  	  	     	 
	    columnModel = new ColumnModel(configs);  
	  
	    grid = new Grid<BaseModelData>(gridStore, columnModel);  
	    grid.setStyleAttribute("borderTop", "none");  
	    grid.setAutoExpandColumn(CONFIG_COL_KEY);  
		grid.setAutoExpandMin(100);
		// This is important, the grid must resize to fit its height.
		grid.setAutoHeight(true);
		grid.setAutoWidth(false);
		grid.setBorders(true);
		grid.setStripeRows(false);
		grid.setColumnLines(false);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(false);
		grid.setShadow(false);		
		
		mainPanel.add(grid);
		
		this.add(mainPanel);
	}
	
	@Override
	public void configure(List<APITableColumnConfig> configs) {
		token2ColumnConfig = new HashMap<Integer, APITableColumnConfig>();
		gridStore.removeAll();
		isEmpty = configs == null || configs.size() == 0;
		if(isEmpty){
			addNoConfigRow();
		} else {
			populateStore(configs);			
		}
		
		if(isRendered())
			grid.reconfigure(gridStore, columnModel);
		this.layout(true);		
	}

	/**
	 * Show the add column dialog
	 * 
	 */
	public void showAddColumnDialog() {
		// Show a form for adding an Annotations
		final Dialog dialog = new Dialog();
		dialog.setMaximizable(false);
		dialog.setSize(400, 175);
		dialog.setPlain(true);
		dialog.setModal(true);
		dialog.setButtons(Dialog.OKCANCEL);
		dialog.setHideOnButtonClick(true);
		dialog.setHeading("Column Configuration");
		dialog.setLayout(new FitLayout());
		dialog.setBorders(false);
		Button okButton = dialog.getButtonById(Dialog.OK);

		//define the inputs
		//drop down of available renderers
		//input column names (required)
		//display column name (optional)
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("margin-top-left-10");
		final TextField<String> columnNames = new TextField<String>();
		final TextField<String> displayColumnName = new TextField<String>();
		final SimpleComboBox<String> combo = new SimpleComboBox<String>();
		combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
		combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID);
		combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE);
		combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE);
		combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID);
		//combo.add(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS);	//don't want to expose
		combo.setSimpleValue(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
		combo.setTriggerAction(TriggerAction.ALL);
		
		final SimpleComboBox<String> sortCb = new SimpleComboBox<String>();
		sortCb.add(COLUMN_SORT_TYPE.NONE.toString());
		sortCb.add(COLUMN_SORT_TYPE.DESC.toString());
		sortCb.add(COLUMN_SORT_TYPE.ASC.toString());
		sortCb.setSimpleValue(COLUMN_SORT_TYPE.NONE.toString());
		sortCb.setTriggerAction(TriggerAction.ALL);
		
		initNewField("Renderer", combo, panel);
		initNewField("Input Column Names", columnNames, panel);
		initNewField("Display Column Name (optional)", displayColumnName, panel);
		initNewField("Sort", sortCb, panel);
		
		okButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.addColumnConfig(combo.getValue().getValue(), columnNames.getValue(), displayColumnName.getValue(), COLUMN_SORT_TYPE.valueOf(sortCb.getValue().getValue()));
			}
	    });

		dialog.add(panel);
		dialog.show();
	}
	
	private LayoutContainer initNewField(String label, Field field, FlowPanel container) {
		HorizontalPanel hp= new HorizontalPanel();
		
		Label labelField = new Label(label);
		labelField.setWidth(140);
		field.setWidth(198);
		hp.add(labelField);
		hp.add(field);
		
		container.add(hp);
		return hp;
	}


	private void addNoConfigRow() {
		BaseModelData model = new BaseModelData();
		model.set(CONFIG_COL_KEY, DisplayConstants.TEXT_NO_COLUMNS);
		gridStore.add(model);
	}
	
	private void populateStore(List<APITableColumnConfig> configs) {		
		int i = 0;
		for(APITableColumnConfig data: configs){
			SafeHtmlBuilder builder = new SafeHtmlBuilder();
			builder.appendHtmlConstant("<div style=\"margin-left:20px\">");
			builder.appendEscaped(data.getDisplayColumnName());
			builder.appendHtmlConstant("</div>");
			Html listItem = new Html(builder.toSafeHtml().asString());
		    BaseModelData model = new BaseModelData();
			model.set(CONFIG_COL_KEY, listItem.getHtml());
			model.set(DATA_TOKEN_KEY, i);
			model.set(DATA_NAME_KEY, SafeHtmlUtils.fromString(data.getDisplayColumnName()).asString());
			gridStore.add(model);
			token2ColumnConfig.put(i, data);
			i++;
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
				SimplePanel div = new SimplePanel();
				div.addStyleName("attachments-widget-row");
				div.add(new Html(value));

				HorizontalPanel panel = new HorizontalPanel();
				LayoutContainer wrap = new LayoutContainer();
				wrap.add(div);
				wrap.setWidth(COLUMN_WIDTH_PX-50);
				
				panel.add(wrap);
				if (!isEmpty) {
					AbstractImagePrototype img = AbstractImagePrototype.create(iconsImageBundle.deleteButtonGrey16());
					Anchor button = DisplayUtils.createIconLink(img, new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							deleteColumnConfigAt(rowIndex);
						}
					});
	
					TableData td = new TableData();
					td.setHorizontalAlign(HorizontalAlignment.RIGHT);
					td.setVerticalAlign(VerticalAlignment.MIDDLE);
					panel.add(button, td);
				}
					
				panel.setAutoWidth(true);
				return panel;
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
	}

	@Override
	public void clear() {
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
	public void deleteColumnConfigAt(int rowIndex) {
		final BaseModelData model = grid.getStore().getAt(rowIndex);
		if (model != null) {
			Integer dataIndex = (Integer)model.get(DATA_TOKEN_KEY);
			presenter.deleteColumnConfig(token2ColumnConfig.get(dataIndex));
		}
	}
}
