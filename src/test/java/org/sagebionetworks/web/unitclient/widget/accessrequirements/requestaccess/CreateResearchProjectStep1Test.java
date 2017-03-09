package org.sagebionetworks.web.unitclient.widget.accessrequirements.requestaccess;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionStep2;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateResearchProjectWizardStep1View;

public class CreateResearchProjectStep1Test {
	
	@Mock
	CreateResearchProjectWizardStep1View view,
	DataAccessClientAsync client, 
	CreateDataAccessSubmissionStep2 step2
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
