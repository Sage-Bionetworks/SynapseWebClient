package org.sagebionetworks.web.unitclient.widget.accessrequirements.requestaccess;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectStep1;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateResearchProjectStep1Test {

	CreateResearchProjectStep1 widget;
	@Mock
	CreateResearchProjectWizardStep1View mockView;
	@Mock
	DataAccessClientAsync mockClient;
	@Mock
	CreateDataAccessSubmissionStep2 mockStep2;
	@Mock
	ModalPresenter mockModalPresenter;
	@Mock
	ResearchProject mockResearchProject;
	@Mock
	ManagedACTAccessRequirement mockACTAccessRequirement;
	@Captor
	ArgumentCaptor<ResearchProject> researchProjectCaptor;
	@Mock
	RestrictableObjectDescriptor mockSubject;

	public static final String INSTITUTION = "MIT";
	public static final String INTENDED_DUS = "Do no harm.";
	public static final String PROJECT_LEAD = "Megamind";
	public static final String INSTITUTION2 = "WWU";
	public static final String INTENDED_DUS2 = "Novel research";
	public static final String PROJECT_LEAD2 = "Dr. Megamind";

	public static final Long AR_ID = 98765L;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateResearchProjectStep1(mockView, mockClient, mockStep2);
		widget.setModalPresenter(mockModalPresenter);
		AsyncMockStubber.callSuccessWith(mockResearchProject).when(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));
		when(mockResearchProject.getInstitution()).thenReturn(INSTITUTION);
		when(mockResearchProject.getIntendedDataUseStatement()).thenReturn(INTENDED_DUS);
		when(mockResearchProject.getProjectLead()).thenReturn(PROJECT_LEAD);
		when(mockACTAccessRequirement.getId()).thenReturn(AR_ID);

		when(mockView.getInstitution()).thenReturn(INSTITUTION2);
		when(mockView.getIntendedDataUseStatement()).thenReturn(INTENDED_DUS2);
		when(mockView.getProjectLead()).thenReturn(PROJECT_LEAD2);

		AsyncMockStubber.callSuccessWith(mockResearchProject).when(mockClient).updateResearchProject(any(ResearchProject.class), any(AsyncCallback.class));
	}

	@Test
	public void testSetModalPresenter() {
		verify(mockModalPresenter).setPrimaryButtonText(DisplayConstants.NEXT);
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		boolean isIDUPublic = true;
		when(mockACTAccessRequirement.getIsIDUPublic()).thenReturn(isIDUPublic);
		widget.configure(mockACTAccessRequirement, mockSubject);
		verify(mockView).setIDUPublicNoteVisible(isIDUPublic);
		verify(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));
		verify(mockView).setInstitution(INSTITUTION);
		verify(mockView).setIntendedDataUseStatement(INTENDED_DUS);
		verify(mockView).setProjectLead(PROJECT_LEAD);
	}

	@Test
	public void testConfigureNewResearchProject() {
		AsyncMockStubber.callSuccessWith(mock(ResearchProject.class)).when(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));

		widget.configure(mockACTAccessRequirement, mockSubject);
		verify(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));
		verify(mockView, never()).setInstitution(INSTITUTION);
		verify(mockView, never()).setIntendedDataUseStatement(INTENDED_DUS);
		verify(mockView, never()).setProjectLead(PROJECT_LEAD);
	}

	@Test
	public void testConfigureFailure() {
		boolean isIDUPublic = false;
		when(mockACTAccessRequirement.getIsIDUPublic()).thenReturn(isIDUPublic);
		String error = "an error";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));

		widget.configure(mockACTAccessRequirement, mockSubject);
		verify(mockView).setIDUPublicNoteVisible(isIDUPublic);
		verify(mockClient).getResearchProject(anyLong(), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}

	@Test
	public void testNext() {
		widget.configure(mockACTAccessRequirement, mockSubject);
		widget.onPrimary();
		verify(mockModalPresenter).setLoading(true);
		// verify research project values were updated from the view
		verify(mockClient).updateResearchProject(researchProjectCaptor.capture(), any(AsyncCallback.class));
		ResearchProject rp = researchProjectCaptor.getValue();
		assertEquals(mockResearchProject, rp);
		verify(mockResearchProject).setInstitution(INSTITUTION2);
		verify(mockResearchProject).setIntendedDataUseStatement(INTENDED_DUS2);
		verify(mockResearchProject).setProjectLead(PROJECT_LEAD2);

		verify(mockStep2).configure(mockResearchProject, mockACTAccessRequirement, mockSubject);
		verify(mockModalPresenter).setNextActivePage(mockStep2);
	}

	@Test
	public void testNextInvalidViewValue() {
		when(mockView.getInstitution()).thenReturn(null);
		widget.onPrimary();
		verify(mockModalPresenter, never()).setLoading(true);
		verify(mockClient, never()).updateResearchProject(any(ResearchProject.class), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(anyString());
	}

	@Test
	public void testNextInvalidViewValue2() {
		when(mockView.getProjectLead()).thenReturn("");
		widget.onPrimary();
		verify(mockModalPresenter, never()).setLoading(true);
		verify(mockClient, never()).updateResearchProject(any(ResearchProject.class), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(anyString());
	}

	@Test
	public void testNextFailure() {
		String error = "a save error";
		Exception ex = new Exception(error);
		AsyncMockStubber.callFailureWith(ex).when(mockClient).updateResearchProject(any(ResearchProject.class), any(AsyncCallback.class));

		widget.configure(mockACTAccessRequirement, mockSubject);
		widget.onPrimary();
		verify(mockModalPresenter).setLoading(true);
		verify(mockClient).updateResearchProject(any(ResearchProject.class), any(AsyncCallback.class));
		verify(mockModalPresenter).setErrorMessage(error);
	}
}
