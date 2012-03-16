package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.ClientLogger;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.widget.entity.row.EntityRowModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CellSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Editor of a list of primitives
 * 
 * @author John
 * 
 */
public class ListEditorGrid<String> extends LayoutContainer {

	ContentPanel cp;
	ListStore<ListItem<String>> store;
	Field<String> field;
	EditorGrid<ListItem<String>> grid;
	ColumnModel columnModel;
	IconsImageBundle iconBundle;
	ClientLogger log;

	public ListEditorGrid(IconsImageBundle iconBundle, ClientLogger log) {
		this.iconBundle = iconBundle;
		this.log = log;
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		cp = new ContentPanel();
		cp.setBodyBorder(false);
		cp.setButtonAlign(HorizontalAlignment.CENTER);
		cp.setLayout(new FitLayout());
		cp.setHeaderVisible(false);
		cp.setScrollMode(Scroll.AUTOY);
		int oneWidth = 120;
		int twoWidth = 45;
		int scrollWidth = 0;
		int totalWidth = oneWidth+ twoWidth+ scrollWidth;
		cp.setSize(totalWidth, 200);

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		// Label
		ColumnConfig column = new ColumnConfig();
		column.setId(ListItem.VALUE);
		column.setHeader("Value");
		column.setRowHeader(false);
		column.setWidth(oneWidth);
		column.setEditor(new CellEditor(field));
		configs.add(column);
		// remove column
		column = new ColumnConfig();
		column.setId(ListItem.REMOVE_COLUMN_ID);
		column.setHeader("");
		column.setAlignment(HorizontalAlignment.LEFT);
		column.setWidth(twoWidth);
		column.setRenderer(createRemoveRenderer());
		configs.add(column);

		columnModel = new ColumnModel(configs);

		grid = new EditorGrid<ListItem<String>>(
				new ListStore<ListItem<String>>(), columnModel);
		// the delete button column absorbs width changes due to the scroll
		// bars being shown or hidden.
		grid.setAutoExpandColumn(ListItem.REMOVE_COLUMN_ID);
		grid.setAutoExpandMax(100);
		grid.setAutoExpandMin(25);
		grid.setBorders(false);
		grid.setStripeRows(false);
		grid.setColumnLines(true);
		grid.setColumnReordering(false);
		grid.setHideHeaders(true);
		grid.setTrackMouseOver(true);
		grid.setShadow(false);
		grid.getAriaSupport().setLabelledBy(cp.getHeader().getId() + "-label");
		grid.setSelectionModel(new CellSelectionModel<ListItem<String>>());
		
		grid.addListener(Events.ValidateEdit, new Listener<GridEvent<ListItem<String>>>() {
			@Override
			public void handleEvent(GridEvent<ListItem<String>> be) {
				// If this is an edit of the last cell then add another cell to the model
				if(isLastIndex(be.getRowIndex())){
					Object value = be.getValue();
					if(value != null && !"".equals(value) ){
						// Add a new empty value at the end.
						ListItem<String> model = new ListItem<String>((String)"");
						store.add(model);
					}
				}
			}
		});
		// when this component gets the focus forward it to the grid.
		this.addListener(Events.Focus, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				// this does not seem to work as expected :(
				grid.getView().focusCell(0, 0, true);
				
			}
		});
		
		cp.add(grid);
		this.add(cp);
		rebuild();
	}
	
	public void stopEditing(){
		// This should apply any changes the user was making.
		grid.stopEditing(false);
	}
	
	/**
	 * Is this the last index?
	 * @param index
	 * @return
	 */
	private boolean isLastIndex(int index){
		return store.getCount()-1 == index;
	}

	private GridCellRenderer<ListItem<String>> createRemoveRenderer() {
		GridCellRenderer<ListItem<String>> removeButton = new GridCellRenderer<ListItem<String>>() {

			@Override
			public Object render(final ListItem<String> model,
					java.lang.String property, ColumnData config,
					int rowIndex, int colIndex,
					final ListStore<ListItem<String>> store,
					Grid<ListItem<String>> grid) {
				Anchor removeAnchor = new Anchor();
				StringBuilder builder = new StringBuilder();
				builder.append("<div>");
				builder.append(AbstractImagePrototype.create(iconBundle.delete16Grey()).getHTML());
				builder.append("</div>");
				removeAnchor.setHTML(builder.toString());
				removeAnchor.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// First determine what the index of the element currently
						// We cannot count on the passed row index because it can change if items are deleted from the list
						int index = store.indexOf(model);
						// Remove this row as long as it is not the last index
						if(!isLastIndex(index)){
							//log.debug("Deleting row: "+index);
							store.remove(model);
						}
					}
				});
				return removeAnchor;
			}
		};
		return removeButton;
	}

	/**
	 * Rebuild this component.
	 */
	public void rebuild() {
		// there is nothing to do if we have not been rendered.
		if (!this.isRendered())
			return;
		// Create a grid
		grid.reconfigure(store, columnModel);
		this.layout(true);
	}

	/**
	 * Set the list of data to be edited along with with the editor.
	 * 
	 * @param list
	 * @param field
	 */
	public void setList(List<String> list, Field<String> field) {
		this.field = field;
		// Build up our list model from the passed list
		store = new ListStore<ListItem<String>>();
		for (String it : list) {
			ListItem<String> model = new ListItem<String>(it);
			store.add(model);
		}
		// Add an empty cell to the end
		ListItem<String> model = new ListItem<String>((String)"");
		store.add(model);
		rebuild();

	}

	/**
	 * Get the resulting list from this editor.
	 * 
	 * @return
	 */
	public List<String> getList() {
		List<String> results = new ArrayList<String>();
		// Read from the store
		// Note the last cell is always empty hence the store.getCount()-1
		for (int i = 0; i < store.getCount()-1; i++) {
			ListItem<String> model = store.getAt(i);
			results.add(model.getItem());
		}
		return results;
	}

}
