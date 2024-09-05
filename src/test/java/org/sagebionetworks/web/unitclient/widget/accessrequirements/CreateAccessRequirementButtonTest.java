package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.place.shared.Place;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.FeatureFlagConfig;
import org.sagebionetworks.web.client.FeatureFlagKey;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.CreateOrUpdateAccessRequirementWizardProps;
import org.sagebionetworks.web.client.place.AccessRequirementPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateOrUpdateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.LegacyCreateAccessRequirementWizard;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget;

public class CreateAccessRequirementButtonTest {

  CreateAccessRequirementButton widget;

  @Mock
  SingleButtonView mockView;

  @Mock
  IsACTMemberAsyncHandler mockIsACTMemberAsyncHandler;

  @Mock
  CookieProvider mockCookies;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  LegacyCreateAccessRequirementWizard mockLegacyCreateAccessRequirementWizard;

  @Mock
  CreateOrUpdateAccessRequirementWizard mockCreateOrUpdateAccessRequirementWizard;

  @Captor
  ArgumentCaptor<
    CreateOrUpdateAccessRequirementWizardProps.OnComplete
  > createOrUpdateArOnCompleteCaptor;

  @Captor
  ArgumentCaptor<
    CreateOrUpdateAccessRequirementWizardProps.OnCancel
  > createOrUpdateArOnCancelCaptor;

  @Captor
  ArgumentCaptor<CallbackP<Boolean>> callbackPCaptor;

  @Mock
  AccessRequirement mockAccessRequirement;

  @Mock
  RestrictableObjectDescriptor mockSubject;

  @Captor
  ArgumentCaptor<ModalWizardWidget.WizardCallback> wizardCallbackCallback;

  @Mock
  Callback mockRefreshCallback;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  FeatureFlagConfig mockFeatureFlagConfig;

