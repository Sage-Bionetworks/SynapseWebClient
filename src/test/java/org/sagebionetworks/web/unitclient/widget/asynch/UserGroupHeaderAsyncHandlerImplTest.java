package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.UserGroupHeaderAsyncHandlerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserGroupHeaderAsyncHandlerImplTest {
	UserGroupHeaderAsyncHandlerImpl asyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	UserGroupHeaderResponsePage mockUserGroupHeaderResponsePage;
	String userId = "123";
	@Mock
	AsyncCallback mockCallback;
	List<UserGroupHeader> resultList;
	@Mock
	UserGroupHeader mockResult;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		asyncHandler = new UserGroupHeaderAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt);
		resultList = new ArrayList<UserGroupHeader>();
		AsyncMockStubber.callSuccessWith(mockUserGroupHeaderResponsePage).when(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		when(mockUserGroupHeaderResponsePage.getChildren()).thenReturn(resultList);
		when(mockResult.getOwnerId()).thenReturn(userId);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}

	@Test
	public void testSuccess() {
		// verify no rpc if nothing has been requested.
		asyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseJavascriptClient);

		// add one, simulate single result
		asyncHandler.getUserGroupHeader(userId, mockCallback);
		resultList.add(mockResult);

		asyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockResult);
	}

	@Test
	public void testFailure() {
		// simulate exception response
		Exception ex = new Exception("problem loading batch");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		asyncHandler.getUserGroupHeader(userId, mockCallback);
		asyncHandler.executeRequests();

		verify(mockCallback).onFailure(ex);
	}

	@Test
	public void testNotFound() {
		when(mockResult.getOwnerId()).thenReturn("another id");
		// add one, simulate different response
		asyncHandler.getUserGroupHeader(userId, mockCallback);
		resultList.add(mockResult);

		asyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}

}
