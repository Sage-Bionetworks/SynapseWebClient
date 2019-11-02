package org.sagebionetworks.web.unitclient.widget.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.profile.UserProfileEditorWidget;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalView;
import org.sagebionetworks.web.client.widget.profile.UserProfileModalWidgetImpl;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UserProfileModalWidgetImplTest {

	UserProfileModalView mockView;
	UserProfileEditorWidget mockEditorWidget;
	SynapseClientAsync mockSynapse;
	Callback mockCallback;
	UserProfileModalWidgetImpl widget;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	ClientCache mockClientCache;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	UserProfile profile;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(UserProfileModalView.class);
		mockEditorWidget = Mockito.mock(UserProfileEditorWidget.class);
		mockSynapse = Mockito.mock(SynapseClientAsync.class);
		mockCallback = Mockito.mock(Callback.class);
		widget = new UserProfileModalWidgetImpl(mockView, mockEditorWidget, mockSynapse, mockSynapseJavascriptClient, mockAuthController, mockClientCache);

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

		AsyncMockStubber.callSuccessWith(profile).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testShowEditProfile() {
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		verify(mockView).showModal();
		verify(mockView).setLoading(true);
		verify(mockView).hideError();
		verify(mockView).setProcessing(false);
		verify(mockEditorWidget).configure(profile);
		verify(mockView).setLoading(false);
	}

	@Test
	public void testShowEditProfileError() {
		String error = "An error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapseJavascriptClient).getUserProfile(anyString(), any(AsyncCallback.class));
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		verify(mockView).showModal();
		verify(mockView).setLoading(true);
		verify(mockView).hideError();
		verify(mockView).setProcessing(false);
		verify(mockEditorWidget, never()).configure(any(UserProfile.class));
		verify(mockView).setLoading(false);
		verify(mockView).showError(error);
	}


	@Test
	public void testOnSaveNotValid() {
		when(mockEditorWidget.isValid()).thenReturn(false);
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		reset(mockView);
		widget.onSave();
		verify(mockView).showError(UserProfileModalWidgetImpl.SEE_ERRORS_ABOVE);
		verify(mockView, never()).hideError();
		verify(mockCallback, never()).invoke();
		verify(mockView, never()).hideModal();
	}

	@Test
	public void testOnSave() {
		when(mockEditorWidget.isValid()).thenReturn(true);
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		reset(mockView);
		AsyncMockStubber.callSuccessWith(null).when(mockSynapse).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		widget.onSave();
		verify(mockView, never()).showError(anyString());
		verify(mockView).hideError();
		verify(mockView).setProcessing(true);
		verify(mockCallback).invoke();
		verify(mockView).hideModal();
		verify(mockClientCache).remove(profile.getOwnerId() + WebConstants.USER_PROFILE_SUFFIX);
		verify(mockAuthController).updateCachedProfile(profile);
	}

	@Test
	public void testOnSaveFailed() {
		when(mockEditorWidget.isValid()).thenReturn(true);
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		reset(mockView);
		String error = "An error";
		AsyncMockStubber.callFailureWith(new Throwable(error)).when(mockSynapse).updateUserProfile(any(UserProfile.class), any(AsyncCallback.class));
		widget.onSave();
		verify(mockView).hideError();
		verify(mockView).setProcessing(true);
		verify(mockView).setProcessing(false);
		verify(mockCallback, never()).invoke();
		verify(mockView, never()).hideModal();
		verify(mockView).showError(error);
	}

	@Test
	public void testUpdateProfileFromEditor() {
		when(mockEditorWidget.isValid()).thenReturn(true);
		String userId = "123";
		widget.showEditProfile(userId, mockCallback);
		reset(mockView);
		UserProfile changes = new UserProfile();
		changes.setOwnerId("123");
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
		when(mockEditorWidget.getImageId()).thenReturn(changes.getProfilePicureFileHandleId());
		when(mockEditorWidget.getUsername()).thenReturn(changes.getUserName());
		when(mockEditorWidget.getFirstName()).thenReturn(changes.getFirstName());
		when(mockEditorWidget.getLastName()).thenReturn(changes.getLastName());
		when(mockEditorWidget.getPosition()).thenReturn(changes.getPosition());
		when(mockEditorWidget.getCompany()).thenReturn(changes.getCompany());
		when(mockEditorWidget.getIndustry()).thenReturn(changes.getIndustry());
		when(mockEditorWidget.getLocation()).thenReturn(changes.getLocation());
		when(mockEditorWidget.getUrl()).thenReturn(changes.getUrl());
		when(mockEditorWidget.getSummary()).thenReturn(changes.getSummary());

		UserProfile back = widget.updateProfileFromEditor();
		assertEquals(changes, back);
	}

	@Test
	public void testMergeFirstIntoSecondNull() {
		String startEtag = profile.getEtag();
		UserProfile merged = UserProfileModalWidgetImpl.mergeFirstIntoSecond(null, profile);
		assertNotNull(merged);
		assertEquals(startEtag, merged.getEtag());
	}

	@Test
	public void testMergeFirstIntoSecondEmpty() {
		String startEtag = profile.getEtag();
		String first = profile.getFirstName();
		String last = profile.getLastName();
		String summary = profile.getSummary();
		String position = profile.getPosition();
		String location = profile.getLocation();
		String industry = profile.getIndustry();
		String company = profile.getCompany();
		String imageId = profile.getProfilePicureFileHandleId();
		UserProfile merged = UserProfileModalWidgetImpl.mergeFirstIntoSecond(new UserProfile(), profile);
		assertNotNull(merged);
		// nothing should have changed.
		assertEquals("The etag should not have changed.", startEtag, merged.getEtag());
		assertEquals(first, merged.getFirstName());
		assertEquals(last, merged.getLastName());
		assertEquals(summary, merged.getSummary());
		assertEquals(position, merged.getPosition());
		assertEquals(location, merged.getLocation());
		assertEquals(industry, merged.getIndustry());
		assertEquals(company, merged.getCompany());
		assertEquals(imageId, merged.getProfilePicureFileHandleId());
	}

	@Test
	public void testMergeFirstIntoSecond() {
		String startEtag = profile.getEtag();
		UserProfile imported = new UserProfile();
		imported.setFirstName("importedFristName");
		imported.setLastName("importedLastName");
		imported.setSummary("importedSummary");
		imported.setPosition("importedPosition");
		imported.setLocation("importedLocation");
		imported.setIndustry("importedIndustry");
		imported.setCompany("importedCompany");
		imported.setProfilePicureFileHandleId("importedid");
		UserProfile merged = UserProfileModalWidgetImpl.mergeFirstIntoSecond(imported, profile);
		assertNotNull(merged);
		assertEquals("The etag should not have changed.", startEtag, merged.getEtag());
		assertEquals(imported.getFirstName(), merged.getFirstName());
		assertEquals(imported.getLastName(), merged.getLastName());
		assertEquals(imported.getSummary(), merged.getSummary());
		assertEquals(imported.getPosition(), merged.getPosition());
		assertEquals(imported.getLocation(), merged.getLocation());
		assertEquals(imported.getIndustry(), merged.getIndustry());
		assertEquals(imported.getCompany(), merged.getCompany());
		assertEquals(imported.getProfilePicureFileHandleId(), merged.getProfilePicureFileHandleId());
	}
}

