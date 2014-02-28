package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

public class SimpleTableWidgetViewImpl extends Composite implements SimpleTableWidgetView {
	public interface Binder extends UiBinder<Widget, SimpleTableWidgetViewImpl> {	}

	@UiField
	SimplePanel tableContainer;
	@UiField
	SimplePanel pagerContainer;

	CellTable<TableModel> cellTable;
	SimplePager pager;
	private List<ColumnModel> columns;
	ListHandler<TableModel> sortHandler;
	SelectionModel<TableModel> selectionModel;
	ContactDatabase database;
	Presenter presenter;

	@Inject
	public SimpleTableWidgetViewImpl(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
		
		tableContainer.addStyleName("tableMinWidth");
	}
	
	@Override
	public void configure(List<ColumnModel> columns, RowSet rowset, boolean canEdit, Integer offset, Integer limit) {	
		this.columns = columns;					
				
		buildTable();
		buildColumns(columns, canEdit);
		
		// TODO : REMOVE THIS
		// Add the CellList to the adapter in the database.
		database = ContactDatabase.get();
		database.addDataDisplay(cellTable);
	}

	
//	public void setSelected(boolean isSelected) {
//		int start = pager.getPageStart();
//		int size = pager.getPageSize();
//		HasRows pag = pager.getDisplay();
//		pag.
//		
//	}
	
	private void buildTable() {
		cellTable = new CellTable<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setWidth("100%", true);
		cellTable.setPageSize(15);
		cellTable.addStyleName("cellTable");
		tableContainer.setWidget(cellTable);

		// Do not refresh the headers and footers every time the data is
		// updated.
//		cellTable.setAutoHeaderRefreshDisabled(true);
//		cellTable.setAutoFooterRefreshDisabled(true);

		// Attach a column sort handler to the ListDataProvider to sort the
		// list.
		sortHandler = new ListHandler<TableModel>(ContactDatabase.get().getDataProvider().getList());
		cellTable.addColumnSortHandler(sortHandler);

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(cellTable);
		pagerContainer.setWidget(pager);

		// Add a selection model so we can select cells.
		selectionModel = new MultiSelectionModel<TableModel>(TableModel.KEY_PROVIDER);
		cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TableModel> createCheckboxManager());
		
	}

	private void buildColumns(List<ColumnModel> columns, boolean canEdit) {
		// checkbox selector
		Column<TableModel, Boolean> checkColumn = new Column<TableModel, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(TableModel object) {
				// Get the value from the selection model.
				return selectionModel.isSelected(object);
			}		
		}; 
		cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
		cellTable.setColumnWidth(checkColumn, 40, Unit.PX);

		int nPercentageCols = columns.size();
		for(ColumnModel model : columns) {
			// TODO : uncomment and replace below when we have DATE column type
			if(model.getColumnType() == ColumnType.BOOLEAN) nPercentageCols--;	
//			if(model.getColumnType() == ColumnType.BOOLEAN || model.getColumnType() == ColumnType.DATE) nPercentageCols--;
		}		
		
		// Table's columns
		for(ColumnModel model : columns) {
			Column<TableModel, ?> column = ColumnUtils.getColumn(model, sortHandler, canEdit);
			cellTable.addColumn(column, model.getName());
			// TODO : just have a fixed width for each column and let table scroll horizontally?
			if(model.getColumnType() == ColumnType.BOOLEAN) {
				cellTable.setColumnWidth(column, 100, Unit.PX);
//			} else if(model.getColumnType() == ColumnType.DATE) {
//				cellTable.setColumnWidth(column, 140, Unit.PX);								
			} else {
				cellTable.setColumnWidth(column, (100/nPercentageCols), Unit.PCT);			
			}
		}
	}

	
	/*
	 * SynapseView methods
	 */
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}
	
	@Override
	public void clear() {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

}
