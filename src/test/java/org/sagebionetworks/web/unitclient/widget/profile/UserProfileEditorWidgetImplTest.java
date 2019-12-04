package org.sagebionetworks.web.unitclient.widget.profile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetView;
import org.sagebionetworks.web.client.widget.upload.CroppedImageUploadViewImpl;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;

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
	CroppedImageUploadViewImpl mockCroppedImageUploadViewImpl;
	@Mock
	Callback mockCallback;
	UserProfile profile;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockPortalGinInjector.getCroppedImageUploadView()).thenReturn(mockCroppedImageUploadViewImpl);
		widget = new UserProfileEditorWidgetImpl(mockView, mockImageWidget, mockfileHandleUploadWidget, mockPortalGinInjector);

		profile = new UserProfile();
		profile.setOwnerId("123");
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
	}

	@Test
	public void testConstruction() {
		verify(mockfileHandleUploadWidget).setView(mockCroppedImageUploadViewImpl);
	}

	@Test
	public void testConfigure() {
		widget.configure(profile);
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
		verify(mockView).setFirstName(profile.getFirstName());
		verify(mockView).setLastName(profile.getLastName());
		verify(mockView).setUsername(profile.getUserName());
		verify(mockView).setCurrentPosition(profile.getPosition());
		verify(mockView).setCurrentAffiliation(profile.getCompany());
		verify(mockView).setIndustry(profile.getIndustry());
		verify(mockView).setLocation(profile.getLocation());
		verify(mockView).setLink(profile.getUrl());
		verify(mockView).setBio(profile.getSummary());
		verify(mockImageWidget).configure(profile.getProfilePicureFileHandleId());
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
		widget.configure(profile);
		verify(mockImageWidget).configure(null);
	}

	@Test
	public void testIsValid() {
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("valid");
		assertTrue(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
	}

	@Test
	public void testIsValidShortUsername() {
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("12");
		assertFalse(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
		verify(mockView).showUsernameError(UserProfileEditorWidgetImpl.MUST_BE_AT_LEAST_3_CHARACTERS);
	}

	@Test
	public void testIsValidUsernameBadChars() {
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("ABC@");
		assertFalse(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
		verify(mockView).showUsernameError(UserProfileEditorWidgetImpl.CAN_ONLY_INCLUDE);
	}

}
