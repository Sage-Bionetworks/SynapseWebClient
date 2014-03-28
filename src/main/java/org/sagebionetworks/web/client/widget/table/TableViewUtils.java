package org.sagebionetworks.web.client.widget.table;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableViewUtils {
	static final String TRUE = Boolean.TRUE.toString().toLowerCase();
	static final String FALSE = Boolean.FALSE.toString().toLowerCase();
	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
	
	static final Map<ColumnType,String> columnToDisplayName;
	static {
		columnToDisplayName = new HashMap<ColumnType, String>();
		columnToDisplayName.put(ColumnType.STRING, "String");
		columnToDisplayName.put(ColumnType.LONG, "Integer");
		columnToDisplayName.put(ColumnType.DOUBLE, "Double");
		columnToDisplayName.put(ColumnType.BOOLEAN, "Boolean");
		columnToDisplayName.put(ColumnType.FILEHANDLEID, "File");
	}
	
	public static String getColumnDisplayName(ColumnType type) {
		return columnToDisplayName.containsKey(type) ? columnToDisplayName.get(type) : "Unknown Type";
	}
	
	public static Column<TableModel, ?> getColumn(ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit, final RowUpdater rowUpdater, CellTable<TableModel> cellTable, SynapseView view) {
		if(col.getColumnType() == ColumnType.STRING) {
			if(!canEdit || col.getEnumValues() == null || col.getEnumValues().size() == 0) {				
			    return configSimpleText(col, sortHandler, canEdit, rowUpdater, cellTable, view); // Simple text field    				
			} else {				  
			    return configComboString(col, canEdit, rowUpdater, cellTable, view); // Enum combo box  
			}
		} else if(col.getColumnType() == ColumnType.DOUBLE) {
			return configNumberField(col, sortHandler, true, canEdit, rowUpdater, cellTable, view);   
		} else if(col.getColumnType() == ColumnType.LONG) {
			return configNumberField(col, sortHandler, false, canEdit, rowUpdater, cellTable, view);   
		} else if(col.getColumnType() == ColumnType.BOOLEAN) {			
			if(canEdit) return configBooleanCombo(col, rowUpdater, cellTable, view); 
			else return configSimpleText(col, sortHandler, canEdit, rowUpdater, cellTable, view);			
		} else if(col.getColumnType() == ColumnType.FILEHANDLEID) {
			return configFileHandle(col, canEdit, rowUpdater, cellTable, view);  
//		} else if(col.getColumnType() == ColumnType.DATE) {
//			return configDateColumn(col, sortHandler, canEdit, rowUpdater, cellTable, view);
		} else {
			return null;
		} 
	}	
	
	
	/*
	 * Private Methods
	 */
	private static Column<TableModel, String> configComboString(final ColumnModel col, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		if(col.getEnumValues() == null) return null;
		final SelectionCell comboCell = new SelectionCell(col.getEnumValues());
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
				@Override
				public void update(int index, final TableModel object, String value) {
					final String original = object.get(col.getId()); 
					for (String enumVal : col.getEnumValues()) {
						if (enumVal.equals(value)) {
							object.put(col.getId(), enumVal);
						}
					}
					rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
						@Override
						public void onSuccess(RowReferenceSet result) { 
							checkForTempRowId(object, result, view);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							object.put(col.getId(), original);
							comboCell.clearViewData(TableModel.KEY_PROVIDER.getKey(object));
							cellTable.redraw();
						}
					});
				}
			});
		}
		return column;
	}

	private static Column<TableModel, String> configSimpleText(final ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		final AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
		Column<TableModel, String> column = new Column<TableModel, String>(cell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		column.setSortable(true);
		sortHandler.setComparator(column,
				new Comparator<TableModel>() {
					@Override
					public int compare(TableModel o1, TableModel o2) {
						return o1.get(col.getId()).compareTo(o2.get(col.getId()));
					}
				});		
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
						@Override
						public void update(int index, final TableModel object,
								final String value) {
							final String original = object.get(col.getId());
							object.put(col.getId(), value);
							rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
								@Override
								public void onSuccess(RowReferenceSet result) {
									checkForTempRowId(object, result, view);
								}

								@Override
								public void onFailure(Throwable caught) {
									object.put(col.getId(), original);
									((EditTextCell)cell).clearViewData(TableModel.KEY_PROVIDER.getKey(object));
									cellTable.redraw();
								}
							});
						}
					});
		}
		return column;
	}

	private static Column<TableModel, String> configNumberField(final ColumnModel col, ListHandler<TableModel> sortHandler, final boolean isDouble, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		final AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
		Column<TableModel, String> column = new Column<TableModel, String>(cell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		column.setSortable(true);
		sortHandler.setComparator(column,
				new Comparator<TableModel>() {
					@Override
					public int compare(TableModel o1, TableModel o2) {
						if(isDouble) {
							return Double.valueOf(o1.get(col.getId())) == Double.valueOf(o2.get(col.getId())) ? 0 : Double.valueOf(o1.get(col.getId())) < Double.valueOf(o2.get(col.getId())) ? -1 : 1;
						} else {
							return Long.valueOf(o1.get(col.getId())) == Long.valueOf(o2.get(col.getId())) ? 0 : Long.valueOf(o1.get(col.getId())) < Long.valueOf(o2.get(col.getId())) ? -1 : 1;
						}						
					}
				});		
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
						@Override
						public void update(int index, final TableModel object, String value) {						
							final String original = object.get(col.getId()); 
							try {
								if(isDouble) {
									Double.parseDouble(value);
								} else {
									Long.parseLong(value);
								}
								// save value after validation
								object.put(col.getId(), value);
								rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
									@Override
									public void onSuccess(RowReferenceSet result) { 
										checkForTempRowId(object, result, view);
									}
									
									@Override
									public void onFailure(Throwable caught) {
										object.put(col.getId(), original);
										((EditTextCell)cell).clearViewData(TableModel.KEY_PROVIDER.getKey(object));
										cellTable.redraw();
									}
								});
							} catch(NumberFormatException e) {
								view.showErrorMessage(DisplayConstants.NUMBER_NOT_VALID + ": "+ value);
							}
						}
					});
		}
		return column;
	}

	private static Column<TableModel, String> configBooleanCombo(final ColumnModel col, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		final SelectionCell comboCell = new SelectionCell(Arrays.asList(new String[] { TRUE, FALSE }));
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
			@Override
			public void update(int index, final TableModel object, String value) {				
				final String original = object.get(col.getId()); 
				if (TRUE.equals(value)) {
					object.put(col.getId(), TRUE);
				} else {
					object.put(col.getId(), FALSE);
				}			
				rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
					@Override
					public void onSuccess(RowReferenceSet result) {
						checkForTempRowId(object, result, view);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						object.put(col.getId(), original);
						comboCell.clearViewData(TableModel.KEY_PROVIDER.getKey(object));
						cellTable.redraw();						
					}
				});
			}
		});
		return column;
	}
	
	private static Column<TableModel, ?> configFileHandle(final ColumnModel col, boolean canEdit, final RowUpdater rowUpdater, CellTable<TableModel> cellTable, SynapseView view) {		
	Column<TableModel, String> column = new Column<TableModel, String>(new FileHandleCell(canEdit)) {
		@Override
		public String getValue(TableModel object) {
			return object.get(col.getId());
		}
	};
	// TODO : complete
	return column;
		
	}	
	
	private static Column<TableModel, Date> configDateColumn(final ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {		
		final AbstractCell<Date> cell = canEdit ? new DatePickerCell(DATE_FORMAT) : new DateCell(DATE_FORMAT);
		Column<TableModel, Date> column = new Column<TableModel, Date>(cell) {
			@Override
			public Date getValue(TableModel object) {
				try {
					return new Date(Long.parseLong(object.get(col.getId())));
				} catch (Exception e) {
					return null; 
				}
			}					
		};
		column.setSortable(true);
		sortHandler.setComparator(column,
				new Comparator<TableModel>() {
					@Override
					public int compare(TableModel o1, TableModel o2) {						
						return Long.valueOf(o1.get(col.getId())) == Long.valueOf(o2.get(col.getId())) ? 0 : Long.valueOf(o1.get(col.getId())) < Long.valueOf(o2.get(col.getId())) ? -1 : 1;
					}
				});		
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, Date>() {
						@Override
						public void update(int index, final TableModel object, Date value) {					
							final String original = object.get(col.getId()); 
							object.put(col.getId(), String.valueOf(value.getTime()));
							rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
								@Override
								public void onSuccess(RowReferenceSet result) { 
									checkForTempRowId(object, result, view);
								}
								
								@Override
								public void onFailure(Throwable caught) {
									object.put(col.getId(), original);
									((DatePickerCell)cell).clearViewData(TableModel.KEY_PROVIDER.getKey(object));
									cellTable.redraw();
								}
							});
						}
					});
		}
				
		return column;
	}	

	/**
	 * Check if the TableModel has a temporary id and if so replace with first row in the RowReferenceSet
	 * This is for new Rows added to the view after their first cell update. Future updates need to reference the actual rowId
	 * created in the first update otherwise duplicate rows will result.
	 * @param object
	 * @param result
	 * @param view
	 */
	private static void checkForTempRowId(final TableModel object, RowReferenceSet result, SynapseView view) {
		// set rowId if this was a UI added row
		if(object.getId().startsWith(TableModel.TEMP_ID_PREFIX)) {										
			if (result != null
					&& result.getRows() != null
					&& result.getRows().size() > 0
					&& result.getRows().get(0) != null
					&& result.getRows().get(0).getRowId() != null) {
				object.setId(result.getRows().get(0).getRowId().toString());
			} else {
				// page reload should fix any row id/view sync issues
				view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		}
	}
	
	
}
