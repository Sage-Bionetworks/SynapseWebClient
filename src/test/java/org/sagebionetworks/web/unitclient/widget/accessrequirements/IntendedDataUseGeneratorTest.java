package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
	SubmissionInfo mockSubmission2InPage1;
	@Mock
	SubmissionInfoPage mockSubmissionPage2;
	@Mock
	SubmissionInfo mockSubmissionInPage2;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	DateTimeUtils mockDateTimeUtils;

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

		when(mockSubmission2InPage1.getProjectLead()).thenReturn(RESEARCH_PROJECT_2_LEAD);
		when(mockSubmission2InPage1.getInstitution()).thenReturn(RESEARCH_PROJECT_2_INSTITUTION);
		when(mockSubmission2InPage1.getIntendedDataUseStatement()).thenReturn(RESEARCH_PROJECT_2_IDU);
	}

	@Test
	public void testGatherAllSubmissions() {
		widget.gatherAllSubmissions(AR_ID.toString(), mockCallbackP);
		
		verify(mockCallbackP).invoke(stringCaptor.capture());
		String markdownOutput = stringCaptor.getValue();
		
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_LEAD));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_INSTITUTION));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_1_IDU));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_LEAD));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_INSTITUTION));
		assertTrue(markdownOutput.contains(RESEARCH_PROJECT_2_IDU));
	}
}
