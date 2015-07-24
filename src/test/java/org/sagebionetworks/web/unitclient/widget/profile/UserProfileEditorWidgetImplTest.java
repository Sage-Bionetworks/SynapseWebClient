package org.sagebionetworks.web.unitclient.widget.profile;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetImpl;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidgetView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.FileValidator;
import org.sagebionetworks.web.client.widget.upload.ImageFileValidator;

public class UserProfileEditorWidgetImplTest {
	
	UserProfileEditorWidgetView mockView;
	ProfileImageWidget mockImageWidget;
	FileHandleUploadWidget mockfileHandleUploadWidget;
	UserProfileEditorWidgetImpl widget;
	
	UserProfile profile;
	
	@Before
	public void before(){
		mockView = Mockito.mock(UserProfileEditorWidgetView.class);
		mockImageWidget = Mockito.mock(ProfileImageWidget.class);
		mockfileHandleUploadWidget = Mockito.mock(FileHandleUploadWidget.class);
		widget = new UserProfileEditorWidgetImpl(mockView, mockImageWidget, mockfileHandleUploadWidget);
		
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
	public void testConfigure(){
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
		
		//also verify that max image size is set
		ArgumentCaptor<FileValidator> captor = ArgumentCaptor.forClass(FileValidator.class);
		verify(mockfileHandleUploadWidget).setValidation(captor.capture());
		FileValidator validator = captor.getValue();
		assertTrue(validator instanceof ImageFileValidator);
		ImageFileValidator v = (ImageFileValidator)validator;
		assertEquals(UserProfileEditorWidgetImpl.MAX_IMAGE_SIZE, v.getMaxFileSize(), .1);
	}
	
	@Test
	public void testConfigureNoProfileImage(){
		profile.setProfilePicureFileHandleId(null);
		widget.configure(profile);
		verify(mockImageWidget).configure(null);
	}
	
	@Test
	public void testIsValid(){
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("valid");
		assertTrue(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
	}
	
	@Test
	public void testIsValidShortUsername(){
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("12");
		assertFalse(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
		verify(mockView).showUsernameError(UserProfileEditorWidgetImpl.MUST_BE_AT_LEAST_3_CHARACTERS);
	}
	
	@Test
	public void testIsValidUsernameBadChars(){
		widget.configure(profile);
		reset(mockView);
		when(mockView.getUsername()).thenReturn("ABC@");
		assertFalse(widget.isValid());
		verify(mockView).hideLinkError();
		verify(mockView).hideUsernameError();
		verify(mockView).showUsernameError(UserProfileEditorWidgetImpl.CAN_ONLY_INCLUDE);
	}
	
}
