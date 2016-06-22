package org.sagebionetworks.web.unitclient.widget.table.v2.results;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.QueryResult;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.RowReferenceSetResults;
import org.sagebionetworks.repo.model.table.RowSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.TablePageWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.asynch.JobTrackingWidgetStub;
import org.sagebionetworks.web.unitclient.widget.table.v2.TableModelTestUtils;

/**
 * Unit tests for QueryResultEditorWidget.
 * 
 * @author John
 *
 */
public class QueryResultEditorWidgetTest {

	QueryResultEditorView mockView;
	TablePageWidget mockPageWidget;
	QueryResultEditorWidget widget;
	JobTrackingWidgetStub jobTrackingStub;
	GlobalApplicationState mockGlobalState;
	Callback mockCallback;
	

	QueryResult results;
	RowSet rowSet;

	SelectColumn select;
	
	Row rowOne;
	Row rowTwo;
	List<ColumnModel> schema;
	List<SelectColumn> headers;
	List<Row> updates;
	QueryResultBundle bundle;
	
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockView = Mockito.mock(QueryResultEditorView.class);
		mockPageWidget = Mockito.mock(TablePageWidget.class);
		jobTrackingStub = new JobTrackingWidgetStub();
		mockGlobalState = Mockito.mock(GlobalApplicationState.class);
		mockCallback = Mockito.mock(Callback.class);
		widget = new QueryResultEditorWidget(mockView, mockPageWidget, jobTrackingStub, mockGlobalState);

		schema = TableModelTestUtils.createColumsWithNames("one", "two");
		headers = TableModelTestUtils.buildSelectColumns(schema);
		rowOne = new Row();
		rowOne.setRowId(1L);
		rowOne.setValues(Arrays.asList("1,1","1,2"));
		rowTwo = new Row();
		rowTwo.setRowId(2L);
		rowTwo.setValues(Arrays.asList("2,1","2,2"));
		
		rowSet = new RowSet();
		rowSet.setTableId("syn999");
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
	
	
	@Test
	public void testOnEdit(){
		widget.showEditor(bundle, mockCallback);
		verify(mockView).setErrorMessageVisible(false);
		verify(mockView).hideProgress();
		verify(mockView).setSaveButtonLoading(false);
		verify(mockView, times(2)).showEditor();
		verify(mockGlobalState).setIsEditing(true);
		verify(mockGlobalState, never()).setIsEditing(false);
	}
	
	@Test
	public void testOnCancelNoChanges(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		// No changes
		widget.onCancel();
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
	}
	
	@Test
	public void testOnCancelWithChangesConfirmOkay(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		
		updates.get(0).setValues(Arrays.asList("update1","update2"));
		// Invoking the callback occurs on okay;
		AsyncMockStubber.callWithInvoke().when(mockView).showConfirmDialog(anyString(), any(Callback.class));
		
		widget.onCancel();
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class));
	}
	
	@Test
	public void testOnCancelWithChangesConfirmCanceld(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		
		updates.get(0).setValues(Arrays.asList("update1","update2"));
		// no invoke
		AsyncMockStubber.callNoInvovke().when(mockView).showConfirmDialog(anyString(), any(Callback.class));
	
		widget.onCancel();
		verify(mockGlobalState, never()).setIsEditing(false);
		verify(mockView, never()).hideEditor();
		verify(mockView).showConfirmDialog(anyString(), any(Callback.class));
	}
	
	@Test
	public void testOnSaveNoChanges(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		
		widget.onSave();
		verify(mockView).setSaveButtonLoading(true);
		verify(mockView).setErrorMessageVisible(false);
		// end false
		verify(mockGlobalState).setIsEditing(false);
		verify(mockView).hideEditor();
		verify(mockPageWidget, never()).isValid();
		// callback should not be invoked
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testOnSaveWithChagnesNotValid(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1","update2"));
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
		// callback should not be invoked
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testOnSaveWithChagnesValidJobSuccessful(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1","update2"));
		// not valid
		when(mockPageWidget.isValid()).thenReturn(true);
		// setup successful job
		jobTrackingStub.setResponse(new RowReferenceSetResults());
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
		verify(mockCallback).invoke();
	}
	
	@Test
	public void testOnSaveWithChagnesValidJobFailed(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1","update2"));
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
		verify(mockCallback, never()).invoke();
	}
	
	@Test
	public void testOnSaveWithChagnesValidJobCanceled(){
		widget.showEditor(bundle, mockCallback);
		reset(mockView);
		reset(mockGlobalState);
		// make changes
		updates.get(0).setValues(Arrays.asList("update1","update2"));
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
		
		// The editor should be hidden and the callback invoked
		// end false
		verify(mockGlobalState).setIsEditing(false);
		// The callback should be invoked
		verify(mockCallback).invoke();
	}
	
}
