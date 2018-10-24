package org.sagebionetworks.web.unitclient.widget.entity.file.downloadlist;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRow;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.FileHandleAssociationRowView;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.dev.javac.testing.impl.MockJavaResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class FileHandleAssociationRowTest {
	FileHandleAssociationRow widget;
	@Mock
	FileHandleAssociationRowView mockView;
	@Mock
	FileHandleAsyncHandler mockFhaAsyncHandler;
	@Mock
	UserProfileAsyncHandler mockUserProfileAsyncHandler;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	EntityHeaderAsyncHandler mockEntityHeaderAsyncHandler;
	@Mock
	FileHandleAssociation mockFha;
	@Mock
	CallbackP<FileHandleAssociation> mockOnDeleteCallback;
	@Mock
	CallbackP<Double> mockAddToPackageSizeCallback;
	@Mock
	Callback mockAccessRestrictionDetectedCallback;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	EntityHeader mockEntityHeader;
	@Mock
	FileResult mockFileResult;
	@Mock
	FileHandle mockFileHandle;
	@Mock
	UserProfile mockUserProfile;
	@Captor
	ArgumentCaptor<AsyncCallback<RestrictionInformationResponse>> asyncCallbackCaptor;
	@Mock
	RestrictionInformationResponse mockRestrictionInformationResponse;
	
	public static final String ENTITY_ID = "syn92832";
	public static final String FILENAME = "filename.txt";
	public static final Date CREATED_ON = new Date();
	public static final String FRIENDLY_DATE = "9/25/2018";
	public static final String CREATED_BY = "3947834";
	public static final Long CONTENT_SIZE = 38473L;
	public static final String FRIENDLY_SIZE = "38 KB";
	public static final String USERNAME = "myusername";
	 
	@Before
	public void setUp() throws Exception {
		widget = new FileHandleAssociationRow(mockView, mockFhaAsyncHandler, mockUserProfileAsyncHandler, mockJsniUtils, mockEntityHeaderAsyncHandler, mockDateTimeUtils, mockGwt, mockJsClient);
		when(mockFileResult.getFileHandle()).thenReturn(mockFileHandle);
		when(mockFha.getAssociateObjectId()).thenReturn(ENTITY_ID);
		when(mockEntityHeader.getName()).thenReturn(FILENAME);
		when(mockEntityHeader.getId()).thenReturn(ENTITY_ID);
		when(mockFileHandle.getCreatedOn()).thenReturn(CREATED_ON);
		when(mockFileHandle.getContentSize()).thenReturn(CONTENT_SIZE);
		when(mockFileHandle.getCreatedBy()).thenReturn(CREATED_BY);
		when(mockDateTimeUtils.getDateTimeString(any(Date.class))).thenReturn(FRIENDLY_DATE);
		when(mockGwt.getFriendlySize(anyDouble(), anyBoolean())).thenReturn(FRIENDLY_SIZE);
		when(mockView.isAttached()).thenReturn(true);
		when(mockUserProfile.getUserName()).thenReturn(USERNAME);
		AsyncMockStubber.callSuccessWith(mockEntityHeader).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockFhaAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(mockUserProfile).when(mockUserProfileAsyncHandler).getUserProfile(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testHappyCase() {
		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);
		
		verify(mockView).setFileName(FILENAME, ENTITY_ID);
		verify(mockView).setCreatedOn(FRIENDLY_DATE);
		verify(mockView).setFileSize(FRIENDLY_SIZE);
		verify(mockAddToPackageSizeCallback).invoke(CONTENT_SIZE.doubleValue());
		verify(mockView).setCreatedBy(DisplayUtils.getDisplayName(mockUserProfile));
		verifyZeroInteractions(mockAccessRestrictionDetectedCallback);
		assertEquals(CREATED_ON, widget.getCreatedOn());
		assertEquals(DisplayUtils.getDisplayName(mockUserProfile), widget.getCreatedBy());
		assertEquals(CONTENT_SIZE, widget.getFileSize());
		assertEquals(FILENAME, widget.getFileName());
		assertEquals(true, widget.getHasAccess());
	}

	@Test
	public void testGetEntityHeaderFailure() {
		String errorMessage = "unable to get entity header";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockEntityHeaderAsyncHandler).getEntityHeader(anyString(), any(AsyncCallback.class));

		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);
		
		verify(mockJsniUtils).consoleError(errorMessage);
	}
	
	@Test
	public void testNoFileHandleReturned() {
		when(mockFileResult.getFileHandle()).thenReturn(null);

		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);
		
		verifyZeroInteractions(mockAccessRestrictionDetectedCallback);
		assertEquals(false, widget.getHasAccess());
	}
	
	@Test
	public void testHasAccessRestriction() {
		String errorMessage = "forbidden";
		AsyncMockStubber.callFailureWith(new ForbiddenException(errorMessage)).when(mockFhaAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));

		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);
		
		assertEquals(false, widget.getHasAccess());
		verify(mockAccessRestrictionDetectedCallback, never()).invoke();
		
		verify(mockJsClient).getRestrictionInformation(eq(ENTITY_ID), eq(RestrictableObjectType.ENTITY), asyncCallbackCaptor.capture());
		when(mockRestrictionInformationResponse.getHasUnmetAccessRequirement()).thenReturn(true);
		asyncCallbackCaptor.getValue().onSuccess(mockRestrictionInformationResponse);
		
		verify(mockView).showHasUnmetAccessRequirements(ENTITY_ID);
		verify(mockAccessRestrictionDetectedCallback).invoke();
	}
	
	@Test
	public void testFileNotFound() {
		String errorMessage = "file not found";
		AsyncMockStubber.callFailureWith(new NotFoundException(errorMessage)).when(mockFhaAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));

		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);
		
		assertEquals(false, widget.getHasAccess());
		verify(mockJsniUtils).consoleError(errorMessage);
		verifyZeroInteractions(mockAccessRestrictionDetectedCallback);
	}

	@Test
	public void testGetUserProfileError() {
		String errorMessage = "unable to get user profile";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockUserProfileAsyncHandler).getUserProfile(anyString(), any(AsyncCallback.class));

		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);

		verify(mockJsniUtils).consoleError(errorMessage);
	}
	
	@Test
	public void testOnRemove() {
		widget.configure(mockFha, mockAccessRestrictionDetectedCallback, mockAddToPackageSizeCallback, mockOnDeleteCallback);

		widget.onRemove();
		
		verify(mockOnDeleteCallback).invoke(mockFha);
	}
}
