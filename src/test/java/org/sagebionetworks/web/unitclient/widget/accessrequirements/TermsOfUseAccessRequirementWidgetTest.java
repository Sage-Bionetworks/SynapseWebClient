package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;

public class TermsOfUseAccessRequirementWidgetTest {
	TermsOfUseAccessRequirementWidget widget;
	
	@Mock
	TermsOfUseAccessRequirementWidgetView mockView;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	TermsOfUseAccessRequirement mockTermsOfUseAccessRequirement;
	@Mock
	CreateAccessRequirementButton mockCreateAccessRequirementButton;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new TermsOfUseAccessRequirementWidget(mockView, mockAuthController, mockSynapseClient, mockWikiPageWidget, mockSynAlert, mockCreateAccessRequirementButton);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockView).setEditAccessRequirementWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByHistoryVisible(false);
	}

	@Test
	public void testSetRequirementWithContactInfoTerms() {
		String tou = "must do things before access is allowed";
		when(mockTermsOfUseAccessRequirement.getTermsOfUse()).thenReturn(tou);
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
		verify(mockCreateAccessRequirementButton).configure(mockTermsOfUseAccessRequirement);
	}
	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockTermsOfUseAccessRequirement);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
		
	}
}
