package org.sagebionetworks.web.client.widget.table;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;

public class TableUtils {
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
	
	public static Column<TableModel, ?> getColumn(ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit) {
		if(col.getColumnType() == ColumnType.STRING) {
			if(!canEdit || col.getEnumValues() == null || col.getEnumValues().size() == 0) {				
			    return configSimpleText(col, sortHandler, canEdit); // Simple text field    				
			} else {				  
			    return configComboString(col, canEdit); // Enum combo box  
			}
		} else if(col.getColumnType() == ColumnType.DOUBLE) {
			return configNumberField(col, sortHandler, true, canEdit);   
		} else if(col.getColumnType() == ColumnType.LONG) {
			return configNumberField(col, sortHandler, false, canEdit);   
		} else if(col.getColumnType() == ColumnType.BOOLEAN) {			
			if(canEdit) return configBooleanCombo(col); 
			else return configSimpleText(col, sortHandler, canEdit);			
		} else if(col.getColumnType() == ColumnType.FILEHANDLEID) {
			return configFileHandle(col, canEdit);  
//		} else if(col.getColumnType() == ColumnType.DATE) {
//			return configDateColumn(col, sortHandler, canEdit);
		} else {
			return null;
		} 
	}

	public static QueryProblem parseQueryProblem(String message) {
		if(message.contains("blah")) {
			return QueryProblem.UNRECOGNIZED_COLUMN;
		} else {
			return QueryProblem.UNKNOWN;
		}
	}
	
	
	/*
	 * Private Methods
	 */
	private static Column<TableModel, String> configComboString(final ColumnModel col, boolean canEdit) {
		if(col.getEnumValues() == null) return null;
		SelectionCell comboCell = new SelectionCell(col.getEnumValues());
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
				@Override
				public void update(int index, TableModel object, String value) {
					for (String enumVal : col.getEnumValues()) {
						if (enumVal.equals(value)) {
							object.put(col.getId(), enumVal);
						}
					}
					ContactDatabase.get().refreshDisplays();
				}
			});
		}
		return column;
	}

	private static Column<TableModel, String> configSimpleText(final ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit) {
		AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
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
						public void update(int index, TableModel object,
								String value) {
							// Called when the user changes the value.
							object.put(col.getId(), value);
							ContactDatabase.get().refreshDisplays();
						}
					});
		}
		return column;
	}

	private static Column<TableModel, String> configNumberField(final ColumnModel col, ListHandler<TableModel> sortHandler, final boolean isDouble, boolean canEdit) {
		AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
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
						public void update(int index, TableModel object, String value) {						
							try {
								if(isDouble) {
									Double.parseDouble(value);
								} else {
									Long.parseLong(value);
								}
								// save value after validation
								object.put(col.getId(), value);
								ContactDatabase.get().refreshDisplays();
							} catch(NumberFormatException e) {
								// TODO : better way to alert view?
								Window.alert("Number not valid: " + value);
							}
							ContactDatabase.get().refreshDisplays();
						}
					});
		}
		return column;
	}

	private static Column<TableModel, String> configBooleanCombo(final ColumnModel col) {
		SelectionCell comboCell = new SelectionCell(Arrays.asList(new String[] { TRUE, FALSE }));
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				return object.get(col.getId());
			}
		};
		column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
			@Override
			public void update(int index, TableModel object, String value) {				
				if (TRUE.equals(value)) {
					object.put(col.getId(), TRUE);
				} else {
					object.put(col.getId(), FALSE);
				}			
				ContactDatabase.get().refreshDisplays();
			}
		});
		return column;
	}
	
	private static Column<TableModel, ?> configFileHandle(final ColumnModel col, boolean canEdit) {		
	Column<TableModel, String> column = new Column<TableModel, String>(new FileHandleCell(canEdit)) {
		@Override
		public String getValue(TableModel object) {
			return object.get(col.getId());
		}
	};
	
	return column;
		
	}	
	
	private static Column<TableModel, Date> configDateColumn(final ColumnModel col, ListHandler<TableModel> sortHandler, boolean canEdit) {
		
		AbstractCell<Date> cell = canEdit ? new DatePickerCell(DATE_FORMAT) : new DateCell(DATE_FORMAT);
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
						public void update(int index, TableModel object, Date value) {					
							object.put(col.getId(), String.valueOf(value.getTime()));
							ContactDatabase.get().refreshDisplays();
						}
					});
		}
				
		return column;
	}	

}
