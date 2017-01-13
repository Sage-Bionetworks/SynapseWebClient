package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.FileViewDefaultColumns;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileViewDefaultColumnsTest {
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	ColumnModel mockColumn;
	List<ColumnModel> columns;
	@Mock
	AsyncCallback<List<ColumnModel>> mockCallback;
	FileViewDefaultColumns fileViewDefaultColumns;
	@Mock
	Exception mockException;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		columns = Collections.singletonList(mockColumn);
		fileViewDefaultColumns = new FileViewDefaultColumns(mockSynapseClient);
		AsyncMockStubber.callSuccessWith(columns).when(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
	}

	@Test
	public void testGetDefaultColumnsWithIds() {
		boolean isClearIds = false;
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(columns);
		
		//verify results are cached
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		verifyNoMoreInteractions(mockSynapseClient);
		verify(mockCallback, times(2)).onSuccess(columns);
		
		verifyZeroInteractions(mockColumn);
	}
	
	@Test
	public void testGetDefaultColumnsWithIdsFailure() {
		boolean isClearIds = false;
		AsyncMockStubber.callFailureWith(mockException).when(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockCallback).onFailure(mockException);
	}


	@Test
	public void testGetDefaultColumnsWithOutIds() {
		boolean isClearIds = true;
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(columns);
		
		verify(mockColumn).setId(null);
		
		//verify results are cached
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		verifyNoMoreInteractions(mockSynapseClient);
		verify(mockCallback, times(2)).onSuccess(columns);
	}
	
	@Test
	public void testGetDefaultColumnsWithoutIdsFailure() {
		boolean isClearIds = true;
		AsyncMockStubber.callFailureWith(mockException).when(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		fileViewDefaultColumns.getDefaultColumns(isClearIds, mockCallback);
		
		verify(mockSynapseClient).getDefaultColumnsForView(eq(ViewType.file), any(AsyncCallback.class));
		verify(mockCallback).onFailure(mockException);
	}

}
