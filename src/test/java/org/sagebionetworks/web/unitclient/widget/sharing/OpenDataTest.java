package org.sagebionetworks.web.unitclient.widget.sharing;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.widget.sharing.OpenData;
import org.sagebionetworks.web.client.widget.sharing.OpenDataView;

@RunWith(MockitoJUnitRunner.class)
public class OpenDataTest {
	OpenData openData;
	@Mock
	OpenDataView mockView;
	
	boolean isOpenData, canChangePermissions, isPublicRead;
	@Before
	public void setUp(){
		openData = new OpenData(mockView);
	}

	@Test
	public void testIsOpenDataAndCanChangePermissionsAndIsPublicRead() {
		isOpenData = true;
		canChangePermissions = true;
		isPublicRead = true;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).setIsPublicAndOpenAndAdmin();
	}
	
	@Test
	public void testIsOpenDataAndIsPublicRead() {
		isOpenData = true;
		canChangePermissions = false;
		isPublicRead = true;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		// This should be reflected in the sharing grid, where the public group is rendered as Can Download.
		// But no UI here
		verify(mockView).reset();
		verifyNoMoreInteractions(mockView);
	}
	
	@Test
	public void testCanChangePermissionsAndIsPublicRead() {
		isOpenData = false;
		canChangePermissions = true;
		isPublicRead = true;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).reset();
		verify(mockView).setIsPublicAndAdmin();
	}
	
	@Test
	public void testIsPublicRead() {
		isOpenData = false;
		canChangePermissions = false;
		isPublicRead = true;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).reset();
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testIsOpenDataAndCanChangePermissions() {
		isOpenData = true;
		canChangePermissions = true;
		isPublicRead = false;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).reset();
		verify(mockView).setIsPrivateAndOpenAndAdmin();
	}

	@Test
	public void testIsOpenData() {
		isOpenData = true;
		canChangePermissions = false;
		isPublicRead = false;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).reset();
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testCanChangePermissions() {
		isOpenData = false;
		canChangePermissions = true;
		isPublicRead = false;

		openData.configure(isOpenData, canChangePermissions, isPublicRead);

		verify(mockView).reset();
		verifyNoMoreInteractions(mockView);
	}
}
