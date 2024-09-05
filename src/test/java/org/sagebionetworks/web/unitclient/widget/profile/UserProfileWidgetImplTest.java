package org.sagebionetworks.web.unitclient.widget.profile;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.NotificationEmail;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileWidgetView;
import org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class UserProfileWidgetImplTest {

  @Mock
  UserProfileWidgetView mockView;

  @Mock
  ProfileImageWidget mockImageWidget;

  @Mock
  ImageUploadWidget mockfileHandleUploadWidget;

  UserProfileWidgetImpl widget;

  @Mock
  PortalGinInjector mockPortalGinInjector;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  CroppedImageUploadViewImpl mockCroppedImageUploadViewImpl;

  @Mock
  ClientCache mockClientCache;

  @Mock
  AuthenticationController mockAuthController;

  @Mock
  Callback mockCallback;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PopupUtilsView mockPopupUtilsView;

  @Mock
  GlobalApplicationState mockGlobalAppState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  NotificationEmail mockNotificationEmail;

  @Captor
  ArgumentCaptor<Profile> profilePlaceCaptor;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Captor
  ArgumentCaptor<UserProfile> userProfileCaptor;

  UserProfile profile, changes;
  public static final String ORC_ID = "https://orcid";
  public static final String USER_PROFILE_ID = "123";
  public List<String> userEmails;
  public static final String EMAIL1 = "007@uk.supersecret.gov";
  public static final String EMAIL2 = "008@uk.supersecret.gov"; // notification/primary email

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    when(mockPortalGinInjector.getCroppedImageUploadView())
      .thenReturn(mockCroppedImageUploadViewImpl);
    when(mockAuthController.getCurrentUserPrincipalId())
      .thenReturn(USER_PROFILE_ID);
    widget =
      new UserProfileWidgetImpl(
        mockView,
        mockImageWidget,
        mockfileHandleUploadWidget,
        mockJsClient,
        mockClientCache,
        mockAuthController,
        mockPortalGinInjector,
        mockSynAlert,
        mockPopupUtilsView,
        mockGlobalAppState
      );

    profile = new UserProfile();
    profile.setOwnerId(USER_PROFILE_ID);
    profile.setUserName("a-user-name");
    profile.setFirstName("James");
    profile.setLastName("Bond");
    profile.setPosition("Spy");
    profile.setCompany("SI6");
    profile.setEtag("etag");
    profile.setIndustry("Politics");
    profile.setLocation("Britain");
    profile.setUrl("http://spys.r.us");
    profile.setSummary("My live story...");
    profile.setProfilePicureFileHandleId("45678");
    userEmails = new ArrayList<>();
    userEmails.add(EMAIL1);
    userEmails.add(EMAIL2);
    profile.setEmails(userEmails);
    when(mockNotificationEmail.getEmail()).thenReturn(EMAIL2);

    changes = new UserProfile();
    changes.setOwnerId(USER_PROFILE_ID);
    changes.setUserName("a-user-name-2");
    changes.setFirstName("James-2");
    changes.setLastName("Bond-2");
    changes.setPosition("Spy-2");
    changes.setCompany("SI6-2");
    changes.setEtag("etag");
    changes.setIndustry("Politics-2");
    changes.setLocation("Britain-2");
    changes.setUrl("http://spys.r.us.two");
    changes.setSummary("My live story...2");
    changes.setProfilePicureFileHandleId("456782");
    changes.setDisplayName("James-2 Bond-2");

    when(mockGlobalAppState.getPlaceChanger()).thenReturn(mockPlaceChanger);
    AsyncMockStubber
      .callSuccessWith(profile)
      .when(mockJsClient)
      .updateMyUserProfile(any(UserProfile.class), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockNotificationEmail)
      .when(mockJsClient)
      .getNotificationEmail(any(AsyncCallback.class));
  }

  @Test
  public void testConstruction() {
    verify(mockfileHandleUploadWidget).setView(mockCroppedImageUploadViewImpl);
    verify(mockfileHandleUploadWidget).setButtonIcon(IconType.EDIT);
    verify(mockfileHandleUploadWidget).setButtonText("");
    verify(mockfileHandleUploadWidget).setButtonType(ButtonType.DEFAULT);
    verify(mockfileHandleUploadWidget).setButtonSize(ButtonSize.SMALL);
    verify(mockfileHandleUploadWidget).addStyleName("editProfileImageButton");
    verify(mockfileHandleUploadWidget).setVisible(false);
    verify(mockImageWidget).setRemovePictureCommandVisible(false);
    verify(mockView).setSynAlert(mockSynAlert);
  }

  private void verifyConfigure(int nCalls) {
    verify(mockSynAlert, times(nCalls)).clear();
    verify(mockView, times(nCalls)).clearEmails();
    verify(mockView, times(nCalls)).setOwnerId(profile.getOwnerId());
    verify(mockView, times(nCalls)).setFirstName(profile.getFirstName());
    verify(mockView, times(nCalls)).setOrcIdHref(ORC_ID);
    verify(mockView, times(nCalls)).setLastName(profile.getLastName());
    verify(mockView, times(nCalls)).setUsername(profile.getUserName());
    verify(mockView, times(nCalls)).setCurrentPosition(profile.getPosition());
    verify(mockView, times(nCalls)).setCurrentAffiliation(profile.getCompany());
    verify(mockView, times(nCalls)).setIndustry(profile.getIndustry());
    verify(mockView, times(nCalls)).setLocation(profile.getLocation());
    verify(mockView, times(nCalls)).setLink(profile.getUrl());
    verify(mockView, times(nCalls)).setBio(profile.getSummary());
    verify(mockImageWidget, times(nCalls))
      .configure(profile.getOwnerId(), profile.getProfilePicureFileHandleId());
  }

  @Test
  public void testConfigure() {
    widget.configure(profile, ORC_ID, mockCallback);

    verifyConfigure(1);
    verify(mockView).setEmails(userEmails, EMAIL2);
    verify(mockView).setEmailsVisible(true);
    verify(mockView).setCanEdit(true);
  }

  @Test
  public void testConfigureNoEmail() {
    // should not cause an error
    profile.setEmails(null);

    widget.configure(profile, ORC_ID, mockCallback);
  }

  @Test
  public void testConfigureNotOwner() {
    when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(null); //anonymous
    profile.setEmails(null);
    widget.configure(profile, ORC_ID, mockCallback);

    verifyConfigure(1);
    verify(mockView, never()).setEmails(anyList(), anyString());
    verify(mockView).setEmailsVisible(false);
    verify(mockView).setCanEdit(false);
  }

  @Test
  public void testConfigureACT() {
    // in this case, the current user ID does not match the profile user ID, but the email list is returned by the service.
    when(mockAuthController.getCurrentUserPrincipalId())
      .thenReturn("100200300400");
    widget.configure(profile, ORC_ID, mockCallback);

    verifyConfigure(1);
    verify(mockView).setEmails(userEmails, null);
    verify(mockView).setEmailsVisible(true);
    verify(mockView).setCanEdit(false);
  }

  @Test
  public void testConfigureNoProfileImage() {
    profile.setProfilePicureFileHandleId(null);
    widget.configure(profile, ORC_ID, mockCallback);
    verify(mockImageWidget).configure(profile.getOwnerId(), null);
  }
}