  @Captor
  ArgumentCaptor<Place> placeCaptor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget =
      new CreateAccessRequirementButton(
        mockView,
        mockIsACTMemberAsyncHandler,
        mockCookies,
        mockFeatureFlagConfig,
        mockGinInjector
      );
    when(mockGinInjector.getLegacyCreateAccessRequirementWizard())
      .thenReturn(mockLegacyCreateAccessRequirementWizard);
    when(mockGinInjector.getCreateOrUpdateAccessRequirementWizard())
      .thenReturn(mockCreateOrUpdateAccessRequirementWizard);
    when(mockGinInjector.getGlobalApplicationState())
      .thenReturn(mockGlobalAppState);
    when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setButtonVisible(false);
  }

  @Test
  public void testConfigureWithAR() {
    widget.configure(mockAccessRequirement, mockRefreshCallback);
    verify(mockView)
      .setButtonText(
        CreateAccessRequirementButton.EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT
      );
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
    // invoking with false should hide the button again
    isACTMemberCallback.invoke(false);
    verify(mockView, times(2)).setButtonVisible(false);

    isACTMemberCallback.invoke(true);
    verify(mockView).setButtonVisible(true);

    // configured with an AR, when clicked it should pop up the wizard with the existing AR
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(false);
    widget.onClick();
    verify(mockLegacyCreateAccessRequirementWizard)
      .configure(mockAccessRequirement);
    verify(mockLegacyCreateAccessRequirementWizard)
      .showModal(wizardCallbackCallback.capture());
    wizardCallbackCallback.getValue().onFinished();
    verify(mockRefreshCallback).invoke();
    verify(mockCreateOrUpdateAccessRequirementWizard, never()).setOpen(true);
  }

  @Test
  public void testConfigureWithSubject() {
    widget.configure(mockSubject, mockRefreshCallback);
    verify(mockView)
      .setButtonText(
        CreateAccessRequirementButton.CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT
      );
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    // configured with a subject, when clicked it should pop up the wizard pointing to the new subject
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(false);
    widget.onClick();
    verify(mockLegacyCreateAccessRequirementWizard).configure(mockSubject);
    verify(mockLegacyCreateAccessRequirementWizard)
      .showModal(any(ModalWizardWidget.WizardCallback.class));
    verify(mockCreateOrUpdateAccessRequirementWizard, never()).setOpen(true);
  }

  @Test
  public void testOnCancelRefreshPage() {
    widget.configure(mockAccessRequirement, mockRefreshCallback);
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(false);
    widget.onClick();
    verify(mockLegacyCreateAccessRequirementWizard)
      .configure(mockAccessRequirement);
    verify(mockLegacyCreateAccessRequirementWizard)
      .showModal(wizardCallbackCallback.capture());
    wizardCallbackCallback.getValue().onCanceled();
    verify(mockRefreshCallback).invoke();
    verify(mockCreateOrUpdateAccessRequirementWizard, never()).setOpen(true);
  }

  @Test
  public void testConfigureWithARInExperimentalMode() {
    widget.configure(mockAccessRequirement, mockRefreshCallback);
    verify(mockView)
      .setButtonText(
        CreateAccessRequirementButton.EDIT_ACCESS_REQUIREMENT_BUTTON_TEXT
      );
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    CallbackP<Boolean> isACTMemberCallback = callbackPCaptor.getValue();
    isACTMemberCallback.invoke(true);
    verify(mockView).setButtonVisible(true);

    // feature flag -- use SRC wizard
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(true);
    widget.onClick();

    verify(mockCreateOrUpdateAccessRequirementWizard)
      .configure(
        eq(mockAccessRequirement),
        createOrUpdateArOnCompleteCaptor.capture(),
        createOrUpdateArOnCancelCaptor.capture()
      );
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(true);

    verify(mockLegacyCreateAccessRequirementWizard, never())
      .configure(mockAccessRequirement);
    verify(mockLegacyCreateAccessRequirementWizard, never())
      .showModal(wizardCallbackCallback.capture());

    String arID = "12345";
    createOrUpdateArOnCompleteCaptor.getValue().onComplete(arID);
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(false);
    verify(mockPlaceChanger).goTo(placeCaptor.capture());
    Place actualPlace = placeCaptor.getValue();
    assertTrue(actualPlace instanceof AccessRequirementPlace);
    assertEquals(
      arID,
      ((AccessRequirementPlace) actualPlace).getParam(
          AccessRequirementPlace.AR_ID_PARAM
        )
    );
  }

  @Test
  public void testConfigureWithSubjectInExperimentalMode() {
    widget.configure(mockSubject, mockRefreshCallback);
    verify(mockView)
      .setButtonText(
        CreateAccessRequirementButton.CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT
      );
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    // feature flag -- use SRC wizard
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(true);
    widget.onClick();

    verify(mockCreateOrUpdateAccessRequirementWizard)
      .configure(
        eq(mockSubject),
        createOrUpdateArOnCompleteCaptor.capture(),
        createOrUpdateArOnCancelCaptor.capture()
      );
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(true);

    verify(mockLegacyCreateAccessRequirementWizard, never())
      .configure(mockSubject);
    verify(mockLegacyCreateAccessRequirementWizard, never())
      .showModal(wizardCallbackCallback.capture());

    String arID = "12345";
    createOrUpdateArOnCompleteCaptor.getValue().onComplete(arID);
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(false);
    verify(mockPlaceChanger).goTo(placeCaptor.capture());
    Place actualPlace = placeCaptor.getValue();
    assertTrue(actualPlace instanceof AccessRequirementPlace);
    assertEquals(
      arID,
      ((AccessRequirementPlace) actualPlace).getParam(
          AccessRequirementPlace.AR_ID_PARAM
        )
    );
  }

  @Test
  public void testOnCancelRefreshPageInExperimentalMode() {
    widget.configure(mockAccessRequirement, mockRefreshCallback);
    when(
      mockFeatureFlagConfig.isFeatureEnabled(
        FeatureFlagKey.SRC_BASED_AR_MODAL_WIZARD
      )
    )
      .thenReturn(true);
    widget.onClick();

    // feature flag -- use SRC wizard
    verify(mockCreateOrUpdateAccessRequirementWizard)
      .configure(
        eq(mockAccessRequirement),
        createOrUpdateArOnCompleteCaptor.capture(),
        createOrUpdateArOnCancelCaptor.capture()
      );
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(true);

    createOrUpdateArOnCancelCaptor.getValue().onCancel();
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(false);
    verify(mockRefreshCallback).invoke();
    verify(mockLegacyCreateAccessRequirementWizard, never())
      .configure(mockAccessRequirement);
  }
}
