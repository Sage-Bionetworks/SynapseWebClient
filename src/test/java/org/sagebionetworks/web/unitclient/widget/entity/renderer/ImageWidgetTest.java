package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget.ALIGN_CENTER_STYLES;
import static org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget.FLOAT_LEFT_STYLES;
import static org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget.FLOAT_RIGHT_STYLES;
import static org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget.getAlignmentStyleNames;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_ALT_TEXT_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.WIDGET_ENTITY_VERSION_KEY;
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
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
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
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	FileEntity mockFileEntity;
	@Mock
	FileHandle mockFileHandle1;
	@Mock
	FileHandle mockFileHandle2;
	@Captor
	ArgumentCaptor<Throwable> throwableCaptor;
	@Mock
	SynapseJSNIUtils mockJSNIUtils;

	public static final String PRESIGNED_URL = "https://s3.presigned/image.jpg";
	public static final String FILE_NAME = "image.jpg";
	public static final String ALT_TEXT = "image alternate text";

	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(ImageWidgetView.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		widget = new ImageWidget(mockView, mockAuthenticationController, mockSynapseJavascriptClient, mockSynAlert, mockJSNIUtils);
		descriptor = new HashMap<String, String>();
		descriptor.put(IMAGE_WIDGET_FILE_NAME_KEY, FILE_NAME);
		descriptor.put(IMAGE_WIDGET_ALT_TEXT_KEY, ALT_TEXT);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		when(mockJSNIUtils.getFileHandleAssociationUrl(anyString(),  any(FileHandleAssociateType.class), anyString())).thenReturn(PRESIGNED_URL);
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
		descriptor.put(IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		String dataFileHandleId = "8765";
		when(mockFileEntity.getId()).thenReturn(synId);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getEntityForVersion(eq(synId), eq((Long) null), any(AsyncCallback.class));
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq(ALT_TEXT), eq(synId), eq(isLoggedIn));
	}

	@Test
	public void testConfigureFromSynapseIdWithVersion() {
		AsyncMockStubber.callSuccessWith(mockFileEntity).when(mockSynapseJavascriptClient).getEntityForVersion(anyString(), anyLong(), any(AsyncCallback.class));
		String synId = "syn239";
		Long version = 999L;
		descriptor.put(IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);
		descriptor.put(WIDGET_ENTITY_VERSION_KEY, version.toString());
		String dataFileHandleId = "8765";
		when(mockFileEntity.getId()).thenReturn(synId);
		when(mockFileEntity.getDataFileHandleId()).thenReturn(dataFileHandleId);

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getEntityForVersion(eq(synId), eq(version), any(AsyncCallback.class));
		verify(mockJSNIUtils).getFileHandleAssociationUrl(synId, FileHandleAssociateType.FileEntity, dataFileHandleId);
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq(ALT_TEXT), eq(synId), eq(isLoggedIn));
	}

	@Test
	public void testConfigureFromSynapseIdError() {
		Exception ex = new Exception("so sad");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getEntityForVersion(anyString(), anyLong(), any(AsyncCallback.class));
		String synId = "syn239";
		descriptor.put(IMAGE_WIDGET_SYNAPSE_ID_KEY, synId);

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);
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

		widget.configure(wikiKey, descriptor, null, wikiVersion);

		verify(mockSynAlert).clear();
		verify(mockSynapseJavascriptClient).getWikiAttachmentFileHandles(any(WikiPageKey.class), eq(wikiVersion), any(AsyncCallback.class));
		verify(mockJSNIUtils).getFileHandleAssociationUrl(wikiKey.getWikiPageId(), FileHandleAssociateType.WikiAttachment, fileHandleId2);
		boolean isLoggedIn = true;
		verify(mockView).configure(eq(PRESIGNED_URL), eq(FILE_NAME), anyString(), anyString(), eq(ALT_TEXT), eq((String) null), eq(isLoggedIn));
	}

	@Test
	public void testConfigureFromWikiAttachmentError() {
		Exception ex = new Exception("so sad");
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getWikiAttachmentFileHandles(any(WikiPageKey.class), anyLong(), any(AsyncCallback.class));

		widget.configure(wikiKey, descriptor, null, null);

		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(ex);
	}


	@Test
	public void testConfigureDefaultResponsive() {
		descriptor.put(IMAGE_WIDGET_RESPONSIVE_KEY, null);
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}

	@Test
	public void testConfigureResponsive() {
		descriptor.put(IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.TRUE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView, never()).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}

	@Test
	public void testConfigureNotResponsive() {
		descriptor.put(IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.FALSE.toString());
		widget.configure(wikiKey, descriptor, null, null);
		verify(mockView).addStyleName(ImageWidget.MAX_WIDTH_NONE);
	}

	@Test
	public void testAlignmentStyles() {
		assertEquals("", getAlignmentStyleNames(WidgetConstants.FLOAT_NONE));
		assertEquals(FLOAT_LEFT_STYLES, getAlignmentStyleNames(WidgetConstants.FLOAT_LEFT));
		assertEquals(FLOAT_RIGHT_STYLES, getAlignmentStyleNames(WidgetConstants.FLOAT_RIGHT));
		assertEquals(ALIGN_CENTER_STYLES, getAlignmentStyleNames(WidgetConstants.FLOAT_CENTER));
	}
}
