package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.Arrays;
import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.PortalGinInjector;

import com.google.inject.Inject;

/**
 * Factory for creating table cells.
 * 
 * @author John
 *
 */
public class CellFactoryImpl implements CellFactory {
	
	PortalGinInjector ginInjector;
	
	@Inject
	public CellFactoryImpl(PortalGinInjector ginInjector){
		this.ginInjector = ginInjector;
	}

	@Override
	public Cell createRenderer(ColumnModel model) {
		switch(model.getColumnType()){
		case ENTITYID:
			return ginInjector.createEntityIdCellRenderer();
		default:
			return ginInjector.createStringRendererCell();
		}
	}

	@Override
	public CellEditor createEditor(ColumnModel model) {
		CellEditor editor;
		// enums get their own special editor
		if(model.getEnumValues() != null && !model.getEnumValues().isEmpty()){
			EnumCellEditor enumEditor = ginInjector.createEnumCellEditor();
			enumEditor.configure(model.getEnumValues());
			editor = enumEditor;
		}else{
			switch(model.getColumnType()){
			case BOOLEAN:
				// Boolean use an enum editor.
				editor = ginInjector.createBooleanCellEditor();
				break;
			case ENTITYID:
				editor = ginInjector.createEntityIdCellEditor();
				break;
			default:
				editor = ginInjector.createStringEditorCell();
			}
		}
		// Configure each editor with the default value.
		editor.setValue(model.getDefaultValue());
		return editor;

	}

}
