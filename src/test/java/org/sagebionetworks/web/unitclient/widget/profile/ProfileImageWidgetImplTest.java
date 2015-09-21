package org.sagebionetworks.web.unitclient.widget.profile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.profile.ProfileImageView;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidgetImpl;

public class ProfileImageWidgetImplTest {
	
	ProfileImageView mockView;
	SynapseJSNIUtils mockJniUtils;
	ProfileImageWidgetImpl widget;
	Callback callback;
	String baseUrl;
	
	@Before
	public void before(){
		mockView = Mockito.mock(ProfileImageView.class);
		mockJniUtils = Mockito.mock(SynapseJSNIUtils.class);
		callback = Mockito.mock(Callback.class);
		baseUrl = "baseUrl";
		when(mockJniUtils.getBaseProfileAttachmentUrl()).thenReturn(baseUrl);
		widget = new ProfileImageWidgetImpl(mockView, mockJniUtils);
		widget.setRemovePictureCallback(callback);
	}
	
	@Test
	public void testSetup() {
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testOnRemovePicture() {
		widget.onRemovePicture();
		verify(callback).invoke();
	}
	
	@Test
	public void testConfigureFileHandle(){
		widget.configure("123");
		verify(mockView).setImageUrl("baseUrl?imageId=123&userId=null&preview=false&applied=false");
		verify(mockView).setRemovePictureButtonVisible(true);
	}
	
	@Test
	public void testConfigureFileHandleNull(){
		widget.configure(null);
		verify(mockView).showDefault();
		verify(mockView).setRemovePictureButtonVisible(false);
	}
	
	@Test
	public void testConfigureUserIdFileHandle(){
		String userId = "007";
		String imageId = "444";
		widget.configure(userId, imageId);
		verify(mockView).setImageUrl("baseUrl?imageId=444&userId=007&preview=true&applied=true");
		verify(mockView).setRemovePictureButtonVisible(true);
	}

	@Test
	public void testConfigureUserIdFileHandleNull(){
		String userId = "007";
		String imageId = null;
		widget.configure(userId, imageId);
		verify(mockView).showDefault();
		verify(mockView).setRemovePictureButtonVisible(false);
	}
}
