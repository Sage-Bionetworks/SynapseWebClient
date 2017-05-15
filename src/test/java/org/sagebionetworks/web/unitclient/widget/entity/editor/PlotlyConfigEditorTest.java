package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigEditor.*;
import static org.sagebionetworks.web.shared.WidgetConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.plotly.BarMode;
import org.sagebionetworks.web.client.plotly.GraphType;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.PlotlyConfigView;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class PlotlyConfigEditorTest {
		
	PlotlyConfigEditor editor;
	@Mock
	PlotlyConfigView mockView;
	
	@Mock
	EntityFinder mockFinder;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	Button mockShowHideAdvancedButton;
	@Captor
	ArgumentCaptor<ClickHandler> clickHandlerCaptor;
	List<ColumnModel> columnModels;
	@Mock
	ColumnModel mockXColumnModel;
	@Mock
	ColumnModel mockYColumnModel;
	@Mock
	ColumnModel mockY2ColumnModel;
	@Captor
	ArgumentCaptor<List<String>> availableColumnNamesCaptor;
	@Captor
	ArgumentCaptor<DisplayUtils.SelectedHandler<Reference>> finderCallbackCaptor;
	public static String X_COLUMN_NAME = "x Column";
	public static String Y_COLUMN_NAME = "y column";
	public static String Y2_COLUMN_NAME = "y2 column";
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		editor = new PlotlyConfigEditor(mockView, mockFinder, mockSynAlert, mockSynapseClient, mockShowHideAdvancedButton);
		columnModels = new ArrayList<ColumnModel>();
		AsyncMockStubber.callSuccessWith(columnModels).when(mockSynapseClient).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		when(mockXColumnModel.getName()).thenReturn(X_COLUMN_NAME);
		when(mockYColumnModel.getName()).thenReturn(Y_COLUMN_NAME);
		when(mockY2ColumnModel.getName()).thenReturn(Y2_COLUMN_NAME);
	}
	
	@Test
	public void testConstruction() {
		verify(mockView).add(mockFinder);
		verify(mockView).setSynAlert(mockSynAlert);
		verify(mockView).setPresenter(editor);
		verify(mockView).setShowHideButton(mockShowHideAdvancedButton);
		
		verify(mockView).setAdvancedUIVisible(false);
		verify(mockView, never()).setAdvancedUIVisible(true);
		verify(mockShowHideAdvancedButton).setText(SHOW);
		verify(mockShowHideAdvancedButton).setIcon(IconType.TOGGLE_RIGHT);
		
		//test showHideAdvancedButton, simulate click
		verify(mockShowHideAdvancedButton).addClickHandler(clickHandlerCaptor.capture());
		clickHandlerCaptor.getValue().onClick(null);
		verify(mockView).setAdvancedUIVisible(true);
		verify(mockShowHideAdvancedButton).setText(HIDE);
		verify(mockShowHideAdvancedButton).setIcon(IconType.TOGGLE_DOWN);
	}
	
	@Test
	public void testConfigure() {
		// verifies that params are reflected in view.  also tests basic sql parsing
		String tableSynId = "syn987";
		String advancedClause = "where \"x Column\" > 2 group by \'y column\'";
		String sql = "select \"" + X_COLUMN_NAME + "\", \'" + Y_COLUMN_NAME + "\' from " + tableSynId + " " + advancedClause;
		
		columnModels.add(mockXColumnModel);
		columnModels.add(mockYColumnModel);
		
		String plotTitle = "My Plot";
		GraphType type = GraphType.BAR;
		BarMode barMode = BarMode.STACK;
		String xAxisTitle = "X";
		String yAxisTitle = "Y";
		
		Map<String, String> params = new HashMap<>();
		params.put(TABLE_QUERY_KEY, sql);
		params.put(TITLE, plotTitle);
		params.put(X_AXIS_TITLE, xAxisTitle);
		params.put(Y_AXIS_TITLE, yAxisTitle);
		params.put(TYPE, type.name());
		params.put(BAR_MODE, barMode.name());
		
		WikiPageKey wikiKey = null;
		DialogCallback callback = null;
		editor.configure(wikiKey, params, callback);
		
		verify(mockView).setTableSynId(tableSynId);
		verify(mockView, atLeastOnce()).setXAxisColumnName(X_COLUMN_NAME);
		verify(mockView).addYAxisColumn(Y_COLUMN_NAME);
		verify(mockView).setGraphType(type);
		verify(mockView).setBarMode(barMode);
		verify(mockView).setAdvancedClause(advancedClause);
		verify(mockView).setAdvancedUIVisible(true);
	}
	
	@Test
	public void testGetXColumnFromSql() {
		assertNull(getXColumnFromSql("X_column from missing_select"));
		assertNull(getXColumnFromSql(null));
		assertNull(getXColumnFromSql("SELECT x FROM syn2")); // graph requires more than one column
		assertEquals("MY_x_column", getXColumnFromSql("SeLeCt MY_x_column, y1, y2 FrOM syn2"));
		assertEquals("x column", getXColumnFromSql("SELECT \'x column\', y1, y2 from syn1"));
		assertEquals("x column", getXColumnFromSql("SELECT \"x column\", y1, y2 from syn1"));
	}
	
	@Test
	public void testGetYColumnsFromSql() {
		assertNull(getYColumnsFromSql("select missing_from, y"));
		assertNull(getYColumnsFromSql(null));
		assertNull(getYColumnsFromSql("SELECT x FROM syn2")); // graph requires more than one column
		assertArrayEquals(new String[]{"y1"}, getYColumnsFromSql("SeLeCt MY_x_column, y1 FrOM syn2"));
		assertArrayEquals(new String[]{"y1 Column", "y2 COLUMN"},  getYColumnsFromSql("SELECT \'x column\', \'y1 Column\',  \"   y2 COLUMN\" from syn1"));
	}
	
	@Test
	public void testGetAdvancedClauseFromQuery() {
		assertNull(getAdvancedClauseFromQuery("select missing_from, y"));
		assertNull(getAdvancedClauseFromQuery(null));
		assertNull(getAdvancedClauseFromQuery("select x, y from syn1"));
		assertEquals("where x>2", getAdvancedClauseFromQuery("select x, y from syn1 where x>2"));
		assertEquals("GrOUP BY x", getAdvancedClauseFromQuery("select x from syn1 GrOUP BY x"));
		assertEquals("where x>2 group by x", getAdvancedClauseFromQuery("select x from syn1 where x>2 group by x"));
	}
	
	@Test
	public void testUpdateDescriptorFromView() {
		Map<String, String> params = new HashMap<>();
		WikiPageKey wikiKey = null;
		DialogCallback callback = null;
		editor.configure(wikiKey, params, callback);

		// simulate table selected, x column selected, and y column added
		String newSynId = "syn999999";
		String newX = "new X";
		String newY1 = "new Y1";
		when(mockXColumnModel.getName()).thenReturn(newX);
		when(mockYColumnModel.getName()).thenReturn(newY1);
		columnModels.add(mockXColumnModel);
		columnModels.add(mockYColumnModel);
		when(mockView.getXAxisColumnName()).thenReturn(newX);
		
		when(mockView.getTableSynId()).thenReturn(newSynId);
		editor.setTableId(newSynId);
		editor.onXColumnChanged();
		editor.onAddYColumn(newY1);
		
		String advancedClause = "WHERE x>1 GROUP BY X";
		when(mockView.getAdvancedClause()).thenReturn(advancedClause);
		String title = "my plot title";
		when(mockView.getTitle()).thenReturn(title);
		String xAxisLabel = "a X axis";
		when(mockView.getXAxisLabel()).thenReturn(xAxisLabel);
		String yAxisLabel = "a Y axis";
		when(mockView.getYAxisLabel()).thenReturn(yAxisLabel);
		GraphType type = GraphType.SCATTER;
		when(mockView.getGraphType()).thenReturn(type);
		
		editor.updateDescriptorFromView();
		
		//verify
		assertEquals("select \"new X\", \"new Y1\" from syn999999 " + advancedClause, params.get(TABLE_QUERY_KEY));
		assertEquals(title, params.get(TITLE));
		assertEquals(xAxisLabel, params.get(X_AXIS_TITLE));
		assertEquals(yAxisLabel, params.get(Y_AXIS_TITLE));
		assertEquals(GraphType.SCATTER.toString(), params.get(TYPE));
	}
	
	@Test
	public void testSetTableIdFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		editor.setTableId("syn2222");
		InOrder order = Mockito.inOrder(mockSynAlert);
		order.verify(mockSynAlert).clear();
		order.verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testAvailableColumns() {
		columnModels.add(mockXColumnModel);
		columnModels.add(mockYColumnModel);
		columnModels.add(mockY2ColumnModel);
		Map<String, String> params = new HashMap<>();
		WikiPageKey wikiKey = null;
		DialogCallback callback = null;
		editor.configure(wikiKey, params, callback);
		verify(mockSynapseClient, never()).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		
		// no columns defined, so it will pick the first column from the column models as the x column, and leave the rest for the available columns
		editor.setTableId("syn2222");
		verify(mockSynapseClient).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).setAvailableColumns(availableColumnNamesCaptor.capture());
		List<String> availableColumnNames = availableColumnNamesCaptor.getValue();
		assertEquals(2, availableColumnNames.size());
		assertTrue(availableColumnNames.contains(Y_COLUMN_NAME));
		assertTrue(availableColumnNames.contains(Y2_COLUMN_NAME));
		verify(mockView).setXAxisColumnName(X_COLUMN_NAME);
		
		//change the x-column to the second column (Y_COLUMN_NAME)
		reset(mockView);
		when(mockView.getXAxisColumnName()).thenReturn(Y_COLUMN_NAME);
		editor.onXColumnChanged();
		verify(mockView).setAvailableColumns(availableColumnNamesCaptor.capture());
		availableColumnNames = availableColumnNamesCaptor.getValue();
		assertEquals(2, availableColumnNames.size());
		assertTrue(availableColumnNames.contains(X_COLUMN_NAME));
		assertTrue(availableColumnNames.contains(Y2_COLUMN_NAME));
		
		//add the y2 column to the y list
		reset(mockView);
		when(mockView.getXAxisColumnName()).thenReturn(Y_COLUMN_NAME);
		editor.onAddYColumn(Y2_COLUMN_NAME);
		verify(mockView).setAvailableColumns(availableColumnNamesCaptor.capture());
		availableColumnNames = availableColumnNamesCaptor.getValue();
		assertEquals(1, availableColumnNames.size());
		assertTrue(availableColumnNames.contains(X_COLUMN_NAME));
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testOnFindTable() {
		editor.onFindTable();
		verify(mockFinder).configure(eq(EntityFilter.PROJECT_OR_TABLE), eq(false), finderCallbackCaptor.capture());
		verify(mockFinder).show();
		DisplayUtils.SelectedHandler<Reference> callback = finderCallbackCaptor.getValue();
		String newTableId = "syn222";
		Reference r = new Reference();
		r.setTargetId(newTableId);
		callback.onSelected(r);
		verify(mockFinder).hide();
		verify(mockView).setTableSynId(newTableId);
	}
}
