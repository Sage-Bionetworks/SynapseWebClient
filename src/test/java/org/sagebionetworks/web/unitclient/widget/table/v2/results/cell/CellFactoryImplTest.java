package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCell;

public class CellFactoryImplTest {

	StringEditorCell mockStringEditorCell;
	StringRendererCell mockStringRendererCell;
	PortalGinInjector mockInjector;
	CellFactoryImpl cellFactory;

	
	@Before
	public void before() {
		mockInjector = Mockito.mock(PortalGinInjector.class);
		mockStringEditorCell = Mockito.mock(StringEditorCell.class);
		mockStringRendererCell = Mockito.mock(StringRendererCell.class);
		when(mockInjector.createStringEditorCell()).thenReturn(mockStringEditorCell);
		when(mockInjector.createStringRendererCell()).thenReturn(mockStringRendererCell);
		cellFactory = new CellFactoryImpl(mockInjector);
	}

	/**
	 * Must be able to get a cell editor for each column type.
	 */
	@Test
	public void testEditorAllTypes() {
		for(ColumnType type: ColumnType.values()){
			ColumnModel cm = new ColumnModel();
			cm.setColumnType(type);
			Cell cell = cellFactory.createEditor(cm);
			assertNotNull("Could not create a cell editor for type: "+type, cell);
		}
	}
	
	/**
	 * Must be able to get a cell renderer for each column type.
	 */
	@Test
	public void testRendererAllTypes() {
		for(ColumnType type: ColumnType.values()){
			ColumnModel cm = new ColumnModel();
			cm.setColumnType(type);
			Cell cell = cellFactory.createRenderer(cm);
			assertNotNull("Could not create a cell renderer for type: "+type, cell);
		}
	}

}
