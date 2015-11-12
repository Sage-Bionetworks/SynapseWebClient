package org.sagebionetworks.web.unitclient.widget.verification;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidgetView;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;

public class VerificationSubmissionWidgetTest {
	@Mock
	VerificationSubmissionWidgetView mockView; 
	@Mock
	UserProfileClientAsync mockUserProfileClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	MarkdownWidget mockMarkdownWidget;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	FileHandleList mockFileHandleList;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	PromptModalView mockPromptModalView;
	@Mock
	CookieProvider mockCookieProvider;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	
	VerificationSubmissionWidget widget;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		widget = new VerificationSubmissionWidget(mockGinInjector, mockUserProfileClient, mockMarkdownWidget, mockSynapseClient, mockSynapseAlert, mockFileHandleList, mockSynapseJSNIUtils, mockPromptModalView, mockCookieProvider, mockGlobalApplicationState);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testConfigure() {
	
	}

	

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
