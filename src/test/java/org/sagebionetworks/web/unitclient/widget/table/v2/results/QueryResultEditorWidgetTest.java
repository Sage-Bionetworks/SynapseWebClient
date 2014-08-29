package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;

/**
 * Unit tests for QueryResultEditorWidget.  Currently this widget has very little business logic.
 * 
 * @author John
 *
 */
public class QueryResultEditorWidgetTest {

	QueryResultEditorView mockView;
	TablePageWidget mockPageWidget;
	QueryResultEditorWidget widget;
	
	@Before
	public void before(){
		mockView = Mockito.mock(QueryResultEditorView.class);
		mockPageWidget = Mockito.mock(TablePageWidget.class);
		widget = new QueryResultEditorWidget(mockView, mockPageWidget);
	}
	
	@Test
	public void testOnSelectionChangedNone(){
		when(mockPageWidget.isOneRowOrMoreRowsSelected()).thenReturn(false);
		widget.onSelectionChanged();
		verify(mockView).setDeleteButtonEnabled(false);
	}
	
	@Test
	public void testOnSelectionChangedOne(){
		when(mockPageWidget.isOneRowOrMoreRowsSelected()).thenReturn(true);
		widget.onSelectionChanged();
		verify(mockView).setDeleteButtonEnabled(true);
	}
	
	@Test
	public void testOnDeleteSelected(){
		widget.onDeleteSelected();
		verify(mockPageWidget).onDeleteSelected();
	}
	
	@Test
	public void testOnSelectAll(){
		widget.onSelectAll();
		verify(mockPageWidget).onSelectAll();
	}
	
	@Test
	public void testOnSelectNone(){
		widget.onSelectNone();
		verify(mockPageWidget).onSelectNone();
	}
	
	@Test
	public void testOnToggleSelect(){
		widget.onToggleSelect();
		verify(mockPageWidget).onToggleSelect();
	}
	
}
