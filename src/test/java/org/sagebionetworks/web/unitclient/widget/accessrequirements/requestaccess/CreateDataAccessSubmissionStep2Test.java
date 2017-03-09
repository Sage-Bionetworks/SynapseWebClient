package org.sagebionetworks.web.unitclient.widget.accessrequirements.requestaccess;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessSubmissionWizardStep2View;
import org.sagebionetworks.web.client.widget.entity.act.UserBadgeList;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;

public class CreateDataAccessSubmissionStep2Test {
	@Mock
	CreateDataAccessSubmissionWizardStep2View mockView;
	@Mock
	DataAccessClientAsync mockClient;
	@Mock
	FileHandleWidget mockTemplateFileRenderer;
	@Mock
	FileHandleUploadWidget mockDucUploader;
	@Mock
	FileHandleUploadWidget mockIrbUploader;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	UserBadgeList mockAccessorsList;
	@Mock
	SynapseSuggestBox mockPeopleSuggestBox;
	@Mock
	UserGroupSuggestionProvider mockProvider;
	@Mock
	FileHandleList mockOtherDocuments;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
