package org.sagebionetworks.web.client.widget.table;

import java.util.HashMap;
import java.util.Map;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.i18n.client.NumberFormat;

public class ColumnUtils {
	static final String trueStr = "True";
	static final String falseStr = "False";	
	
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
	
	public static ColumnConfig getColumnConfig(ColumnModel col) {
		ColumnConfig colConfig = new ColumnConfig();
		colConfig.setId(col.getName());		
		colConfig.setHeader(col.getName());
		colConfig.setWidth(100);
		if(col.getColumnType() == ColumnType.STRING) {
			if(col.getEnumValues() == null || col.getEnumValues().size() == 0) {
				// Simple text field
			    configSimpleText(colConfig);    				
			} else {
				// Enum combo box				  
			    configComboString(col, colConfig);  
			}
		} else if(col.getColumnType() == ColumnType.DOUBLE) {
		    colConfig.setEditor(new CellEditor(new NumberField()));  
		} else if(col.getColumnType() == ColumnType.LONG) {
			colConfig.setNumberFormat(NumberFormat.getDecimalFormat());  
			colConfig.setEditor(new CellEditor(new NumberField()));
		} else if(col.getColumnType() == ColumnType.BOOLEAN) {
			// Enum combo box			
			configBooleanCombo(colConfig, col);
		} else if(col.getColumnType() == ColumnType.FILEHANDLEID) {
			configFileHandle(colConfig);  
//		} else if(col.getColumnType() == ColumnType.DATE) {
//		    DateField dateField = new DateField();  
//		    dateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("MM/dd/y"));  		  
//		    colConfig.setEditor(new CellEditor(dateField));  
//		    colConfig.setDateTimeFormat(DateTimeFormat.getFormat("MMM dd yyyy"));  
		} else {
			// unknown column type
		} 

		return colConfig;
	}

	private static void configFileHandle(ColumnConfig colConfig) {
		GridCellRenderer<BaseModelData> buttonRenderer = new GridCellRenderer<BaseModelData>() {  
			  
		      private boolean init;  
		  
		      public Object render(final BaseModelData model, String property, ColumnData config, final int rowIndex,  
		          final int colIndex, ListStore<BaseModelData> store, Grid<BaseModelData> grid) {  
		        if (!init) {  
		          init = true;  
		          grid.addListener(Events.ColumnResize, new Listener<GridEvent<BaseModelData>>() {  
		  
		            public void handleEvent(GridEvent<BaseModelData> be) {  
		              for (int i = 0; i < be.getGrid().getStore().getCount(); i++) {  
		                if (be.getGrid().getView().getWidget(i, be.getColIndex()) != null  
		                    && be.getGrid().getView().getWidget(i, be.getColIndex()) instanceof BoxComponent) {  
		                  ((BoxComponent) be.getGrid().getView().getWidget(i, be.getColIndex())).setWidth(be.getWidth() - 10);  
		                }  
		              }  
		            }  
		          });  
		        }  
		  
		        Button b = new Button((String) model.get(property), new SelectionListener<ButtonEvent>() {  
		          @Override  
		          public void componentSelected(ButtonEvent ce) {  
		            // button clicked			        	  
		          }  
		        });  
		        b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);  
		        b.setToolTip("Click for more information");  
		  
		        return b;  
		      }  
		    };  
		colConfig.setRenderer(buttonRenderer);
	}

	private static void configBooleanCombo(ColumnConfig colConfig, ColumnModel col) {
		final SimpleComboBox<String> combo = new SimpleComboBox<String>();  
	    combo.setForceSelection(true);  
	    combo.setTriggerAction(TriggerAction.ALL);
	    combo.setEditable(false);
	    combo.setForceSelection(true);
	    combo.add(Boolean.TRUE.toString().toLowerCase());
	    combo.add(Boolean.FALSE.toString().toLowerCase());
	    
	    if(col.getDefaultValue() != null) {
	    	if(trueStr.equalsIgnoreCase(col.getDefaultValue())) combo.setSimpleValue(trueStr);
	    	else combo.setSimpleValue(falseStr);
	    }
	    
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
	    colConfig.setEditor(editor);  					
	}

	private static void configComboString(ColumnModel col,
			ColumnConfig colConfig) {
		final SimpleComboBox<String> combo = new SimpleComboBox<String>();  
		combo.setForceSelection(true);  
		combo.setTriggerAction(TriggerAction.ALL);  
		for(String value : col.getEnumValues()) {			    	
			combo.add(value);  
		}			    
		if(col.getDefaultValue() != null) combo.setSimpleValue(col.getDefaultValue());			    

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
		colConfig.setEditor(editor);
	}

	private static void configSimpleText(ColumnConfig colConfig) {
		TextField<String> text = new TextField<String>();  
		text.setAllowBlank(false);  
		colConfig.setEditor(new CellEditor(text));
	}
	
}
