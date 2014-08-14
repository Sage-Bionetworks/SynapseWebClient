package org.sagebionetworks.web.client.widget.table;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.table.SimpleTableWidgetView.Presenter;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class TableViewUtils {
	private static final int DEFAULT_STRING_MAX_LENGTH = 50;
	static final String TRUE = Boolean.TRUE.toString().toLowerCase();
	static final String FALSE = Boolean.FALSE.toString().toLowerCase();
	static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
	static PortalGinInjector ginInjector;
	
	static final Map<ColumnType,String> columnToDisplayName;
	static final Map<ColumnType,Integer> columnToDisplayWidth;
	static {
		columnToDisplayName = new HashMap<ColumnType, String>();
		columnToDisplayName.put(ColumnType.STRING, "String");
		columnToDisplayName.put(ColumnType.INTEGER, "Integer");
		columnToDisplayName.put(ColumnType.DOUBLE, "Double");
		columnToDisplayName.put(ColumnType.BOOLEAN, "Boolean");
		columnToDisplayName.put(ColumnType.FILEHANDLEID, "File");
		columnToDisplayName.put(ColumnType.DATE, "Date");
		
		columnToDisplayWidth = new HashMap<ColumnType, Integer>();
		columnToDisplayWidth.put(ColumnType.STRING, 150);
		columnToDisplayWidth.put(ColumnType.INTEGER, 100);
		columnToDisplayWidth.put(ColumnType.DOUBLE, 100);
		columnToDisplayWidth.put(ColumnType.BOOLEAN, 100);
		columnToDisplayWidth.put(ColumnType.FILEHANDLEID, 150);
		columnToDisplayWidth.put(ColumnType.DATE, 140);
	}
	
	public static String getColumnDisplayName(ColumnType type) {
		return columnToDisplayName.containsKey(type) ? columnToDisplayName.get(type) : "Unknown Type";
	}
	
	public static int getColumnDisplayWidth(ColumnType type) {
		return columnToDisplayWidth.containsKey(type) ? columnToDisplayWidth.get(type).intValue() : 150;
	}
	
	public static Column<TableModel, ?> getColumn(String tableEntityId,
			ColumnModel col, boolean canEdit, final RowUpdater rowUpdater,
			CellTable<TableModel> cellTable, SynapseView view,
			SynapseJSNIUtils synapseJSNIUtils) {
		// any restrained column, regardless of type
		if(canEdit && col.getEnumValues() != null && col.getEnumValues().size() > 0)
			return configComboString(col, canEdit, rowUpdater, cellTable, view); // Enum combo box
		
		// determine types
		if(col.getColumnType() == ColumnType.STRING) {
		    return configSimpleText(col, canEdit, rowUpdater, cellTable, view); // Simple text field    				
		} else if(col.getColumnType() == ColumnType.DOUBLE) {
			return configNumberField(col, true, canEdit, rowUpdater, cellTable, view);   
		} else if(col.getColumnType() == ColumnType.INTEGER) {
			return configNumberField(col, false, canEdit, rowUpdater, cellTable, view);   
		} else if(col.getColumnType() == ColumnType.BOOLEAN) {			
			if(canEdit) return configBooleanCombo(col, rowUpdater, cellTable, view); 
			else return configSimpleText(col, canEdit, rowUpdater, cellTable, view);			
		} else if(col.getColumnType() == ColumnType.FILEHANDLEID) {
			return configFileHandle(tableEntityId, col, canEdit, rowUpdater, cellTable, view, synapseJSNIUtils);  
		} else if(col.getColumnType() == ColumnType.DATE) {
			return configDateColumn(col, canEdit, rowUpdater, cellTable, view);
		} else {
			return null;
		} 
	}	
	
	public static boolean isAllFixedWidthColumns(List<ColumnModel> columns, int windowWidth) {
		if(windowWidth > 0) {
			int minRequiredWidth = 0;
			for(ColumnModel model : columns) minRequiredWidth += getColumnDisplayWidth(model.getColumnType());
			return minRequiredWidth > windowWidth;
		} 		
		return true; // if we don't know the window width, just use fixed widths
	}

	private interface SwitchHandler {
		void onSwitchChanged(boolean switchOn);
	}
	
	/**
	 * Create a default value input with on/off switch. Initializes to the given col, and modifiees the given col.
	 * @param col
	 * @return
	 */
	public static Widget createDefaultValueRadio(final ColumnModel col) {
		FlowPanel row = new FlowPanel();		
		
		HTML inputLabel = new HTML(DisplayConstants.DEFAULT_VALUE + " (" + DisplayConstants.OPTIONAL + "): ");
		inputLabel.addStyleName("margin-top-15 boldText");
		row.add(inputLabel);

		
		FlowPanel defaultValueRadio = new FlowPanel();
		defaultValueRadio.addStyleName("btn-group");
		 						
		final Button onBtn = DisplayUtils.createButton(DisplayConstants.ON_CAP);
		final Button offBtn = DisplayUtils.createButton(DisplayConstants.OFF);
		final Widget defaultValueBox;
		final SwitchHandler switchHandler;
		if(col.getColumnType() == ColumnType.BOOLEAN) {
			// make drop down box
			defaultValueBox = new ListBox();
			((ListBox)defaultValueBox).addItem(FALSE);
			((ListBox)defaultValueBox).addItem(TRUE);
			((ListBox)defaultValueBox).addChangeHandler(new ChangeHandler() {				
				@Override
				public void onChange(ChangeEvent event) {
					col.setDefaultValue(((ListBox)defaultValueBox).getValue(((ListBox)defaultValueBox).getSelectedIndex()));					
				}
			});
			switchHandler = new SwitchHandler() {				
				@Override
				public void onSwitchChanged(boolean switchOn) {
					if(switchOn) col.setDefaultValue(((ListBox)defaultValueBox).getValue(((ListBox)defaultValueBox).getSelectedIndex()));
					else col.setDefaultValue(null);
				}
			};
		} else if(col.getColumnType() == ColumnType.DATE) {
			// make date picker
			defaultValueBox = new DateBox();
		    ((DateBox)defaultValueBox).setFormat(new DateBox.DefaultFormat(DATE_FORMAT));
		    ((DateBox)defaultValueBox).addValueChangeHandler(new ValueChangeHandler<Date>() {				
				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					col.setDefaultValue(String.valueOf(((DateBox)defaultValueBox).getValue().getTime()));
				}
			});
			switchHandler = new SwitchHandler() {				
				@Override
				public void onSwitchChanged(boolean switchOn) {
					if(switchOn && ((DateBox)defaultValueBox).getValue() != null) col.setDefaultValue(String.valueOf(((DateBox)defaultValueBox).getValue().getTime()));
					else col.setDefaultValue(null);
				}
			};
		} else if(col.getColumnType() == ColumnType.FILEHANDLEID) {
			// just hide the default value for files
			defaultValueBox = new TextBox();
			switchHandler = null;
			row.setVisible(false);
		} else {
			// regular text box
			defaultValueBox = new TextBox();
			((TextBox)defaultValueBox).addChangeHandler(new ChangeHandler() {			
				@Override
				public void onChange(ChangeEvent event) {
					col.setDefaultValue(((TextBox)defaultValueBox).getValue());
				}
			});
			DisplayUtils.setPlaceholder(defaultValueBox, DisplayConstants.DEFAULT_VALUE);
			((TextBox)defaultValueBox).setValue(col.getDefaultValue());
			defaultValueBox.getElement().setAttribute("placeholder", "Default Value");
			switchHandler = new SwitchHandler() {				
				@Override
				public void onSwitchChanged(boolean switchOn) {
					if(switchOn) col.setDefaultValue(((TextBox)defaultValueBox).getValue());
					else col.setDefaultValue(null);
				}
			};

		}
		onBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				offBtn.removeStyleName("active");
				onBtn.addStyleName("active");
				defaultValueBox.setVisible(true);
				if(switchHandler != null) switchHandler.onSwitchChanged(true);
			}
		});
		offBtn.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				onBtn.removeStyleName("active");
				offBtn.addStyleName("active");
				defaultValueBox.setVisible(false);
				if(switchHandler != null) switchHandler.onSwitchChanged(false);
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
		defaultValueBox.addStyleName("form-control display-inline margin-top-5");
		defaultValueBox.setWidth("300px");		

		
		row.add(defaultValueRadio);
		row.add(defaultValueBox);
		return row;
	}
	
	public static void swapColumns(final List<ColumnDetailsPanel> columnPanelOrder, final FlowPanel allColumnsPanel, final ColumnDetailsPanel thisColumn,
			final int formerIdx, final int newIdx, final Presenter presenter) {
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

				presenter.updateColumnOrder(extractColumns(columnPanelOrder));
				
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

	public static void setArrowVisibility(int idx, int size, Anchor moveUp, Anchor moveDown) {
		if(idx == 0) moveUp.setVisible(false);
		else moveUp.setVisible(true);
		if(idx == size-1) moveDown.setVisible(false);
		else moveDown.setVisible(true);
	}

	public static List<String> extractColumns(List<ColumnDetailsPanel> columnPanelOrder) {
		List<String> columns = new ArrayList<String>();
		for(ColumnDetailsPanel colD : columnPanelOrder) {
			columns.add(colD.getCol().getId());
		}		
		return columns;
	}

	public static String getDateStringTableFormat(Date date) {
		return DATE_FORMAT.format(date);
	}
	
	/*
	 * ----- Column Type methods -----
	 */
	private static Column<TableModel, String> configComboString(final ColumnModel col, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		if(col.getEnumValues() == null) return null;
		List<String> options = (col.getDefaultValue() == null) ? withEmpty(col.getEnumValues()) : col.getEnumValues(); 
		final SelectionCell comboCell = new SelectionCell(options);
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				return object.getNeverNull(col.getId());
			}
		};
		column.setSortable(true);
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
				@Override
				public void update(int index, final TableModel object, String value) {
					final String original = object.getNeverNull(col.getId());
					if(value.equals("")) {
						object.put(col.getId(), value);
					} else {
						for (String enumVal : col.getEnumValues()) {
							if (enumVal.equals(value)) {
								object.put(col.getId(), enumVal);
							}
						}
					}
					rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
						@Override
						public void onSuccess(RowReferenceSet result) { 
							updateRowIdAndVersion(object, result, view);
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

	private static Column<TableModel, String> configSimpleText(final ColumnModel col, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		final AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
		Column<TableModel, String> column = new Column<TableModel, String>(cell) {
			@Override
			public String getValue(TableModel object) {
				return object.getNeverNull(col.getId());
			}
		};
		column.setSortable(true);
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
						@Override
						public void update(int index, final TableModel object,
								final String value) {
							final String original = object.getNeverNull(col.getId());
							object.put(col.getId(), value);
							rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
								@Override
								public void onSuccess(RowReferenceSet result) {
									updateRowIdAndVersion(object, result, view);
								}

								@Override
								public void onFailure(Throwable caught) {
									revertEditTextCell(col, cellTable, cell, object, original);
								}
							});
						}
					});
		}
		return column;
	}

	private static Column<TableModel, String> configNumberField(final ColumnModel col, final boolean isDouble, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		final AbstractCell<String> cell = canEdit ? new EditTextCell() : new TextCell();
		Column<TableModel, String> column = new Column<TableModel, String>(cell) {
			@Override
			public String getValue(TableModel object) {
				return object.getNeverNull(col.getId());
			}
		};
		column.setSortable(true);
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
						@Override
						public void update(int index, final TableModel object, String value) {						
							final String original = object.getNeverNull(col.getId()); 
							try {
								if(value != null && !"".equals(value)) {
									if(isDouble) {
										Double.parseDouble(value);
									} else {
										Long.parseLong(value);
									}
								}
								// save value after validation
								object.put(col.getId(), value);
								rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
									@Override
									public void onSuccess(RowReferenceSet result) { 
										updateRowIdAndVersion(object, result, view);
									}
									
									@Override
									public void onFailure(Throwable caught) {
										revertEditTextCell(col, cellTable, cell, object, original);
									}

								});
							} catch(NumberFormatException e) {
								view.showErrorMessage(DisplayConstants.NUMBER_NOT_VALID + ": "+ value);
								revertEditTextCell(col, cellTable, cell, object, original);
							}
						}						
					});
		}
		return column;
	}
	
	/**
	 * revert a cell back to its original value
	 * @param col
	 * @param cellTable
	 * @param cell
	 * @param object
	 * @param original
	 */
	private static void revertEditTextCell(final ColumnModel col,
			final CellTable<TableModel> cellTable,
			final AbstractCell<String> cell, final TableModel object,
			final String original) {
		object.put(col.getId(), original);
		((EditTextCell)cell).clearViewData(TableModel.KEY_PROVIDER.getKey(object));
		cellTable.redraw();
	}


	private static Column<TableModel, String> configBooleanCombo(final ColumnModel col, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {
		List<String> trueFalse = Arrays.asList(new String[] { TRUE, FALSE });
		List<String> options = (col.getDefaultValue() == null) ? withEmpty(trueFalse) : trueFalse;
		final SelectionCell comboCell = new SelectionCell(options);
		Column<TableModel, String> column = new Column<TableModel, String>(comboCell) {
			@Override
			public String getValue(TableModel object) {
				// convert 0/1 to display string false/true
				if(object.getNeverNull(col.getId()).equals("1") || object.getNeverNull(col.getId()).equalsIgnoreCase("true")) return TRUE;
				else if(object.getNeverNull(col.getId()).equals("0") || object.getNeverNull(col.getId()).equalsIgnoreCase("false")) return FALSE;
				else return null;
			}
		};
		column.setSortable(true);
		column.setFieldUpdater(new FieldUpdater<TableModel, String>() {
			@Override
			public void update(int index, final TableModel object, String value) {				
				final String original = object.getNeverNull(col.getId());
				if(value.equals("")) {
					object.put(col.getId(), value);
				} else {
					// convert true/false display string to DB value true/false, not 0/1 as you would expect. See PLFM-2703 
					if (TRUE.equals(value) || "1".equals(value)) object.put(col.getId(), "true");
					else if (FALSE.equals(value) || "0".equals(value)) object.put(col.getId(), "false");
					else object.put(col.getId(), null);
				}
				rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
					@Override
					public void onSuccess(RowReferenceSet result) {
						updateRowIdAndVersion(object, result, view);
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
	
	private static Column<TableModel, ?> configFileHandle(
			final String tableEntityId, final ColumnModel col, boolean canEdit,
			final RowUpdater rowUpdater, final CellTable<TableModel> cellTable,
			final SynapseView view, SynapseJSNIUtils synapseJSNIUtils) {
		final FileHandleCell cell = new FileHandleCell(canEdit, synapseJSNIUtils, ginInjector);
		Column<TableModel, TableCellFileHandle> column = new Column<TableModel, TableCellFileHandle>(cell) {
			@Override
			public TableCellFileHandle getValue(TableModel object) {				
				return new TableCellFileHandle(tableEntityId, col.getId(), object.getId(), object.getVersionNumber(), object.get(col.getId()));				
			}
		};
		column.setSortable(false);
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, TableCellFileHandle>() {
						@Override
						public void update(final int index, final TableModel object, TableCellFileHandle updatedFileHandle) {						
							if(updatedFileHandle.getFileHandleId() != null) {
								object.put(col.getId(), updatedFileHandle.getFileHandleId());
								rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
									@Override
									public void onSuccess(RowReferenceSet result) { 
										updateRowIdAndVersion(object, result, view);										
										cellTable.redrawRow(index); // to get updated URLs												
									}
									
									@Override
									public void onFailure(Throwable caught) {
										cellTable.redraw();											
									}
								});
							}
						}
					});
		}
		
		return column;		
	}	
	
	private static Column<TableModel, Date> configDateColumn(final ColumnModel col, boolean canEdit, final RowUpdater rowUpdater, final CellTable<TableModel> cellTable, final SynapseView view) {		
		final AbstractCell<Date> cell = canEdit ? new DatePickerCellNullTolerant(DATE_FORMAT) : new DateCell(DATE_FORMAT);
		Column<TableModel, Date> column = new Column<TableModel, Date>(cell) {
			@Override
			public Date getValue(TableModel object) {
				try {
					return new Date(Long.parseLong(object.getNeverNull(col.getId())));
				} catch (Exception e) {
					return null; 
				}
			}					
		};
		column.setSortable(true);
		if(canEdit) {
			column.setFieldUpdater(new FieldUpdater<TableModel, Date>() {
						@Override
						public void update(int index, final TableModel object, Date value) {					
							final String original = object.getNeverNull(col.getId()); 
							object.put(col.getId(), String.valueOf(value.getTime()));
							rowUpdater.updateRow(object, new AsyncCallback<RowReferenceSet>() {								
								@Override
								public void onSuccess(RowReferenceSet result) { 
									updateRowIdAndVersion(object, result, view);
								}
								
								@Override
								public void onFailure(Throwable caught) {
									object.put(col.getId(), original);
									((DatePickerCellNullTolerant)cell).clearViewData(TableModel.KEY_PROVIDER.getKey(object));
									cellTable.redraw();
								}
							});
						}
					});
		}
				
		return column;
	}	

	/**
	 * Update row's id and version. 
	 * If this is a new Row, rowId and version are added to the view after their first cell update. Future updates need to reference the actual rowId
	 * created in the first update otherwise duplicate rows will result.
	 * @param object
	 * @param result
	 * @param view
	 */
	private static void updateRowIdAndVersion(final TableModel object, RowReferenceSet result, SynapseView view) {
		// set rowId if this was a UI added row
			if (result != null
					&& result.getRows() != null
					&& result.getRows().size() > 0
					&& result.getRows().get(0) != null
					&& result.getRows().get(0).getRowId() != null) {
				object.setId(result.getRows().get(0).getRowId().toString());
				object.setVersionNumber(result.getRows().get(0).getVersionNumber().toString());
		}
	}
	

	private static List<String> withEmpty(List<String> values) {
		List<String> enums = new ArrayList<String>();
		enums.add("");
		enums.addAll(values);
		return enums;
	}

	public static Widget createStringLengthField(final ColumnModel col, final Widget container) {
		final IntegerBox stringLength = new IntegerBox();
		stringLength.setWidth("100px");
		stringLength.addStyleName("form-control");
		// show error when non-integer values added
		stringLength.addKeyUpHandler(new KeyUpHandler() {	
			@Override
			public void onKeyUp(KeyUpEvent event) {
				try {
					stringLength.getValueOrThrow();
					container.removeStyleName("has-error");
				} catch (ParseException e) {
					container.addStyleName("has-error");
				}
			}
		});
		
		// set length
		setStringMaxLength(col, stringLength);
		stringLength.addChangeHandler(new ChangeHandler() {			
			@Override
			public void onChange(ChangeEvent event) {
				try {					
					col.setMaximumSize(new Long(stringLength.getValueOrThrow()));
				} catch (ParseException e) {
					// clear user's invalid change
					setStringMaxLength(col, stringLength);
					container.removeStyleName("has-error");
				}

			}
		});
		return stringLength;
	}

	private static void setStringMaxLength(final ColumnModel col,
			final IntegerBox stringLength) {
		if(col.getMaximumSize() != null) stringLength.setValue(col.getMaximumSize().intValue());
		else stringLength.setValue(DEFAULT_STRING_MAX_LENGTH);
	}

	
}
