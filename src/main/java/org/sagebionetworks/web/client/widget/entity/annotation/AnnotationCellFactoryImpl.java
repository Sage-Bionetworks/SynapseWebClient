package org.sagebionetworks.web.client.widget.entity.annotation;

import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellEditor;
import com.google.inject.Inject;

/**
 * Factory for creating annotation cells.
 */
public class AnnotationCellFactoryImpl implements AnnotationCellFactory {

	PortalGinInjector ginInjector;

	@Inject
	public AnnotationCellFactoryImpl(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}

	@Override
	public CellEditor createEditor(AnnotationsValue annotation) {
		CellEditor editor;
		switch (annotation.getType()) {
			case LONG:
				editor = ginInjector.createIntegerCellEditor();
				break;
			case DOUBLE:
				editor = ginInjector.createDoubleCellEditor();
				break;
			case TIMESTAMP_MS:
				editor = ginInjector.createDateCellEditor();
				break;
			case STRING:
				editor = ginInjector.createStringEditorCell();
				break;
			default:
				throw new IllegalArgumentException("Unrecognized annotation type " + annotation.getType());
		}
		return editor;
	}
}

