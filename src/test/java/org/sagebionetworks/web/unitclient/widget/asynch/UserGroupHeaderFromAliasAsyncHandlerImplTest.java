package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderFromAliasAsyncHandlerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserGroupHeaderFromAliasAsyncHandlerImplTest {
	UserGroupHeaderFromAliasAsyncHandlerImpl asyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GWTWrapper mockGwt;
	String alias = "bob";
	@Mock
	AsyncCallback mockCallback;
	List<UserGroupHeader> resultList;
	@Mock
	UserGroupHeader mockResult;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		asyncHandler = new UserGroupHeaderFromAliasAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt);
		resultList = new ArrayList<UserGroupHeader>();
		AsyncMockStubber.callSuccessWith(resultList).when(mockSynapseJavascriptClient).getUserGroupHeadersByAlias(any(ArrayList.class), any(AsyncCallback.class));
		when(mockResult.getUserName()).thenReturn(alias);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}

	@Test
	public void testSuccess() {
		when(mockGwt.getUniqueAliasName(anyString())).thenReturn(alias);
		// verify no rpc if nothing has been requested.
		asyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseJavascriptClient);

		// add one, simulate single result
		asyncHandler.getUserGroupHeader(alias, mockCallback);
		resultList.add(mockResult);

		asyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getUserGroupHeadersByAlias(any(ArrayList.class), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockResult);
	}

	@Test
	public void testFailure() {
		// simulate exception response
		when(mockGwt.getUniqueAliasName(anyString())).thenReturn("alias");
		Exception ex = new Exception("problem loading batch");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserGroupHeadersByAlias(any(ArrayList.class), any(AsyncCallback.class));
		asyncHandler.getUserGroupHeader(alias, mockCallback);
		asyncHandler.executeRequests();

		verify(mockCallback).onFailure(ex);
	}

	@Test
	public void testNotFound() {
		when(mockGwt.getUniqueAliasName(anyString())).thenReturn(alias);
		// add one, simulate different response
		asyncHandler.getUserGroupHeader(alias, mockCallback);
		resultList.add(mockResult);
		when(mockResult.getUserName()).thenReturn("another alias");
		when(mockGwt.getUniqueAliasName(anyString())).thenReturn("anotheralias");
		asyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getUserGroupHeadersByAlias(any(ArrayList.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}

}
