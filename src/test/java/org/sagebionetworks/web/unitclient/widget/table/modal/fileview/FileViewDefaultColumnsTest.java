package org.sagebionetworks.web.unitclient.widget.table.modal.fileview;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ViewType;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.table.modal.fileview.FileViewDefaultColumns;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FileViewDefaultColumnsTest {
	
	@Mock
	SynapseClientAsync mockSynapseClient;
	ColumnModel columnModel;
	List<ColumnModel> columns;
	@Mock
	AsyncCallback<List<ColumnModel>> mockCallback;
	FileViewDefaultColumns fileViewDefaultColumns;
	@Mock
	Exception mockException;
	AdapterFactory adapterFactory;
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		columnModel = new ColumnModel();
		columnModel.setId("not null");
		adapterFactory = new AdapterFactoryImpl();
		columns = Collections.singletonList(columnModel);
		fileViewDefaultColumns = new FileViewDefaultColumns(mockSynapseClient, adapterFactory);
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
		
		//verify column id is cleared in the callback
		columnModel.setId(null);
		verify(mockCallback).onSuccess(columns);
		
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
