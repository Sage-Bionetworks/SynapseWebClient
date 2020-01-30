package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Challenge;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadge;
import org.sagebionetworks.web.client.widget.entity.ChallengeBadgeView;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChallengeBadgeTest {

	@Mock
	ChallengeBadgeView mockView;
	@Mock
	EntityHeaderAsyncHandler mockEntityHeaderAsyncHandler;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	ChallengeBadge widget;
	@Mock
	Challenge mockTestChallenge;
	@Mock
	EntityHeader mockChallengeProjectHeader;
	public static final String testProjectName = "my test challenge project";
	public static final String testProjectId = "syn123";

	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new ChallengeBadge(mockView, mockEntityHeaderAsyncHandler, mockSynapseJSNIUtils);
		when(mockTestChallenge.getProjectId()).thenReturn(testProjectId);
		when(mockChallengeProjectHeader.getName()).thenReturn(testProjectName);
		AsyncMockStubber.callSuccessWith(mockChallengeProjectHeader).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
	}

	private void verifyNoProjectIdSet() {
		verify(mockView, never()).setProjectId(anyString());
	}

	/**
	 * Verifies that setHref is called, and returns the value passed to the view
	 * 
	 * @return
	 */
	private String verifyProjectId() {
		ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
		verify(mockView).setProjectId(stringCaptor.capture());
		return stringCaptor.getValue();
	}

	@Test
	public void testConfigure() {
		widget.configure(mockTestChallenge);
		String projectId = verifyProjectId();
		assertEquals(testProjectId, projectId);
	}

	@Test
	public void testConfigureNullChallenge() {
		widget.configure(null);
		verifyNoProjectIdSet();
	}

	@Test
	public void testConfigureNullProjectHeader() {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		widget.configure(mockTestChallenge);
		verifyProjectId();
		verify(mockView, never()).setProjectName(anyString());
	}
}
