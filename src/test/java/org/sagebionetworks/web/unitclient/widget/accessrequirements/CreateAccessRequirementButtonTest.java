package org.sagebionetworks.web.unitclient.widget.accessrequirements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
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
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;

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
    isACTMemberCallback.invoke(true);
    verify(mockView).setButtonVisible(true);

    widget.onClick();

    verify(mockCreateOrUpdateAccessRequirementWizard)
      .configure(
        eq(mockAccessRequirement),
        createOrUpdateArOnCompleteCaptor.capture(),
        createOrUpdateArOnCancelCaptor.capture()
      );
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(true);

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
  public void testConfigureWithSubject() {
    widget.configure(mockSubject, mockRefreshCallback);
    verify(mockView)
      .setButtonText(
        CreateAccessRequirementButton.CREATE_NEW_ACCESS_REQUIREMENT_BUTTON_TEXT
      );
    verify(mockIsACTMemberAsyncHandler)
      .isACTActionAvailable(callbackPCaptor.capture());

    widget.onClick();

    verify(mockCreateOrUpdateAccessRequirementWizard)
      .configure(
        eq(mockSubject),
        createOrUpdateArOnCompleteCaptor.capture(),
        createOrUpdateArOnCancelCaptor.capture()
      );
    verify(mockCreateOrUpdateAccessRequirementWizard).setOpen(true);

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
  public void testOnCancelRefreshPage() {
    widget.configure(mockAccessRequirement, mockRefreshCallback);
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
  }
}
