package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.TableModelUtils;

/**
 * Business logic tests for the TableEntityWidget
 * @author John
 *
 */
public class TableEntityWidgetTest {

	AdapterFactory adapterFactory;
	TableModelUtils tableModelUtils;
	List<ColumnModel> columns;
	TableBundle tableBundle;
	TableEntity tableEntity;
	TableEntityWidgetView mockView;
	PortalGinInjector mockGinInjector;
	QueryChangeHandler mockQueryChangeHandler;
	TableEntityWidget widget;
	EntityBundle entityBundle;
	SynapseClientAsync mockSynapseClient;
	
	@Before
	public void before(){
		// mocks
		mockView = Mockito.mock(TableEntityWidgetView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockQueryChangeHandler = Mockito.mock(QueryChangeHandler.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		// stubs
		adapterFactory = new AdapterFactoryImpl();
		tableModelUtils = new TableModelUtils(adapterFactory);
		columns = TableModelTestUtils.createOneOfEachType();
		tableEntity = new TableEntity();
		tableEntity.setId("syn123");
		tableEntity.setColumnIds(TableModelTestUtils.getColumnModelIds(columns));
		tableBundle = new TableBundle();
		tableBundle.setMaxRowsPerPage(4L);
		tableBundle.setColumnModels(columns);
		widget = new TableEntityWidget(mockView, mockGinInjector, mockSynapseClient, tableModelUtils);
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
	
	@Ignore
	@Test
	public void testConfigureNullDefaultQuery(){
		tableBundle.setMaxRowsPerPage(4L);
		widget.configure(entityBundle, true, mockQueryChangeHandler);
		String expected = "SELECT * FROM "+tableEntity.getId()+" LIMIT 3 OFFSET 0";
		// since a null query string was passed to the configure, the widget needs to set it
		// and notify the change listener.
		verify(mockQueryChangeHandler).onQueryChange(expected);
	}
	
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
	
	@Test
	public void testNoColumnsWithEdit(){
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		when(mockQueryChangeHandler.getQueryString()).thenReturn("SELECT * FROM syn123");
		boolean canEdit = true;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}
	
	@Test
	public void testNoColumnsWithWihtouEdit(){
		entityBundle.getTableBundle().setColumnModels(new LinkedList<ColumnModel>());
		when(mockQueryChangeHandler.getQueryString()).thenReturn("SELECT * FROM syn123");
		boolean canEdit = false;
		widget.configure(entityBundle, canEdit, mockQueryChangeHandler);
		verify(mockView).setQueryInputVisible(false);
		verify(mockView).setQueryResultsVisible(false);
		verify(mockView).setTableMessageVisible(true);
		verify(mockView).showTableMessage(AlertType.INFO, TableEntityWidget.NO_COLUMNS_NOT_EDITABLE);
		// The query should be cleared when there are no columns
		verify(mockQueryChangeHandler).onQueryChange(null);
	}
}
