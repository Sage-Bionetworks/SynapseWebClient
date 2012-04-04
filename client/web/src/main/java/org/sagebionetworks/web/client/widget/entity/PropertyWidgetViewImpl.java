package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class PropertyWidgetViewImpl extends LayoutContainer implements PropertyWidgetView, IsWidget {

	ListStore<EntityRowModel> gridStore;
	Grid<EntityRowModel> grid;
	ColumnModel columnModel;

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		// the label renderer
		GridCellRenderer<EntityRowModel> labelRenderer = createLabelRenderer();

		// Renderer for the value column
		GridCellRenderer<EntityRowModel> valueRenderer = createValueRenderer();
		// Create a grid
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		// Label
		ColumnConfig column = new ColumnConfig();
		column.setId(EntityRowModel.LABEL);
		column.setHeader("Label");
		column.setWidth(100);
		column.setRowHeader(false);
		column.setRenderer(labelRenderer);
		column.setAlignment(HorizontalAlignment.RIGHT);
		configs.add(column);
		// Value
		column = new ColumnConfig();
		column.setId(EntityRowModel.VALUE);
		column.setHeader("Value");
		column.setWidth(156);
		column.setRowHeader(false);
		column.setRenderer(valueRenderer);
		configs.add(column);

		columnModel = new ColumnModel(configs);

		grid = new Grid<EntityRowModel>(gridStore,columnModel);
		grid.setAutoExpandColumn(EntityRowModel.VALUE);
		grid.setAutoExpandMin(100);
		grid.setAutoExpandMax(300);
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
		this.add(grid);
		rebuild();
	}

	/**
	 * Rebuild this component.
	 */
	public void rebuild() {
		// there is nothing to do if we have not been rendered.
		if(!this.isRendered()) return;
		grid.reconfigure(gridStore,columnModel);		
		this.layout(true);
	}

	/**
	 * The value renderer
	 * @return
	 */
	public GridCellRenderer<EntityRowModel> createValueRenderer() {
		GridCellRenderer<EntityRowModel> valueRenderer = new GridCellRenderer<EntityRowModel>() {

			@Override
			public Object render(EntityRowModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<EntityRowModel> store, Grid<EntityRowModel> grid) {
				String value = model.get(property);
				if (value == null) {
					value = "";
				}
				StringBuilder builder = new StringBuilder();
				builder.append("<div style='font-weight: normal;color:black; overflow:hidden; text-overflow:ellipsis; width:auto;'>");
				builder.append(value);
				builder.append("</div>");
				Html html = new Html(builder.toString());
//				html.setWidth(50);
			    ToolTipConfig tipsConfig = new ToolTipConfig();  
			    tipsConfig.setTitle(model.getToolTipTitle());  
			    tipsConfig.setText(model.getToolTipBody());
			    tipsConfig.setMouseOffset(new int[] {0, 0});  
			    tipsConfig.setAnchor("left");  
			    tipsConfig.setDismissDelay(0);
			    tipsConfig.setShowDelay(100);
			    ToolTip tip = new ToolTip(html, tipsConfig);
				return html;
			}

		};
		return valueRenderer;
	}

	/**
	 * The label renderer
	 * @return
	 */
	public GridCellRenderer<EntityRowModel> createLabelRenderer() {
		// Renderer for the label column
		GridCellRenderer<EntityRowModel> labelRenderer = new GridCellRenderer<EntityRowModel>() {

			@Override
			public Object render(EntityRowModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<EntityRowModel> store, Grid<EntityRowModel> grid) {
				String value = model.get(property);

				StringBuilder builder = new StringBuilder();
				builder.append("<div style='font-weight:bold; color:grey; white-space:normal; overflow:hidden; text-overflow:ellipsis;'>");
				builder.append(value);
				builder.append(":</div>");
				Html html = new Html(builder.toString());
				return html;
			}

		};
		return labelRenderer;
	}

	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	public void setRows(List<EntityRow<?>> rows) {
		// Build the store from the rows.
		this.gridStore = GridStoreFactory.createListStore(rows);
		this.rebuild();
	}

}
