package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.LockAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

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
  SynapseJavascriptClient mockJsClient;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  Callback mockRefreshCallback;

  @Mock
  ManagedACTAccessRequirement mockManagedACTAccessRequirement;

  public static final String MANAGED_ACT_AR_ID = "222";

  @Mock
  ACTAccessRequirement mockACTAccessRequirement;

  public static final String ACT_AR_ID = "333";

  @Mock
  TermsOfUseAccessRequirement mockToUAccessRequirement;

  public static final String TOU_AR_ID = "444";

  @Mock
  LockAccessRequirement mockLockAccessRequirement;

  public static final String LOCK_AR_ID = "555";

  @Mock
  AccessRequirement mockAccessRequirement;

  @Mock
  RestrictableObjectDescriptor mockSubject;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    widget =
      new AccessRequirementWidget(mockGinInjector, mockJsClient, mockView);

    AsyncMockStubber
      .callSuccessWith(mockManagedACTAccessRequirement)
      .when(mockJsClient)
      .getAccessRequirement(eq(MANAGED_ACT_AR_ID), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockACTAccessRequirement)
      .when(mockJsClient)
      .getAccessRequirement(eq(ACT_AR_ID), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockToUAccessRequirement)
      .when(mockJsClient)
      .getAccessRequirement(eq(TOU_AR_ID), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockLockAccessRequirement)
      .when(mockJsClient)
      .getAccessRequirement(eq(LOCK_AR_ID), any(AsyncCallback.class));

    when(mockGinInjector.getManagedACTAccessRequirementWidget())
      .thenReturn(mockManagedACTAccessRequirementWidget);
    when(mockGinInjector.getTermsOfUseAccessRequirementWidget())
      .thenReturn(mockToUAccessRequirementWidget);
    when(mockGinInjector.getACTAccessRequirementWidget())
      .thenReturn(mockACTAccessRequirementWidget);
    when(mockGinInjector.getLockAccessRequirementWidget())
      .thenReturn(mockLockAccessRequirementWidget);
    when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
  }

  @Test
  public void testConfigureManagedACTAccessRequirement() {
    widget.configure(MANAGED_ACT_AR_ID, mockSubject);
    verify(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    verify(mockManagedACTAccessRequirementWidget)
      .setRequirement(eq(mockManagedACTAccessRequirement), any(Callback.class));
    verify(mockManagedACTAccessRequirementWidget).setTargetSubject(mockSubject);
    verify(mockView).add(mockManagedACTAccessRequirementWidget);
  }

  @Test
  public void testConfigureACTAccessRequirement() {
    widget.configure(ACT_AR_ID, mockSubject);
    verify(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    verify(mockACTAccessRequirementWidget)
      .setRequirement(eq(mockACTAccessRequirement), any(Callback.class));
    verify(mockView).add(mockACTAccessRequirementWidget);
  }

  @Test
  public void testConfigureToUAccessRequirement() {
    widget.configure(TOU_AR_ID, mockSubject);
    verify(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    verify(mockToUAccessRequirementWidget)
      .setRequirement(eq(mockToUAccessRequirement), any(Callback.class));
    verify(mockView).add(mockToUAccessRequirementWidget);
  }

  @Test
  public void testConfigureLockAccessRequirement() {
    widget.configure(LOCK_AR_ID, mockSubject);
    verify(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    verify(mockLockAccessRequirementWidget)
      .setRequirement(eq(mockLockAccessRequirement), any(Callback.class));
    verify(mockView).add(mockLockAccessRequirementWidget);
  }

  @Test
  public void testConfigureUnsupportedAccessRequirement() {
    widget.configure(mockAccessRequirement, mockSubject, mockRefreshCallback);
    verify(mockSynAlert).handleException(any(Throwable.class));
  }

  @Test
  public void testFailureToLoadAccessRequirement() {
    Exception ex = new Exception();
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockJsClient)
      .getAccessRequirement(anyString(), any(AsyncCallback.class));
    widget.configure("888", mockSubject);
    verify(mockSynAlert).handleException(any(Throwable.class));
  }
}
