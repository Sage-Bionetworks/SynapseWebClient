package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.ResearchProject;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateACTAccessRequirementStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectStep1;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateACTAccessRequirementStep2Test {
	
	CreateACTAccessRequirementStep2 widget;
	@Mock
	ModalPresenter mockModalPresenter;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new CreateACTAccessRequirementStep2();
		widget.setModalPresenter(mockModalPresenter);
	}
}
