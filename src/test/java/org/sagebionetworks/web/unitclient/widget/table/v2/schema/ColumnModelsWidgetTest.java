package org.sagebionetworks.web.unitclient.widget.table.v2.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.TableBundle;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler;
import org.sagebionetworks.web.client.widget.table.KeyboardNavigationHandler.RowOfWidgets;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRow;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelTableRowViewer;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelUtils;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsView.ViewType;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsViewBase;
import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnModelsWidget;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for ColumnModelsViewWidget
 * 
 * @author jmhill
 *
 */
public class ColumnModelsWidgetTest {

	AdapterFactory adapterFactory;
	@Mock
	ColumnModelsViewBase mockBaseView;
	@Mock
	ColumnModelsView mockViewer;
	@Mock
	ColumnModelsEditorWidget mockEditor;
	@Mock
	EntityUpdatedHandler mockUpdateHandler;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseClientAsync mockSynapseClient;
	ColumnModelsWidget widget;
	@Mock
	EntityBundle mockBundle;
	TableEntity table;
	TableBundle tableBundle;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		adapterFactory = new AdapterFactoryImpl();
		table = new TableEntity();
		table.setId("syn123");
		tableBundle = new TableBundle();
		when(mockBundle.getEntity()).thenReturn(table);
		when(mockBundle.getTableBundle()).thenReturn(tableBundle);
		when(mockGinInjector.createNewColumnModelsView()).thenReturn(mockViewer);
		when(mockGinInjector.createColumnModelEditorWidget()).thenAnswer(new Answer<ColumnModelTableRowEditorWidget >() {
			@Override
			public ColumnModelTableRowEditorWidget answer(InvocationOnMock invocation)
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
		widget = new ColumnModelsWidget(mockBaseView, mockGinInjector, mockSynapseClient, mockEditor);
		verify(mockBaseView).setViewer(mockViewer);
		verify(mockBaseView).setEditor(mockEditor);
		when(mockEditor.validate()).thenReturn(true);
	}
	
	@Test
	public void testConfigure(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		verify(mockViewer).configure(ViewType.VIEWER, isEditable);
		// All rows should be added to both the viewer and editor
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	@Test
	public void testOnEditColumns(){
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// show the editor
		widget.onEditColumns();
		verify(mockEditor).configure(table, schema);
		verify(mockBaseView).showEditor();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testOnEditNonEditable(){
		boolean isEditable = false;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// should fail
		widget.onEditColumns();
	}
	
	@Test
	public void testOnSaveSuccess() throws JSONObjectAdapterException{
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		AsyncMockStubber.callSuccessWith(null).when(mockEditor).setTableSchema(any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		verify(mockBaseView).hideEditor();
		verify(mockBaseView).hideErrors();
		// Save success should be called.
		verify(mockUpdateHandler).onPersistSuccess(any(EntityUpdatedEvent.class));
	}
	
	@Test
	public void testOnSaveSuccessValidateFalse() throws JSONObjectAdapterException{
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		when(mockEditor.validate()).thenReturn(false);
		// Now call save
		widget.onSave();
		verify(mockBaseView, never()).setLoading();
		verify(mockBaseView, never()).hideEditor();
		verify(mockBaseView, never()).hideErrors();
		// Save success should be called.
		verify(mockBaseView).showError(ColumnModelsWidget.SEE_THE_ERROR_S_ABOVE);
	}
	
	@Test
	public void testOnSaveFailure() throws JSONObjectAdapterException{
		boolean isEditable = true;
		List<ColumnModel> schema = TableModelTestUtils.createOneOfEachType(true);
		tableBundle.setColumnModels(schema);
		widget.configure(mockBundle, isEditable, mockUpdateHandler);
		// Show the dialog
		widget.onEditColumns();
		String errorMessage = "Something went wrong";
		AsyncMockStubber.callFailureWith(new RestServiceException(errorMessage)).when(mockEditor).setTableSchema(any(AsyncCallback.class));
		// Now call save
		widget.onSave();
		verify(mockBaseView, times(1)).setLoading();
		// The editor must not be hidden on an error.
		verify(mockBaseView, never()).hideEditor();
		verify(mockBaseView).showError(errorMessage);
		// only the original columns should be applied to the view.
		verify(mockViewer, times(schema.size())).addColumn(any(ColumnModelTableRow.class));
	}
	
	
}
