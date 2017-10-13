package org.sagebionetworks.web.unitclient.widget.entity.renderer;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ImageWidgetTest {
//		
	ImageWidget widget;
	@Mock
	ImageWidgetView mockView;
	Map<String, String> descriptor;
	WikiPageKey wikiKey = new WikiPageKey("syn222222", ObjectType.ENTITY.toString(), "9");
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock 
	PresignedURLAsyncHandler mockPresignedURLAsyncHandler;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	FileResult mockFileResult;
	@Mock
	FileHandle mockFileHandle1;
	@Mock
	FileHandle mockFileHandle2;
	@Captor
	ArgumentCaptor<FileHandleAssociation> fhaCaptor;
	
	public static final String PRESIGNED_URL = "https://s3.presigned/image.jpg";
	public static final String FILE_NAME = "image.jpg";
	@Before
	public void setup() throws JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		mockView = mock(ImageWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new ImageWidget(
				mockView, 
				mockAuthenticationController,
				mockPresignedURLAsyncHandler,
				mockSynapseJavascriptClient,
				mockSynAlert);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, FILE_NAME);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockFileResult.getPreSignedURL()).thenReturn(PRESIGNED_URL);
		AsyncMockStubber.callSuccessWith(mockFileResult).when(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigureFromSynapseId() {
		AsyncMockStubber.callSuccessWith(mockFileEntity).when(mockSynapseJavascriptClient).getEntityForVersion(anyString(), anyLong(), any(AsyncCallback.class));
		String synId = "syn239";
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		String dataFileHandleId = "8765";
		when(mockFileEntity.getId()).thenReturn(synId);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);
		
		widget.configure(wikiKey,descriptor, null, null);
		
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getEntityForVersion(eq(synId), eq((Long)null), any(AsyncCallback.class));
		verify(mockPresignedURLAsyncHandler).getFileResult(any(FileHandleAssociation.class), any(AsyncCallback.class));
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq(synId), eq(isLoggedIn));
	}
	
	@Test
	public void testConfigureFromSynapseIdWithVersion() {
		AsyncMockStubber.callSuccessWith(mockFileEntity).when(mockSynapseJavascriptClient).getEntityForVersion(anyString(), anyLong(), any(AsyncCallback.class));
		String synId = "syn239";
		Long version = 999L;
		descriptor.put(WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		descriptor.put(WidgetConstants.WIDGET_ENTITY_VERSION_KEY, version.toString());
		String dataFileHandleId = "8765";
		when(mockFileEntity.getId()).thenReturn(synId);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);
		
		widget.configure(wikiKey,descriptor, null, null);
		
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getEntityForVersion(eq(synId), eq(version), any(AsyncCallback.class));
		verify(mockPresignedURLAsyncHandler).getFileResult(fhaCaptor.capture(), any(AsyncCallback.class));
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(synId, fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.FileEntity, fha.getAssociateObjectType());
		assertEquals(dataFileHandleId, fha.getFileHandleId());
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq(synId), eq(isLoggedIn));
	}
	
	@Test
	public void testConfigureFromWikiAttachment() {
		List<FileHandle> fileHandles = new ArrayList<>();
		fileHandles.add(mockFileHandle1);
		fileHandles.add(mockFileHandle2);
		AsyncMockStubber.callSuccessWith(fileHandles).when(mockSynapseJavascriptClient).getWikiAttachmentFileHandles(any(WikiPageKey.class), anyLong(), any(AsyncCallback.class));
		when(mockFileHandle1.getFileName()).thenReturn("wrong file.txt");
		when(mockFileHandle2.getFileName()).thenReturn(FILE_NAME);
		String fileHandleId1 = "1111";
		String fileHandleId2 = "2222";
		when(mockFileHandle1.getId()).thenReturn(fileHandleId1);
		when(mockFileHandle2.getId()).thenReturn(fileHandleId2);
		Long wikiVersion = 2L;
		
		widget.configure(wikiKey,descriptor, null, wikiVersion);
		
		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getWikiAttachmentFileHandles(any(WikiPageKey.class), eq(wikiVersion), any(AsyncCallback.class));
		verify(mockPresignedURLAsyncHandler).getFileResult(fhaCaptor.capture(), any(AsyncCallback.class));
		FileHandleAssociation fha = fhaCaptor.getValue();
		assertEquals(wikiKey.getWikiPageId(), fha.getAssociateObjectId());
		assertEquals(FileHandleAssociateType.WikiAttachment, fha.getAssociateObjectType());
		assertEquals(fileHandleId2, fha.getFileHandleId());
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq((String)null), eq(isLoggedIn));
	}
	
	@Test
	public void testConfigureDefaultResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, null);
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}
	@Test
	public void testConfigureResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.TRUE.toString());
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}
	@Test
	public void testConfigureNotResponsive() {
		descriptor.put(WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.FALSE.toString());
		widget.configure(wikiKey,descriptor, null, null);
		verify(mockView).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}


	
}
