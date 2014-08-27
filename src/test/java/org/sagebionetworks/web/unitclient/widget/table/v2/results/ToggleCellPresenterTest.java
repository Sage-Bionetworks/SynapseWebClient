package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.ToggleCellPresenter;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Unit test for ToggleCellPresenter.
 * 
 * @author John
 *
 */
public class ToggleCellPresenterTest {
	Cell mockEditor;
	Cell mockRenderer;
	CellFactory mockFactory;
	HasWidgets mockContainer;

	@Before
	public void before(){
		mockFactory = Mockito.mock(CellFactory.class);
		mockEditor = new CellStub();
		mockRenderer = new CellStub();
		mockRenderer.setVisible(true);
		when(mockFactory.createEditor(any(ColumnTypeViewEnum.class))).thenReturn(mockEditor);
		when(mockFactory.createRenderer(any(ColumnTypeViewEnum.class))).thenReturn(mockRenderer);
		mockContainer = Mockito.mock(HasWidgets.class);
	}
	
	@Test
	public void testConstructor(){
		ToggleCellPresenter toggle = new ToggleCellPresenter(ColumnTypeViewEnum.String, mockFactory, mockContainer);
		// The view should exist
		verify(mockFactory).createRenderer(any(ColumnTypeViewEnum.class));
		// the editor should not be created yet
		verify(mockFactory, never()).createEditor(any(ColumnTypeViewEnum.class));
		// set the value
		String startValue = "start value";
		toggle.setValue(startValue);
		// The value should be passed to the renderer
		assertEquals(startValue, mockRenderer.getValue());
		// toggle
		toggle.toggleEdit(false);
		assertTrue(mockRenderer.isVisible());
	}
	
	@Test
	public void testToggleEditFalse(){
		ToggleCellPresenter toggle = new ToggleCellPresenter(ColumnTypeViewEnum.String, mockFactory, mockContainer);
		// this should not cause a change.
		toggle.toggleEdit(false);
		assertTrue(mockRenderer.isVisible());
		// the editor should not be created yet
		verify(mockFactory, never()).createEditor(any(ColumnTypeViewEnum.class));
	}
	
	@Test
	public void testToggleEditTrue(){
		ToggleCellPresenter toggle = new ToggleCellPresenter(ColumnTypeViewEnum.String, mockFactory, mockContainer);
		// set the value
		String startValue = "start value";
		toggle.setValue(startValue);
		// The value should be passed to the renderer
		assertEquals(startValue, mockRenderer.getValue());
		// go into edit mode
		toggle.toggleEdit(true);
		assertFalse(mockRenderer.isVisible());
		// The editor should be called
		verify(mockFactory).createEditor(any(ColumnTypeViewEnum.class));
		// the editor should now have the value
		assertEquals(startValue, mockEditor.getValue());
		assertTrue(mockEditor.isVisible());
	}
	
	@Test
	public void testToggleMultiple(){
		ToggleCellPresenter toggle = new ToggleCellPresenter(ColumnTypeViewEnum.String, mockFactory, mockContainer);
		// set the value
		String startValue = "start value";
		toggle.setValue(startValue);
		// The value should be passed to the renderer
		assertEquals(startValue, mockRenderer.getValue());
		// go into edit mode
		toggle.toggleEdit(true);
		assertFalse(mockRenderer.isVisible());
		// the editor should now have the value
		assertEquals(startValue, mockEditor.getValue());
		assertTrue(mockEditor.isVisible());
		// Change the value of the editor
		String secondValue = "a new value";
		mockEditor.setValue(secondValue);
		// Now toggle it back, the view should get the new value
		toggle.toggleEdit(false);
		assertEquals(secondValue, mockRenderer.getValue());
		assertFalse(mockEditor.isVisible());
		assertTrue(mockRenderer.isVisible());
		// Third value
		String third = "3rd";
		toggle.setValue(third);
		toggle.toggleEdit(true);
		assertEquals(third, mockEditor.getValue());
		assertTrue(mockEditor.isVisible());
		assertFalse(mockRenderer.isVisible());
	}
}
