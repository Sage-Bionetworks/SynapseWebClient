package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.UserSessionData;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.DeleteAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ReviewAccessorsButton;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.JiraURLHelper;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class AccessRequirementWidgetTest {
	AccessRequirementWidget widget;
	
	@Mock
	ManagedACTAccessRequirementWidget mockManagedACTAccessRequirementWidget;
	@Mock
	ACTAccessRequirementWidget mockACTAccessRequirementWidget;
	@Mock
	TermsOfUseAccessRequirementWidget mockToUAccessRequirementWidget;
	@Mock
	LockAccessRequirementWidget mockLockAccessRequirementWidget;
	
	@Mock
	DivView mockView;
	@Mock
	DataAccessClientAsync mockDataAccessClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseAlert mockSynAlert;

	@Mock
	ManagedACTAccessRequirement mockManagedACTAccessRequirement;
	public static final long MANAGED_ACT_AR_ID = 222L;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	public static final long ACT_AR_ID = 333L;
	@Mock
	TermsOfUseAccessRequirement mockToUAccessRequirement;
	public static final long TOU_AR_ID = 444L;
	@Mock
	LockAccessRequirement mockLockAccessRequirement;
	public static final long LOCK_AR_ID = 555L;
	@Mock
	AccessRequirement mockAccessRequirement;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		widget = new AccessRequirementWidget(mockGinInjector,
				mockDataAccessClient,
				mockView);
		
		AsyncMockStubber.callSuccessWith(mockManagedACTAccessRequirement).when(mockDataAccessClient).getAccessRequirement(eq(MANAGED_ACT_AR_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockACTAccessRequirement).when(mockDataAccessClient).getAccessRequirement(eq(ACT_AR_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockToUAccessRequirement).when(mockDataAccessClient).getAccessRequirement(eq(TOU_AR_ID), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockLockAccessRequirement).when(mockDataAccessClient).getAccessRequirement(eq(LOCK_AR_ID), any(AsyncCallback.class));
		
		when(mockGinInjector.getManagedACTAccessRequirementWidget()).thenReturn(mockManagedACTAccessRequirementWidget);
		when(mockGinInjector.getTermsOfUseAccessRequirementWidget()).thenReturn(mockToUAccessRequirementWidget);
		when(mockGinInjector.getACTAccessRequirementWidget()).thenReturn(mockACTAccessRequirementWidget);
		when(mockGinInjector.getLockAccessRequirementWidget()).thenReturn(mockLockAccessRequirementWidget);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
	}

	@Test
	public void testConfigureManagedACTAccessRequirement() {
		widget.configure(Long.toString(MANAGED_ACT_AR_ID));
		verify(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		verify(mockManagedACTAccessRequirementWidget).setRequirement(mockManagedACTAccessRequirement);
		verify(mockView).add(mockManagedACTAccessRequirementWidget);
	}
	@Test
	public void testConfigureACTAccessRequirement() {
		widget.configure(Long.toString(ACT_AR_ID));
		verify(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		verify(mockACTAccessRequirementWidget).setRequirement(mockACTAccessRequirement);
		verify(mockView).add(mockACTAccessRequirementWidget);
	}
	@Test
	public void testConfigureToUAccessRequirement() {
		widget.configure(Long.toString(TOU_AR_ID));
		verify(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		verify(mockToUAccessRequirementWidget).setRequirement(mockToUAccessRequirement);
		verify(mockView).add(mockToUAccessRequirementWidget);
	}
	@Test
	public void testConfigureLockAccessRequirement() {
		widget.configure(Long.toString(LOCK_AR_ID));
		verify(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		verify(mockLockAccessRequirementWidget).setRequirement(mockLockAccessRequirement);
		verify(mockView).add(mockLockAccessRequirementWidget);
	}
	
	@Test
	public void testConfigureUnsupportedAccessRequirement() {
		widget.configure(mockAccessRequirement);
		verify(mockSynAlert).handleException(any(Throwable.class));
	}
	
	@Test
	public void testFailureToLoadAccessRequirement() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getAccessRequirement(anyLong(), any(AsyncCallback.class));
		widget.configure("888");
		verify(mockSynAlert).handleException(any(Throwable.class));
	}
	
	
}
