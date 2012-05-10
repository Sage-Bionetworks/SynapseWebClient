package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.Element;

public class ListPicker extends Grid<ListItem<String>> {
	
	List<String> list;
	
	public ListPicker(){
		super();
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		// Label
		ColumnConfig column = new ColumnConfig();
		column.setId(ListItem.VALUE);
		column.setHeader("Value");
		column.setWidth(100);
		column.setRowHeader(false);
		CellEditor editor = new CellEditor(new TextField<Object>());
		column.setEditor(editor);
//		column.setAlignment(HorizontalAlignment.RIGHT);
		configs.add(column);
		
		cm = new ColumnModel(configs);
		store = new ListStore<ListItem<String>>();
	}

	public void setValue(List<String> list) {
		this.list = list;
		// Create a grid
//		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
//
//		// Label
//		ColumnConfig column = new ColumnConfig();
//		column.setId(ListItem.VALUE);
//		column.setHeader("Value");
//		column.setWidth(100);
//		column.setRowHeader(false);
//		CellEditor editor = new CellEditor(new TextField<Object>());
//		column.setEditor(editor);
////		column.setAlignment(HorizontalAlignment.RIGHT);
//		configs.add(column);
//		
//		ColumnModel cm = new ColumnModel(configs);
		// Create the store
		ListStore<ListItem<String>> store = new ListStore<ListItem<String>>();
		for(String it: list){
			ListItem<String> model = new ListItem<String>(it);
			store.add(model);
		}
		this.reconfigure(store, cm);

	}

	public List<String> getValue() {
		return list;
	}

//	@Override
//	protected void onRender(Element target, int index) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("<ul>");
//		for(String child: list){
//			builder.append("<li>");
//			builder.append(child);
//			builder.append("</li>");
//		}
//		builder.append("</ul>");
//
//		setElement(XDOM.create(builder.toString()));
//		el().insertInto(target, index);
//	}

}
