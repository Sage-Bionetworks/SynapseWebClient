package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.EntitySchemaCache;
import org.sagebionetworks.web.client.EntityTypeProvider;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.entity.AccessRequirementDialog;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidget;
import org.sagebionetworks.web.client.widget.entity.RestrictionWidgetView;

public class RestrictionWidgetTest {

	SynapseClientAsync mockSynapseClient;
	AuthenticationController mockAuthenticationController;
	GlobalApplicationState mockGlobalApplicationState;
	RestrictionWidgetView mockView;
	EntitySchemaCache mockSchemaCache;
	JSONObjectAdapter jsonObjectAdapter;
	EntityTypeProvider mockEntityTypeProvider;
	IconsImageBundle mockIconsImageBundle;
	JiraURLHelper mockJiraURLHelper;
	AccessRequirementDialog mockAccessRequirementDialog;
	RestrictionWidget widget;
	Versionable vb;
	String entityId = "syn123";
	EntityBundle bundle;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	List<String> emailAddresses;
	IconsImageBundle mockIcons;
	Long testAccessRequirementId = 92837L;
	@Before
	public void before() throws JSONObjectAdapterException {
		mockAuthenticationController = mock(AuthenticationController.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockIcons = mock(IconsImageBundle.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockView = mock(RestrictionWidgetView.class);
		mockSchemaCache = mock(EntitySchemaCache.class);
		jsonObjectAdapter = new JSONObjectAdapterImpl();
		mockEntityTypeProvider = mock(EntityTypeProvider.class);
		mockIconsImageBundle = mock(IconsImageBundle.class);
		mockJiraURLHelper = mock(JiraURLHelper.class);

		UserSessionData usd = new UserSessionData();
		emailAddresses = new ArrayList<String>();
		emailAddresses.add("test@test.com");
		UserProfile up = new UserProfile();
		up.setOwnerId("101");
		up.setEmails(emailAddresses);
		usd.setProfile(up);
		
		when(mockAuthenticationController.getCurrentUserSessionData()).thenReturn(usd);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		mockAccessRequirementDialog = mock(AccessRequirementDialog.class);

		widget = new RestrictionWidget(mockView, mockSynapseClient, mockAuthenticationController, mockGlobalApplicationState, mockJiraURLHelper, mockAccessRequirementDialog);

		vb = new Data();
		vb.setId(entityId);
		vb.setVersionNumber(new Long(1));
		vb.setVersionLabel("");
		vb.setVersionComment("");
		bundle = mock(EntityBundle.class, RETURNS_DEEP_STUBS);
		when(bundle.getEntity()).thenReturn(vb);

		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		TermsOfUseAccessRequirement accessRequirement = new TermsOfUseAccessRequirement();
		accessRequirement.setId(testAccessRequirementId);
		accessRequirement.setTermsOfUse("terms of use");
		accessRequirements.add(accessRequirement);
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetAccessRequirements()).thenReturn(accessRequirements);
				
		widget.setEntityBundle(bundle);
		widget.resetAccessRequirementCount();
	}
	
	@Test
	public void testGetJiraFlagUrl() {
		String flagURLString = "flagURLString";
		when(mockJiraURLHelper.createFlagIssue(any(String.class),any(String.class),any(String.class))).thenReturn(flagURLString);
		assertEquals(flagURLString, widget.getJiraFlagUrl());
	}
	
	@Test
	public void testGetRestrictionLevel() {
		assertEquals(RESTRICTION_LEVEL.RESTRICTED, widget.getRestrictionLevel());
	}
	
	@Test
	public void testShowVerifySensitiveDataDialog() {
		//set up so that it has no requirements
		List<AccessRequirement> accessRequirements = new ArrayList<AccessRequirement>();
		when(bundle.getAccessRequirements()).thenReturn(accessRequirements);
		when(bundle.getUnmetAccessRequirements()).thenReturn(accessRequirements);
		widget.setEntityBundle(bundle);
		widget.resetAccessRequirementCount();
		//show nothing if empty and no admin privs (should not get into this state)
		widget.showNextAccessRequirement(false);
		verify(mockAccessRequirementDialog, never()).show();
		verify(mockView, never()).showVerifyDataSensitiveDialog();

		//should show verification of sensitive data dialog if we have admin privileges
		widget.showNextAccessRequirement(true);
		verify(mockAccessRequirementDialog, never()).show();
		verify(mockView).showVerifyDataSensitiveDialog();
	}
}
