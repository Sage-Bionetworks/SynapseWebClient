package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.TwoFactorAuthPlace;
import org.sagebionetworks.web.client.presenter.TwoFactorAuthPresenter;
import org.sagebionetworks.web.client.view.TwoFactorAuthView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

@RunWith(MockitoJUnitRunner.class)
public class TwoFactorAuthPresenterTest {

  TwoFactorAuthPresenter presenter;

  @Mock
  TwoFactorAuthView mockView;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Captor
  ArgumentCaptor<TwoFactorAuthPlace> twoFactorAuthPlaceCaptor;

  @Captor
  ArgumentCaptor<Profile> profilePlaceCaptor;

  @Mock
  SynapseAlert mockSynAlert;

  @Before
  public void setup() throws JSONObjectAdapterException {
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    presenter =
      new TwoFactorAuthPresenter(
        mockView,
        mockGlobalApplicationState,
        mockSynAlert
      );
    verify(mockView).setPresenter(presenter);
    verify(mockView).setSynAlert(mockSynAlert);
  }

  @Test
  public void testConfigureBeginEnrollment() {
    TwoFactorAuthPlace beginEnrollmentPlace = new TwoFactorAuthPlace(
      TwoFactorAuthPlace.BEGIN_ENROLLMENT
    );

    presenter.configureView(beginEnrollmentPlace);

    verify(mockView).showTwoFactorEnrollmentForm();
  }

  @Test
  public void testConfigureCreateRecoveryCodes() {
    TwoFactorAuthPlace createRecoveryCodesPlace = new TwoFactorAuthPlace(
      TwoFactorAuthPlace.CREATE_RECOVERY_CODES
    );

    presenter.configureView(createRecoveryCodesPlace);

    verify(mockView).showGenerateRecoveryCodes(false);
  }

  @Test
  public void testConfigureReplaceRecoveryCodes() {
    TwoFactorAuthPlace replaceRecoveryCodesPlace = new TwoFactorAuthPlace(
      TwoFactorAuthPlace.REPLACE_RECOVERY_CODES
    );

    presenter.configureView(replaceRecoveryCodesPlace);

    verify(mockView).showGenerateRecoveryCodes(true);
  }

  @Test
  public void testOnTwoFactorEnrollmentComplete() {
    presenter.onTwoFactorEnrollmentComplete();

    verify(mockPlaceChanger).goTo(twoFactorAuthPlaceCaptor.capture());

    assertEquals(
      twoFactorAuthPlaceCaptor.getValue().toToken(),
      TwoFactorAuthPlace.CREATE_RECOVERY_CODES
    );
  }

  @Test
  public void testOnGenerateRecoveryCodesComplete() {
    presenter.onGenerateRecoveryCodesComplete();

    verify(mockPlaceChanger).goTo(profilePlaceCaptor.capture());
    assertEquals(
      profilePlaceCaptor.getValue().getUserId(),
      Profile.VIEW_PROFILE_TOKEN
    );
    assertEquals(
      profilePlaceCaptor.getValue().getArea(),
      Synapse.ProfileArea.SETTINGS
    );
  }
}
