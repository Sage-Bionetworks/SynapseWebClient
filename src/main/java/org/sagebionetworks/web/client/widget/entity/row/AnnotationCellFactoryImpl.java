package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;

import com.google.inject.Inject;

/**
 * Factory for creating annotation cells.
 */
public class AnnotationCellFactoryImpl implements AnnotationCellFactory {
	
	PortalGinInjector ginInjector;
	
	@Inject
	public AnnotationCellFactoryImpl(PortalGinInjector ginInjector){
		this.ginInjector = ginInjector;
	}

	@Override
	public CellEditor createEditor(ANNOTATION_TYPE type) {
		CellEditor editor;
	
		switch(type){
			case DATE:
				editor = ginInjector.createDateCellEditor();
				break;
			case DOUBLE:
				editor = ginInjector.createDoubleCellEditor();
				break;
			case LONG:
				editor = ginInjector.createIntegerCellEditor();
				break;
			default:
				StringEditorCell stringEditor = ginInjector.createStringEditorCell();
				editor = stringEditor;
		}
		return editor;
	}
}
