package org.sagebionetworks.web.unitclient.widget.entity.file.downloadlist;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRow;

@RunWith(MockitoJUnitRunner.class)
public class FileHandleAssociationRowTest {
	FileHandleAssociationRow widget;
	@Mock
	FileHandleAsyncHandler mockFhaAsyncHandler;
	@Mock
	UserProfileAsyncHandler mockUserProfileAsyncHandler;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	EntityHeaderAsyncHandler mockEntityHeaderAsyncHandler;
	@Mock
	FileHandleAssociation mockFha;
	@Mock
	CallbackP<FileHandleAssociation> mockOnDeleteCallback;
	@Mock
	Callback mockAccessRestrictionDetectedCallback;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	GWTWrapper mockGwt;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
