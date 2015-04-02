package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleResults;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.RowReferenceSet;
import org.sagebionetworks.repo.model.table.SelectColumn;
import org.sagebionetworks.repo.model.table.TableFileHandleResults;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchTableFileHandleProviderSingleton;
import org.sagebionetworks.web.client.widget.asynch.TableFileHandleRequest;
import org.sagebionetworks.web.client.widget.asynch.TimerProvider;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsynchTableFileHandleProviderSingletonTest {
	
	SynapseClientAsync mockSynpaseClient;
	TimerProvider timerProviderStub;
	AsynchTableFileHandleProviderSingleton singleton;
	String fileHandleId;
	String tableId;
	ColumnModel column;
	Long rowId;
	Long rowVersion;
	CellAddress address;
	Callback<FileHandle, Throwable> mockCallback;
	SelectColumn selectColumn;
	FileHandleResults fhr;
	FileHandle handle;
	TableFileHandleResults results;
	
	@Before
	public void before(){
		mockSynpaseClient = Mockito.mock(SynapseClientAsync.class);
		timerProviderStub = new TimerProviderStub();
		singleton = new AsynchTableFileHandleProviderSingleton(mockSynpaseClient, timerProviderStub);
		
		fileHandleId = "123";
		tableId = "syn456";
		column = new ColumnModel();
		column.setId("999");
		column.setName("foo");

		rowId = 3L;
		rowVersion = 2L;
		address = new CellAddress(tableId, column, rowId, rowVersion);
		mockCallback = Mockito.mock(Callback.class);
		// Results
		selectColumn = new SelectColumn();
		selectColumn.setId(column.getId());
		fhr = new FileHandleResults();
		handle = new S3FileHandle();
		handle.setId(fileHandleId);
		// Add the handle twice.
		fhr.setList(Arrays.asList(handle, handle));
		results = new TableFileHandleResults();
		results.setTableId(tableId);
		results.setHeaders(Arrays.asList(selectColumn));
		results.setRows(Arrays.asList(fhr));
	}
	
	@Test
	public void testSingleRequsetSuccess(){
		// the service call
		AsyncMockStubber.callSuccessWith(results).when(mockSynpaseClient).getTableFileHandle(any(RowReferenceSet.class), any(AsyncCallback.class));
		TableFileHandleRequest request = new TableFileHandleRequest(fileHandleId, address, mockCallback);
		// This is the call under test.
		singleton.requestFileHandle(request);
		// The callback should be passed the filehandle once.
		verify(mockCallback).onSuccess(handle);
		// If we call it again with the same request it should only come back once
		reset(mockCallback);
		singleton.requestFileHandle(request);
		// The callback should be passed the filehandle once.
		verify(mockCallback).onSuccess(handle);
	}

	@Test
	public void testSingleRequsetFailure(){
		Throwable error = new Throwable("error");
		// the service call
		AsyncMockStubber.callFailureWith(error).when(mockSynpaseClient).getTableFileHandle(any(RowReferenceSet.class), any(AsyncCallback.class));
		TableFileHandleRequest request = new TableFileHandleRequest(fileHandleId, address, mockCallback);
		// This is the call under test.
		singleton.requestFileHandle(request);
		// The callback should be passed the filehandle once.
		verify(mockCallback).onFailure(error);
	}
}
