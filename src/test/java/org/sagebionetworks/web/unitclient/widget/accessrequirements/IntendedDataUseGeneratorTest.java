package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.repo.model.dataaccess.SubmissionInfo;
import org.sagebionetworks.repo.model.dataaccess.SubmissionInfoPage;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.IntendedDataUseGenerator;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.BigPromptModalView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class IntendedDataUseGeneratorTest {
	public static final Long AR_ID = 8888L;
	public static final String NEXT_PAGE_TOKEN = "28DJcDS";
	public static final String SUBMISSION1_USER_ID = "10001";
	public static final String USER1_ALIAS = "the-one";
	public static final String SUBMISSION2_USER_ID = "10002";
	public static final String USER2_ALIAS = "the-two";
	public static final String USER3_ID = "10003";
	public static final String USER3_ALIAS = "the-three";
	public static final String RESEARCH_PROJECT_1_ID = "rp1";
	public static final String RESEARCH_PROJECT_1_LEAD = "Luke";
	public static final String RESEARCH_PROJECT_1_INSTITUTION = "Rebel";
	public static final String RESEARCH_PROJECT_1_IDU = "This is the IDU of research project 1";
	public static final String RESEARCH_PROJECT_2_ID = "rp2";
	public static final String RESEARCH_PROJECT_2_LEAD = "Vader";
	public static final String RESEARCH_PROJECT_2_INSTITUTION = "Empire";
	public static final String RESEARCH_PROJECT_2_IDU = "This is the IDU of research project 2";

	IntendedDataUseGenerator widget;
	@Mock
	PopupUtilsView mockPopupUtils;
	@Mock
	IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	AccessRequirement mockAccessRequirement;
	@Mock
	CallbackP mockCallbackP;
	@Mock
	BigPromptModalView mockBigPromptModal;
	@Mock
	SubmissionInfoPage mockSubmissionPage1;
	@Mock
	SubmissionInfo mockSubmission1InPage1;
	@Mock
	AccessorChange mockAccessorChange;
	@Mock
	SubmissionInfo mockSubmission2InPage1;
	@Mock
	SubmissionInfoPage mockSubmissionPage2;
	@Mock
	SubmissionInfo mockSubmissionInPage2;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	
	@Mock
	UserProfile mockUserProfile1;
	@Mock
	UserProfile mockUserProfile2;
	@Mock
	UserProfile mockUserProfile3;

	@Before
	public void setUp() throws Exception {
		when(mockAccessRequirement.getId()).thenReturn(AR_ID);
		when(mockSubmissionPage1.getNextPageToken()).thenReturn(NEXT_PAGE_TOKEN);
		List<SubmissionInfo> page1 = new ArrayList<>();
		page1.add(mockSubmission1InPage1);
		page1.add(mockSubmission2InPage1);
		when(mockSubmissionPage1.getResults()).thenReturn(page1);
		when(mockSubmissionPage2.getResults()).thenReturn(Collections.singletonList(mockSubmissionInPage2));
		widget = new IntendedDataUseGenerator(mockJsClient,mockPopupUtils, mockDateTimeUtils);
		AsyncMockStubber.callSuccessWith(mockSubmissionPage1, mockSubmissionPage2).when(mockJsClient).listApprovedSubmissionInfo(anyString(), anyString(), any(AsyncCallback.class));

		when(mockSubmission1InPage1.getProjectLead()).thenReturn(RESEARCH_PROJECT_1_LEAD);
		when(mockSubmission1InPage1.getInstitution()).thenReturn(RESEARCH_PROJECT_1_INSTITUTION);
		when(mockSubmission1InPage1.getIntendedDataUseStatement()).thenReturn(RESEARCH_PROJECT_1_IDU);
		when(mockSubmission1InPage1.getSubmittedBy()).thenReturn(SUBMISSION1_USER_ID);

		when(mockSubmission2InPage1.getProjectLead()).thenReturn(RESEARCH_PROJECT_2_LEAD);
		when(mockSubmission2InPage1.getInstitution()).thenReturn(RESEARCH_PROJECT_2_INSTITUTION);
		when(mockSubmission2InPage1.getIntendedDataUseStatement()).thenReturn(RESEARCH_PROJECT_2_IDU);
		when(mockSubmission2InPage1.getSubmittedBy()).thenReturn(SUBMISSION2_USER_ID);
		when(mockSubmission2InPage1.getAccessorChanges()).thenReturn(Collections.singletonList(mockAccessorChange));
		when(mockAccessorChange.getUserId()).thenReturn(USER3_ID);
		when(mockAccessorChange.getType()).thenReturn(AccessType.GAIN_ACCESS);
		when(mockUserProfile1.getOwnerId()).thenReturn(SUBMISSION1_USER_ID);
		when(mockUserProfile1.getUserName()).thenReturn(USER1_ALIAS);
		when(mockUserProfile2.getOwnerId()).thenReturn(SUBMISSION2_USER_ID);
		when(mockUserProfile2.getUserName()).thenReturn(USER2_ALIAS);
		when(mockUserProfile3.getOwnerId()).thenReturn(USER3_ID);
		when(mockUserProfile3.getUserName()).thenReturn(USER3_ALIAS);

		List profiles = new ArrayList<>();
		profiles.add(mockUserProfile1);
		profiles.add(mockUserProfile2);
		profiles.add(mockUserProfile3);
		AsyncMockStubber.callSuccessWith(profiles).when(mockJsClient).listUserProfiles(anyList(), any(AsyncCallback.class));
	}

	@Test
	public void testGatherAllSubmissions() {
		widget.gatherAllSubmissions(AR_ID.toString(), mockCallbackP);
		
		verify(mockCallbackP).invoke(stringCaptor.capture());
		String markdownOutput = stringCaptor.getValue();
		
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_LEAD));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_INSTITUTION));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_IDU));
		assertTrue(markdownOutput.contains("@" + USER1_ALIAS)); // submission1 submitted by user1
		assertTrue(markdownOutput.contains("@" + USER3_ALIAS)); // submission1 accessor changes include user3
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_LEAD));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_INSTITUTION));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_IDU));
		assertTrue(markdownOutput.contains("@" + USER2_ALIAS)); // submission2 submitted by user2
	}
}
