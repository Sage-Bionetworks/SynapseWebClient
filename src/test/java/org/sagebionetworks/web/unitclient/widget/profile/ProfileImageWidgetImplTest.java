package org.sagebionetworks.web.unitclient.widget.profile;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.profile.ProfileImageView;
import org.sagebionetworks.web.client.widget.profile.ProfileImageWidgetImpl;

public class ProfileImageWidgetImplTest {
	@Mock
	ProfileImageView mockView;
	@Mock
	SynapseJSNIUtils mockJniUtils;
	@Mock
	Callback callback;

	ProfileImageWidgetImpl widget;
	public static final String RAW_FILE_HANDLE_URL = "http://raw.file.handle/";
	public static final String FILE_HANDLE_ASSOCIATION_URL = "http://file.handle.association/";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(mockJniUtils.getFileHandleAssociationUrl(anyString(), any(FileHandleAssociateType.class), anyString())).thenReturn(FILE_HANDLE_ASSOCIATION_URL);
		when(mockJniUtils.getRawFileHandleUrl(anyString())).thenReturn(RAW_FILE_HANDLE_URL);

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
	public void testConfigureFileHandle() {
		widget.configure("123");
		verify(mockView).setImageUrl(RAW_FILE_HANDLE_URL);
		verify(mockView).setRemovePictureButtonVisible(true);
	}

	@Test
	public void testConfigureFileHandleNull() {
		widget.configure(null);
		verify(mockView).showDefault();
		verify(mockView).setRemovePictureButtonVisible(false);
	}

	@Test
	public void testConfigureUserIdFileHandle() {
		String userId = "007";
		String imageId = "444";
		widget.configure(userId, imageId);
		verify(mockView).setImageUrl(FILE_HANDLE_ASSOCIATION_URL);
		verify(mockView).setRemovePictureButtonVisible(true);
	}

	@Test
	public void testConfigureUserIdFileHandleNull() {
		String userId = "007";
		String imageId = null;
		widget.configure(userId, imageId);
		verify(mockView).showDefault();
		verify(mockView).setRemovePictureButtonVisible(false);
	}
}
