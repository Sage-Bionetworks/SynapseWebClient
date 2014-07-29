package org.sagebionetworks.web.unitclient.widget.table.v2;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowEditor;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.ColumnModelsWidget;
import org.sagebionetworks.web.client.widget.table.v2.TableModelUtils;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for ColumnModelsViewWidget
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidgetTest {

	AdapterFactory adapterFactory;
	ColumnModelsViewBase mockBaseView;
	ColumnModelsView mockViewer;
	ColumnModelsView mockEditor;
	PortalGinInjector mockGinInjector;
	SynapseClientAsync mockSynapseClient;
	TableModelUtils tableModelUtils;
	ColumnModelsWidget widget;
	
	@Before
	public void before(){
		mockBaseView = Mockito.mock(ColumnModelsViewBase.class);
		mockViewer = Mockito.mock(ColumnModelsView.class);
		mockEditor = Mockito.mock(ColumnModelsView.class);
		mockGinInjector = Mockito.mock(PortalGinInjector.class);
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		adapterFactory = new AdapterFactoryImpl();
		tableModelUtils = new TableModelUtils(adapterFactory);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer, mockEditor);
		when(mockGinInjector.createNewColumnModelTableRowEditor()).thenAnswer(new Answer<ColumnModelTableRowEditor>() {
			@Override
			public ColumnModelTableRowEditor answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowEditorStub();
			}
		});
		when(mockGinInjector.createNewColumnModelTableRowViewer()).thenAnswer(new Answer<ColumnModelTableRowViewer>() {
			@Override
			public ColumnModelTableRowViewer answer(InvocationOnMock invocation)
					throws Throwable {
				return new ColumnModelTableRowViewerStub();
			}
		});
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, tableModelUtils);
		verify(mockBaseView, times(1)).setViewer(mockViewer);
		verify(mockBaseView, times(1)).setEditor(mockEditor);
	}
	
	@Test
	public void testConfigure(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		verify(mockViewer, times(1)).configure(ViewType.VIEWER, isEdtiable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor, times(1)).configure(ViewType.EDITOR, isEdtiable);
		verify(mockEditor, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
		verify(mockBaseView, times(1)).showEditor();
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEdtiable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// should fail
		widget.onEditColumns();
	}
	
	@Test
	public void testAddNewColumn(){
		boolean isEdtiable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure("syn123", schema, isEdtiable);
		// show the editor
		widget.onEditColumns();
		// This should add a new string column
		widget.addNewColumn();
		// A string should be added...
		ColumnModel newModel = new ColumnModel();
		newModel.setColumnType(ColumnModelsWidget.DEFAULT_NEW_COLUMN_TYPE);
		newModel.setMaximumSize(ColumnModelsWidget.DEFAULT_STRING_MAX_SIZE);
		schema.add(newModel);
		// Extract the columns from the editor
		List<ColumnModel> clone = widget.getEditedColumnModels();
		assertEquals(schema, clone);
	}
	
	@Test
	public void testOnSaveSuccess() throws JSONObjectAdapterException{
		boolean isEdtiable = true;
		String tableId = "syn123";
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		widget.configure(tableId, schema, isEdtiable);
		List<String> results = tableModelUtils.toJSONList(schema);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).setTableSchema(anyString(), any(List.class), any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
	}
}
