package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget.VIEW_RECENTLY_CHANGED_KEY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.file.BulkFileDownloadResponse;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.EntityUpdateFailureCode;
import org.sagebionetworks.repo.model.table.EntityUpdateResult;
import org.sagebionetworks.repo.model.table.EntityUpdateResults;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowReferenceSetResults;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.TableSchemaChangeResponse;
import org.sagebionetworks.repo.model.table.TableUpdateResponse;
import org.sagebionetworks.repo.model.table.TableUpdateTransactionResponse;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.modal.fileview.TableType;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;
import com.google.gwt.event.shared.EventBus;

/**
 * Unit tests for QueryResultEditorWidget.
 * 
 * @author John
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class QueryResultEditorWidgetTest {
	@Mock
	QueryResultEditorView mockView;
	@Mock
	TablePageWidget mockPageWidget;
	QueryResultEditorWidget widget;
	JobTrackingWidgetStub jobTrackingStub;
	@Mock
	GlobalApplicationState mockGlobalState;

	QueryResult results;
	RowSet rowSet;

	SelectColumn select;

	Row rowOne;
	Row rowTwo;
	List<ColumnModel> schema;
	List<SelectColumn> headers;
	List<Row> updates;
	QueryResultBundle bundle;
	@Mock
	TableUpdateTransactionResponse mockTableUpdateTransactionResponse;
	@Mock
	BulkFileDownloadResponse mockWrongType;
	@Mock
	EntityUpdateResults mockEntityUpdateResults;
	@Mock
	TableSchemaChangeResponse mockTableSchemaChangeResponse;
	@Mock
	EntityUpdateResult mockEntityUpdateResult1;
	@Mock
	EntityUpdateResult mockEntityUpdateResult2;
	@Mock
	ClientCache mockClientCache;
	@Mock
	EventBus mockEventBus;
	List<TableUpdateResponse> tableUpdateResults;
	TableType tableType;
	public static final String ENTITY_ID = "syn999";

	@Before
	public void before() throws JSONObjectAdapterException {
		jobTrackingStub = new JobTrackingWidgetStub();
		widget = new QueryResultEditorWidget(mockView, mockPageWidget, jobTrackingStub, mockGlobalState, mockClientCache, mockEventBus);

		schema = TableModelTestUtils.createColumsWithNames("one", "two");
		headers = TableModelTestUtils.buildSelectColumns(schema);
		rowOne = new Row();
		rowOne.setRowId(1L);
		rowOne.setValues(Arrays.asList("1,1", "1,2"));
		rowTwo = new Row();
		rowTwo.setRowId(2L);
		rowTwo.setValues(Arrays.asList("2,1", "2,2"));

		rowSet = new RowSet();
		rowSet.setTableId(ENTITY_ID);
		rowSet.setRows(Arrays.asList(rowOne, rowTwo));
		updates = TableModelTestUtils.cloneObject(rowSet.getRows(), Row.class);

		results = new QueryResult();
		results.setQueryResults(rowSet);
		bundle = new QueryResultBundle();
		bundle.setMaxRowsPerPage(123L);
		bundle.setQueryCount(88L);
		bundle.setQueryResult(results);
		// By default the view returns a copy of the data.
		when(mockPageWidget.extractHeaders()).thenReturn(schema);
		when(mockPageWidget.extractRowSet()).thenReturn(updates);
		tableUpdateResults = new ArrayList<TableUpdateResponse>();
		when(mockTableUpdateTransactionResponse.getResults()).thenReturn(tableUpdateResults);
		// by default, edit Table results (not a view)
		tableType = TableType.table;
	}

	@Test
	public void testOnSelectionChangedNone() {
		when(mockPageWidget.isOneRowOrMoreRowsSelected()).thenReturn(false);
		widget.onSelectionChanged();
		verify(mockView).setDeleteButtonEnabled(false);
	}

	@Test
	public void testOnSelectionChangedOne() {
		when(mockPageWidget.isOneRowOrMoreRowsSelected()).thenReturn(true);
		widget.onSelectionChanged();
		verify(mockView).setDeleteButtonEnabled(true);
	}

	@Test
	public void testOnDeleteSelected() {
		widget.onDeleteSelected();
		verify(mockPageWidget).onDeleteSelected();
	}

	@Test
	public void testOnSelectAll() {
		widget.onSelectAll();
		verify(mockPageWidget).onSelectAll();
	}

	@Test
	public void testOnSelectNone() {
		widget.onSelectNone();
		verify(mockPageWidget).onSelectNone();
	}

	@Test
	public void testOnToggleSelect() {
		widget.onToggleSelect();
		verify(mockPageWidget).onToggleSelect();
	}


	@Test
	public void testOnEdit() {
		widget.showEditor(bundle, tableType);
		verify(mockView).setErrorMessageVisible(false);
		verify(mockView).hideProgress();
		verify(mockView).setSaveButtonLoading(false);
		verify(mockView).setAddRowButtonVisible(true);
		verify(mockView).setButtonToolbarVisible(true);
		verify(mockView, times(2)).showEditor();
		verify(mockGlobalState).setIsEditing(true);
		verify(mockGlobalState, never()).setIsEditing(false);
	}

	@Test
	public void testOnEditView() {
		tableType = TableType.files;
		widget.showEditor(bundle, tableType);
		verify(mockView).setAddRowButtonVisible(false);
		verify(mockView).setButtonToolbarVisible(false);
	}


	@Test
	public void testOnCancelNoChanges() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// No changes
		widget.onCancel();
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
	}

	@Test
	public void testOnCancelWithChangesConfirmOkay() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);

		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// Invoking the callback occurs on okay;
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDialog(anyString(), any(Callback.class));

		widget.onCancel();
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class));
	}

	@Test
	public void testOnCancelWithChangesConfirmCanceld() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);

		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// no invoke
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDialog(anyString(), any(Callback.class));

		widget.onCancel();
		verify(mockGlobalState, never()).setIsEditing(false);
		verify(mockView, never()).hideEditor();
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class));
	}

	@Test
	public void testOnSaveNoChanges() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);

		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockView).setErrorMessageVisible(false);
		// end false
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
		verify(mockPageWidget, never()).isValid();
		// update should not be invoked
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testOnSaveWithChagnesNotValid() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(false);
		// the call
		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockGlobalState, never()).setIsEditing(false);
		verify(mockView, never()).hideEditor();
		verify(mockView).setErrorMessageVisible(true);
		verify(mockView).showEditor();
		verify(mockView).hideProgress();
		verify(mockView).showErrorMessage(QueryResultEditorWidget.SEE_THE_ERRORS_ABOVE);
		verify(mockView).setSaveButtonLoading(false);
		// update should not be invoked
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testOnSaveWithChangesValidJobSuccessful() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(true);
		// setup successful job
		TableUpdateTransactionResponse response = new TableUpdateTransactionResponse();
		List<TableUpdateResponse> results = new ArrayList<TableUpdateResponse>();
		results.add(new RowReferenceSetResults());
		response.setResults(results);
		jobTrackingStub.setResponse(response);
		// the call
		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockView, never()).setErrorMessageVisible(true);
		verify(mockView, never()).showErrorMessage(anyString());
		// while the job is running the editor should not be visible
		verify(mockView, times(2)).hideEditor();
		// progress should be visible while the job runs.
		verify(mockView).showProgress();

		// The editor should be hidden and the callback invoked
		// end false
		verify(mockGlobalState).setIsEditing(false);

		// The callback should be invoked
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));

		verify(mockClientCache, never()).put(anyString(), anyString(), anyLong());
	}


	@Test
	public void testOnSaveWithChangesValidJobSuccessfulIsView() {
		tableType = TableType.projects;
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(true);
		// setup successful job
		TableUpdateTransactionResponse response = new TableUpdateTransactionResponse();
		List<TableUpdateResponse> results = new ArrayList<TableUpdateResponse>();
		EntityUpdateResult entityUpdateResult = new EntityUpdateResult();
		EntityUpdateResults entityUpdateResults = new EntityUpdateResults();
		entityUpdateResults.setUpdateResults(Collections.singletonList(entityUpdateResult));
		results.add(entityUpdateResults);
		response.setResults(results);
		jobTrackingStub.setResponse(response);
		// the call
		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockView, never()).setErrorMessageVisible(true);
		verify(mockView, never()).showErrorMessage(anyString());
		// while the job is running the editor should not be visible
		verify(mockView, times(2)).hideEditor();
		// progress should be visible while the job runs.
		verify(mockView).showProgress();

		// The editor should be hidden and the callback invoked
		// end false
		verify(mockGlobalState).setIsEditing(false);

		// The event should be sent
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));

		verify(mockClientCache).put(eq(ENTITY_ID + VIEW_RECENTLY_CHANGED_KEY), anyString(), anyLong());
	}


	@Test
	public void testOnSaveWithChagnesValidJobFailed() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(true);
		// setup failed job
		String error = "some errror";
		jobTrackingStub.setError(new Throwable(error));
		// the call
		widget.onSave();
		// start with button loading
		verify(mockView).setSaveButtonLoading(true);
		// while the job is running the editor should not be visible
		verify(mockView).hideEditor();
		// progress should be visible while the job runs.
		verify(mockView).showProgress();
		// After the job fails the editor should be visible
		verify(mockView).hideEditor();
		// After the job fails the progress should not be visible
		verify(mockView).hideProgress();

		verify(mockView).setErrorMessageVisible(true);
		verify(mockView).showErrorMessage(error);
		// The save button must be re-enabled on error
		verify(mockView).setSaveButtonLoading(false);

		// still editing when fails.
		verify(mockGlobalState, never()).setIsEditing(false);
		verify(mockEventBus, never()).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testOnSaveWithChagnesValidJobCanceled() {
		widget.showEditor(bundle, tableType);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1", "update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(true);
		// setup job cancel
		jobTrackingStub.setOnCancel(true);
		// the call
		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockView, never()).setErrorMessageVisible(true);
		verify(mockView, never()).showErrorMessage(anyString());
		// while the job is running the editor should not be visible
		verify(mockView, times(2)).hideEditor();
		// progress should be visible while the job runs.
		verify(mockView).showProgress();

		// The editor should be hidden and the event sent
		// end false
		verify(mockGlobalState).setIsEditing(false);
		verify(mockEventBus).fireEvent(any(EntityUpdatedEvent.class));
	}

	@Test
	public void testGetEntityUpdateResultsEmpty() {
		assertNull(QueryResultEditorWidget.getEntityUpdateResults(mockTableUpdateTransactionResponse));
	}

	@Test
	public void testGetEntityUpdateResultsWrongType() {
		assertNull(QueryResultEditorWidget.getEntityUpdateResults(mockWrongType));
	}

	@Test
	public void testGetEntityUpdateResultsSingleValue() {
		tableUpdateResults.add(mockEntityUpdateResults);
		assertEquals(mockEntityUpdateResults, QueryResultEditorWidget.getEntityUpdateResults(mockTableUpdateTransactionResponse));
	}

	@Test
	public void testGetEntityUpdateResultsMultipleValues() {
		tableUpdateResults.add(mockTableSchemaChangeResponse);
		tableUpdateResults.add(mockEntityUpdateResults);
		assertEquals(mockEntityUpdateResults, QueryResultEditorWidget.getEntityUpdateResults(mockTableUpdateTransactionResponse));
	}

	@Test
	public void testGetEntityUpdateResultsFailureSuccessIndex() {
		EntityUpdateResults results = new EntityUpdateResults();
		List<EntityUpdateResult> updateResults = new ArrayList<EntityUpdateResult>();
		updateResults.add(mockEntityUpdateResult1);
		updateResults.add(mockEntityUpdateResult2);
		results.setUpdateResults(updateResults);
		tableUpdateResults.add(results);
		when(mockEntityUpdateResult1.getFailureCode()).thenReturn(EntityUpdateFailureCode.NOT_FOUND);
		when(mockEntityUpdateResult1.getEntityId()).thenReturn("syn29292");
		when(mockEntityUpdateResult1.getFailureMessage()).thenReturn("Not there, buddy");
		assertEquals("<p>syn29292 (NOT_FOUND): Not there, buddy</p>", QueryResultEditorWidget.getEntityUpdateResultsFailures(mockTableUpdateTransactionResponse));
		assertEquals(1, QueryResultEditorWidget.getFirstIndexOfEntityUpdateResultSuccess(mockTableUpdateTransactionResponse));
	}

	@Test
	public void testGetEntityUpdateResultsNoFailures() {
		EntityUpdateResults results = new EntityUpdateResults();
		results.setUpdateResults(Collections.singletonList(mockEntityUpdateResult1));
		tableUpdateResults.add(results);
		when(mockEntityUpdateResult1.getFailureCode()).thenReturn(null);
		when(mockEntityUpdateResult1.getEntityId()).thenReturn("syn29292");
		assertEquals("", QueryResultEditorWidget.getEntityUpdateResultsFailures(mockTableUpdateTransactionResponse));
	}

	@Test
	public void testGetEntityUpdateResultsNoSuccess() {
		EntityUpdateResults results = new EntityUpdateResults();
		results.setUpdateResults(Collections.singletonList(mockEntityUpdateResult1));
		tableUpdateResults.add(results);
		when(mockEntityUpdateResult1.getFailureCode()).thenReturn(EntityUpdateFailureCode.UNAUTHORIZED);
		when(mockEntityUpdateResult1.getEntityId()).thenReturn("syn29292");
		assertEquals(-1, QueryResultEditorWidget.getFirstIndexOfEntityUpdateResultSuccess(mockTableUpdateTransactionResponse));
	}
}
