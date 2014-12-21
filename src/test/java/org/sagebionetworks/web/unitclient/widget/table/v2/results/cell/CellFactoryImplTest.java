package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.BooleanCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactoryImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DateCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.DoubleCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.IntegerCellEditor;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.LinkCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringEditorCell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.StringRendererCell;

public class CellFactoryImplTest {

	StringEditorCell mockStringEditorCell;
	StringRendererCell mockStringRendererCell;
	EntityIdCellEditor mockEntityIdCellEditor;
	EntityIdCellRenderer mockEntityIdCellRenderer;
	EnumCellEditor mockEnumEditor;
	BooleanCellEditor mockBooleanCellEditor;
	DateCellEditor mockDateCellEditor;
	DateCellRenderer mockDateCellRenderer;
	DoubleCellEditor mockDoubleCellEditor;
	IntegerCellEditor mockIntegerCellEditor;
	LinkCellRenderer mockLinkCellRenderer;
	FileCellEditor mockFileCellEditor;
	FileCellRenderer mockFileCellRenderer;
	PortalGinInjector mockInjector;
	CellFactoryImpl cellFactory;

	
	@Before
	public void before() {
		mockInjector = Mockito.mock(PortalGinInjector.class);
		mockStringEditorCell = Mockito.mock(StringEditorCell.class);
		mockStringRendererCell = Mockito.mock(StringRendererCell.class);
		mockEntityIdCellEditor = Mockito.mock(EntityIdCellEditor.class);
		mockEntityIdCellRenderer = Mockito.mock(EntityIdCellRenderer.class);
		mockEnumEditor = Mockito.mock(EnumCellEditor.class);
		mockBooleanCellEditor = Mockito.mock(BooleanCellEditor.class);
		mockDateCellEditor = Mockito.mock(DateCellEditor.class);
		mockDateCellRenderer = Mockito.mock(DateCellRenderer.class);
		mockDoubleCellEditor = Mockito.mock(DoubleCellEditor.class);
		mockIntegerCellEditor = Mockito.mock(IntegerCellEditor.class);
		mockLinkCellRenderer = Mockito.mock(LinkCellRenderer.class);
		mockFileCellEditor= Mockito.mock(FileCellEditor.class);
		mockFileCellRenderer = Mockito.mock(FileCellRenderer.class);

		when(mockInjector.createStringEditorCell()).thenReturn(mockStringEditorCell);
		when(mockInjector.createStringRendererCell()).thenReturn(mockStringRendererCell);
		when(mockInjector.createEntityIdCellEditor()).thenReturn(mockEntityIdCellEditor);
		when(mockInjector.createEntityIdCellRenderer()).thenReturn(mockEntityIdCellRenderer);
		when(mockInjector.createEnumCellEditor()).thenReturn(mockEnumEditor);
		when(mockInjector.createBooleanCellEditor()).thenReturn(mockBooleanCellEditor);
		when(mockInjector.createDateCellEditor()).thenReturn(mockDateCellEditor);
		when(mockInjector.createDateCellRenderer()).thenReturn(mockDateCellRenderer);
		when(mockInjector.createDoubleCellEditor()).thenReturn(mockDoubleCellEditor);
		when(mockInjector.createIntegerCellEditor()).thenReturn(mockIntegerCellEditor);
		when(mockInjector.createLinkCellRenderer()).thenReturn(mockLinkCellRenderer);
		when(mockInjector.createFileCellEditor()).thenReturn(mockFileCellEditor);
		when(mockInjector.createFileCellRenderer()).thenReturn(mockFileCellRenderer);

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
	
	@Test
	public void testGetEntityIdRenderer(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.ENTITYID);
		assertEquals(mockEntityIdCellRenderer, cellFactory.createRenderer(cm));
	}
	
	@Test
	public void testGetLinkCellRenderer(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.LINK);
		assertEquals(mockLinkCellRenderer, cellFactory.createRenderer(cm));
	}
	
	@Test
	public void testGetEntityIdEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.ENTITYID);
		assertEquals(mockEntityIdCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetEnumEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setEnumValues(Arrays.asList("a","b","c"));
		assertEquals(mockEnumEditor, cellFactory.createEditor(cm));
		// should be configured with the enum values
		verify(mockEnumEditor).configure(cm.getEnumValues());
	}

	@Test
	public void testGetBooleanEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.BOOLEAN);
		assertEquals(mockBooleanCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetEditorDefaultValueNull(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setDefaultValue(null);
		assertEquals(mockStringEditorCell, cellFactory.createEditor(cm));
		// The null default value should be passed to the editor.
		verify(mockStringEditorCell).setValue(null);
	}
	
	@Test
	public void testGetEditorDefaultValueNotNull(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.STRING);
		cm.setDefaultValue("a default value");
		assertEquals(mockStringEditorCell, cellFactory.createEditor(cm));
		// The null default value should be passed to the editor.
		verify(mockStringEditorCell).setValue(cm.getDefaultValue());
	}
	
	@Test
	public void testGetDateCellEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.DATE);
		assertEquals(mockDateCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetDateCellRenderer(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.DATE);
		assertEquals(mockDateCellRenderer, cellFactory.createRenderer(cm));
	}
	
	@Test
	public void testGetDoubleEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.DOUBLE);
		assertEquals(mockDoubleCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetIntegerEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.INTEGER);
		assertEquals(mockIntegerCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetFileCellEditor(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.FILEHANDLEID);
		assertEquals(mockFileCellEditor, cellFactory.createEditor(cm));
	}
	
	@Test
	public void testGetFileCellRenderer(){
		ColumnModel cm = new ColumnModel();
		cm.setColumnType(ColumnType.FILEHANDLEID);
		assertEquals(mockFileCellRenderer, cellFactory.createRenderer(cm));
	}
}
