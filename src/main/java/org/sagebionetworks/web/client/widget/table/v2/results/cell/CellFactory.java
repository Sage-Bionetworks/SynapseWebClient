package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.PortalGinInjector;
import com.google.inject.Inject;

/**
 * Factory for creating table cells.
 * 
 * @author John
 *
 */
public class CellFactory {

	PortalGinInjector ginInjector;

	@Inject
	public CellFactory(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}

	public Cell createRenderer(ColumnModel model) {
		return createRenderer(model.getColumnType());
	}

	public Cell createRenderer(ColumnType type) {
		switch (type) {
			case ENTITYID:
				return ginInjector.createEntityIdCellRenderer();
			case DATE:
				return ginInjector.createDateCellRenderer();
			case LINK:
				return ginInjector.createLinkCellRenderer();
			case FILEHANDLEID:
				return ginInjector.createFileCellRenderer();
			case USERID:
				return ginInjector.createUserIdCellRenderer();
			default:
				return ginInjector.createStringRendererCellView();
		}
	}

	public CellEditor createEditor(ColumnModel model) {
		CellEditor editor;
		// enums get their own special editor
		if (model.getEnumValues() != null && !model.getEnumValues().isEmpty()) {
			EnumCellEditor enumEditor = ginInjector.createEnumCellEditor();
			enumEditor.configure(model.getEnumValues());
			editor = enumEditor;
		} else {
			switch (model.getColumnType()) {
				case DATE:
					editor = ginInjector.createDateCellEditor();
					break;
				case BOOLEAN:
					editor = ginInjector.createBooleanCellEditor();
					break;
				case ENTITYID:
					editor = ginInjector.createEntityIdCellEditor();
					break;
				case DOUBLE:
					editor = ginInjector.createDoubleCellEditor();
					break;
				case INTEGER:
					editor = ginInjector.createIntegerCellEditor();
					break;
				case FILEHANDLEID:
					editor = ginInjector.createFileCellEditor();
					break;
				case USERID:
					editor = ginInjector.createUserIdCellEditor();
					break;
				default:
					StringEditorCell stringEditor = ginInjector.createStringEditorCell();
					stringEditor.setMaxSize(model.getMaximumSize());
					editor = stringEditor;
			}
		}
		// Configure each editor with the default value.
		editor.setValue(model.getDefaultValue());
		return editor;

	}

	public CellEditor createFormEditor(ColumnModel model) {
		CellEditor editor;
		// enums get their own special editor
		if (model.getEnumValues() != null && !model.getEnumValues().isEmpty()) {
			EnumFormCellEditor enumEditor = ginInjector.createEnumFormCellEditor();
			enumEditor.configure(model.getEnumValues());
			editor = enumEditor;
		} else {
			switch (model.getColumnType()) {
				case DATE:
					editor = ginInjector.createDateCellEditor();
					break;
				case BOOLEAN:
					editor = ginInjector.createBooleanFormCellEditor();
					break;
				case ENTITYID:
					editor = ginInjector.createEntityIdCellEditor();
					break;
				case DOUBLE:
					editor = ginInjector.createDoubleCellEditor();
					break;
				case INTEGER:
					editor = ginInjector.createIntegerCellEditor();
					break;
				case FILEHANDLEID:
					editor = ginInjector.createFileCellEditor();
					break;
				case USERID:
					editor = ginInjector.createUserIdCellEditor();
					break;
				case LARGETEXT:
					editor = ginInjector.createLargeTextFormCellEditor();
					break;
				default:
					StringEditorCell stringEditor = ginInjector.createStringEditorCell();
					stringEditor.setMaxSize(model.getMaximumSize());
					editor = stringEditor;
			}
		}
		// Configure each editor with the default value.
		editor.setValue(model.getDefaultValue());
		return editor;

	}

}
