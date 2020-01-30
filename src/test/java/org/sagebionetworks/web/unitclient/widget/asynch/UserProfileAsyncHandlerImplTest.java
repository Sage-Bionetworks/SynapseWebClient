package org.sagebionetworks.web.unitclient.widget.asynch;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandlerImpl;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserProfileAsyncHandlerImplTest {
	UserProfileAsyncHandlerImpl userProfileAsyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	GWTWrapper mockGwt;
	String userId = "123";
	@Mock
	AsyncCallback mockCallback;
	List<UserProfile> resultList;
	@Mock
	UserProfile mockUserProfile;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		userProfileAsyncHandler = new UserProfileAsyncHandlerImpl(mockSynapseJavascriptClient, mockGwt);
		resultList = new ArrayList<UserProfile>();
		AsyncMockStubber.callSuccessWith(resultList).when(mockSynapseJavascriptClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		when(mockUserProfile.getOwnerId()).thenReturn(userId);
	}

	@Test
	public void testConstructor() {
		verify(mockGwt).scheduleFixedDelay(any(Callback.class), anyInt());
	}

	@Test
	public void testSuccess() {
		// verify no rpc if nothing has been requested.
		userProfileAsyncHandler.executeRequests();
		verifyZeroInteractions(mockSynapseJavascriptClient);

		// add one, simulate single file response
		userProfileAsyncHandler.getUserProfile(userId, mockCallback);
		resultList.add(mockUserProfile);

		userProfileAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onSuccess(mockUserProfile);
	}

	@Test
	public void testFailure() {
		// simulate exception response
		Exception ex = new Exception("problem loading batch");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		userProfileAsyncHandler.getUserProfile(userId, mockCallback);
		userProfileAsyncHandler.executeRequests();

		verify(mockCallback).onFailure(ex);
	}

	@Test
	public void testNotFound() {
		when(mockUserProfile.getOwnerId()).thenReturn("another id");
		// add one, simulate different response
		userProfileAsyncHandler.getUserProfile(userId, mockCallback);
		resultList.add(mockUserProfile);

		userProfileAsyncHandler.executeRequests();
		verify(mockSynapseJavascriptClient).listUserProfiles(anyList(), any(AsyncCallback.class));
		verify(mockCallback).onFailure(any(Throwable.class));
	}

}
