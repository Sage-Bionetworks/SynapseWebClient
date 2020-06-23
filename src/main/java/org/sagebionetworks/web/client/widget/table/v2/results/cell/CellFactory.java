package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import java.util.function.Consumer;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.constants.IconType;
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
			case STRING_LIST:
			case BOOLEAN_LIST:
			case INTEGER_LIST:
//			case ENTITYID_LIST:
//			case USERID_LIST:
				return ginInjector.createStringListRendererCellView();
			case DATE_LIST:
				return ginInjector.createDateListRendererCellView();				
			//TODO: add special renderers for entity id list and user id list
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
				case STRING_LIST:
				case INTEGER_LIST:
				case BOOLEAN_LIST:
				case DATE_LIST:
//				case ENTITYID_LIST:
//				case USERID_LIST:
					JSONListCellEditor listEditor = ginInjector.createListCellEditor();
					listEditor.setMaxSize(model.getMaximumSize());
					listEditor.setMaxListLength(model.getMaximumListLength());
					editor = listEditor;
					break;
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
				case STRING_LIST:
				case INTEGER_LIST:
				case BOOLEAN_LIST:
				case DATE_LIST:
//				case ENTITYID_LIST:
//				case USERID_LIST:
					JSONListCellEditor listEditor = ginInjector.createListCellEditor();
					listEditor.setMaxSize(model.getMaximumSize());
					listEditor.setMaxListLength(model.getMaximumListLength());
					editor = listEditor;
					break;
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

	// TODO: test?
	public static InputGroup appendDeleteButton(final CellEditor editor, Consumer<CellEditor> additionalDeleteCallback){
		final InputGroup group = new InputGroup();
		Button deleteButton = new Button("", IconType.TIMES, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				group.removeFromParent();
				additionalDeleteCallback.accept(editor);
			}
		});

		deleteButton.setHeight("35px");
		InputGroupButton deleteButtonGroup = new InputGroupButton();
		deleteButtonGroup.add(deleteButton);
		group.add(editor.asWidget());
		group.add(deleteButtonGroup);

		return group;
	}

}
