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
		return ginInjector.createStringRendererCell();
	}

	@Override
	public Cell createEditor(ColumnModel model) {
		return ginInjector.createStringEditorCell();
	}

}
