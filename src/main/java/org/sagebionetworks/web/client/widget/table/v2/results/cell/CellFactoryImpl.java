package org.sagebionetworks.web.client.widget.table.v2.results.cell;

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
			return ginInjector.createEntityCellRenderer();
		default:
			return ginInjector.createStringRendererCell();
		}
	}

	@Override
	public CellEditor createEditor(ColumnModel model) {
		switch(model.getColumnType()){
		case ENTITYID:
			return ginInjector.createEntityCellEditor();
		default:
			return ginInjector.createStringEditorCell();
		}
	}

}
