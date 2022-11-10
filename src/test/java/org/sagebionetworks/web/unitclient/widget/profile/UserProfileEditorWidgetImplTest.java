package org.sagebionetworks.web.unitclient.widget.profile;

import static org.junit.Assert.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
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
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetView;
import org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class UserProfileEditorWidgetImplTest {

  @Mock
  UserProfileEditorWidgetView mockView;

  @Mock
  ProfileImageWidget mockImageWidget;

  @Mock
  ImageUploadWidget mockfileHandleUploadWidget;

  UserProfileEditorWidgetImpl widget;

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
      new UserProfileEditorWidgetImpl(
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

    widget.setNewFileHandle(changes.getProfilePicureFileHandleId());
    when(mockView.getUsername()).thenReturn(changes.getUserName());
    when(mockView.getFirstName()).thenReturn(changes.getFirstName());
    when(mockView.getLastName()).thenReturn(changes.getLastName());
    when(mockView.getCurrentPosition()).thenReturn(changes.getPosition());
    when(mockView.getCurrentAffiliation()).thenReturn(changes.getCompany());
    when(mockView.getIndustry()).thenReturn(changes.getIndustry());
    when(mockView.getLocation()).thenReturn(changes.getLocation());
    when(mockView.getLink()).thenReturn(changes.getUrl());
    when(mockView.getBio()).thenReturn(changes.getSummary());

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

  private void verifyIsEditingMode(boolean isEditing, int nCalls) {
    verify(mockGlobalAppState, times(nCalls)).setIsEditing(isEditing);
    verify(mockView, times(nCalls)).setEditMode(isEditing);
  }

  private void verifyConfigure(int nCalls) {
    verify(mockSynAlert, times(nCalls)).clear();
    verify(mockView, times(nCalls)).hideLinkError();
    verify(mockView, times(nCalls)).hideUsernameError();
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
    verify(mockView, times(nCalls)).setEmail(EMAIL2);
    verify(mockImageWidget, times(nCalls))
      .configure(profile.getOwnerId(), profile.getProfilePicureFileHandleId());
    verifyIsEditingMode(false, nCalls);
  }

  @Test
  public void testConfigure() {
    widget.configure(profile, ORC_ID, mockCallback);

    verifyConfigure(1);
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
    widget.configure(profile, ORC_ID, mockCallback);

    verifyConfigure(1);
    verify(mockView).setCanEdit(false);
  }

  @Test
  public void testOnCancel() {
    widget.configure(profile, ORC_ID, mockCallback);
    widget.onCancel();

    verifyConfigure(2);
  }

  @Test
  public void testOnSave() {
    widget.configure(profile, null, mockCallback);
    widget.onSave();

    verify(mockSynAlert, times(2)).clear();
    verify(mockJsClient)
      .updateMyUserProfile(
        userProfileCaptor.capture(),
        any(AsyncCallback.class)
      );

    //verify profile was updated to new values
    assertEquals(
      changes.getOwnerId(),
      userProfileCaptor.getValue().getOwnerId()
    );
    assertEquals(
      changes.getFirstName(),
      userProfileCaptor.getValue().getFirstName()
    );
    assertEquals(
      changes.getLastName(),
      userProfileCaptor.getValue().getLastName()
    );
    assertEquals(
      changes.getUserName(),
      userProfileCaptor.getValue().getUserName()
    );

    verify(mockCallback).invoke();
    verify(mockClientCache)
      .remove(profile.getOwnerId() + WebConstants.USER_PROFILE_SUFFIX);
    verify(mockAuthController).updateCachedProfile(profile);
    verify(mockPlaceChanger, never()).goTo(any(Profile.class));
  }

  @Test
  public void testOnSaveFailure() {
    Exception ex = new Exception("An error");
    AsyncMockStubber
      .callFailureWith(ex)
      .when(mockJsClient)
      .updateMyUserProfile(any(UserProfile.class), any(AsyncCallback.class));

    widget.configure(profile, ORC_ID, mockCallback);
    widget.onSave();

    verify(mockSynAlert, times(2)).clear();
    verify(mockJsClient)
      .updateMyUserProfile(
        userProfileCaptor.capture(),
        any(AsyncCallback.class)
      );
    verify(mockSynAlert).handleException(ex);
    verify(mockView).resetSaveButtonState();
  }

  @Test
  public void testSetNewFileHandle() {
    String newFileHandle = "293";
    widget.setUploadingCompleteCallback(mockCallback);
    verify(mockCallback, never()).invoke();

    widget.setNewFileHandle(newFileHandle);

    verify(mockImageWidget).configure(newFileHandle);
    verify(mockCallback).invoke();
  }

  @Test
  public void testSetUploadingCallback() {
    widget.setUploadingCallback(mockCallback);
    verify(mockfileHandleUploadWidget).setUploadingCallback(mockCallback);
  }

  @Test
  public void testConfigureNoProfileImage() {
    profile.setProfilePicureFileHandleId(null);
    widget.configure(profile, ORC_ID, mockCallback);
    verify(mockImageWidget).configure(profile.getOwnerId(), null);
  }

  @Test
  public void testIsValid() {
    widget.configure(profile, ORC_ID, mockCallback);
    reset(mockView);
    when(mockView.getUsername()).thenReturn("valid");
    assertTrue(widget.isValid());
    verify(mockView).hideLinkError();
    verify(mockView).hideUsernameError();
  }

  @Test
  public void testIsValidShortUsername() {
    widget.configure(profile, ORC_ID, mockCallback);
    reset(mockView);
    when(mockView.getUsername()).thenReturn("12");
    assertFalse(widget.isValid());
    verify(mockView).hideLinkError();
    verify(mockView).hideUsernameError();
    verify(mockView)
      .showUsernameError(
        UserProfileEditorWidgetImpl.MUST_BE_AT_LEAST_3_CHARACTERS
      );
  }

  @Test
  public void testIsValidUsernameBadChars() {
    widget.configure(profile, ORC_ID, mockCallback);
    reset(mockView);
    when(mockView.getUsername()).thenReturn("ABC@");
    assertFalse(widget.isValid());
    verify(mockView).hideLinkError();
    verify(mockView).hideUsernameError();
    verify(mockView)
      .showUsernameError(UserProfileEditorWidgetImpl.CAN_ONLY_INCLUDE);
  }
}
