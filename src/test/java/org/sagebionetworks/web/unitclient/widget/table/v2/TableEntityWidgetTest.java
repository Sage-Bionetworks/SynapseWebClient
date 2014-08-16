package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;

/**
 * Business logic tests for the TableEntityWidget
 * @author John
 *
 */
public class TableEntityWidgetTest {

	List<ColumnModel> columns;
	TableBundle tableBundle;
	TableEntity tableEntity;
	TableEntityWidgetView mockView;
	PortalGinInjector mockGinInjector;
	QueryChangeHandler mockQueryChangeHandler;
	TableEntityWidget widget;
	EntityBundle entityBundle;
	
	@Before
	public void before(){
		// mocks
		mockView = Mockito.mock(TableEntityWidgetView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockQueryChangeHandler = Mockito.mock(QueryChangeHandler.class);
		// stubs
		columns = TableModelTestUtils.createOneOfEachType();
		tableEntity = new TableEntity();
		tableEntity.setId("syn123");
		tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		tableBundle = new TableBundle();
		tableBundle.setMaxRowsPerPage(4L);
		tableBundle.setColumnModels(columns);
		widget = new TableEntityWidget(mockView, mockGinInjector);
		// The test bundle
		entityBundle = new EntityBundle(tableEntity, null, null, null, null, null, null, tableBundle);
	}
	
	@Test
	public void testGetDefaultPageSizeMaxUnder(){
		tableBundle.setMaxRowsPerPage(4L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// since the size from the bundle is less than the default,
		// the value used should be 3/4ths of the max allowed for the schema.
		assertEquals(3l, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeMaxOver(){
		tableBundle.setMaxRowsPerPage(TableEntityWidget.DEFAULT_PAGE_SIZE *2L);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// since the size from the bundle is greater than the default
		// the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_PAGE_SIZE, widget.getDefaultPageSize());
	}
	
	@Test
	public void testGetDefaultPageSizeNull(){
		tableBundle.setMaxRowsPerPage(null);
		// Configure with the default values
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// when null the default should be used.
		assertEquals(TableEntityWidget.DEFAULT_PAGE_SIZE, widget.getDefaultPageSize());
	}
	
	@Test 
	public void testDefaultQueryString(){
		tableBundle.setMaxRowsPerPage(4L);
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		String expected = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		assertEquals(expected, widget.getDefaultQueryString());
	}
	
//	@Test
//	public void testConfigureNullDefaultQuery(){
//		tableBundle.setMaxRowsPerPage(4L);
//		widget.configure(entityBundle, true, mockQueryChangeHandler);
//		String expected = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
//		// since a null query string was passed to the configure, the widget needs to set it
//		// and notify the change listener.
//		verify(mockQueryChangeHandler).onQueryChange(expected);
//	}
	
	@Test
	public void testConfigureNotNullDefaultQuery(){
		tableBundle.setMaxRowsPerPage(4L);
		// This time we pass a query
		String sql = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		when(mockQueryChangeHandler.getQueryString()).thenReturn(sql);
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		// The widget must not change the query when it is passed in.
		verify(mockQueryChangeHandler, never()).onQueryChange(anyString());
	}
}
