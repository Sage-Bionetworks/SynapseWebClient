package org.sagebionetworks.web.client.widget.entity.row;

import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.dialog.Annotation;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;

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
	public CellEditor createEditor(Annotation annotation) {
		CellEditor editor;
		switch(annotation.getType()) {
			case LONG:
				editor = ginInjector.createIntegerCellEditor();
				break;
			case DOUBLE:
				editor = ginInjector.createDoubleCellEditor();
				break;
			case DATE:
				editor = ginInjector.createDateCellEditor();
				break;
			case STRING:
			default:
				editor = ginInjector.createStringEditorCell();
				break;
			
		}
		return editor;
	}
}

