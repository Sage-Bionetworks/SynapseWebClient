package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.client.widget.upload.FileHandleLink;
import org.sagebionetworks.web.client.widget.upload.FileHandleList;
import org.sagebionetworks.web.client.widget.upload.FileHandleListView;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputView;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.FileUploadHandler;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.user.client.ui.Widget;

public class FileHandleListTest {
	@Mock
	FileHandleListView mockView; 
	@Mock
	FileHandleUploadWidget mockUploadWidget;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	CallbackP<String> mockFileClickedCallback;
	@Mock
	FileUpload mockFileUpload;
	@Mock
	FileMetadata mockFileMetadata;
	@Mock
	FileHandleLink mockFileHandleLink;
	
	FileHandleList widget;
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(mockGinInjector.getFileHandleLink()).thenReturn(mockFileHandleLink);
		when(mockFileUpload.getFileMeta()).thenReturn(mockFileMetadata);
		when(mockFileHandleLink.configure(anyString(), anyString(), any(CallbackP.class))).thenReturn(mockFileHandleLink);
		when(mockFileHandleLink.setFileSelectCallback(any(Callback.class))).thenReturn(mockFileHandleLink);
		
		widget = new FileHandleList(mockView, mockUploadWidget, mockGinInjector);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setUploadWidget(any(Widget.class));
	}
	
	@Test
	public void testConfigure() {
		FileHandleList returnWidget = widget.configure(mockFileClickedCallback);
		
		//verify fluent interface api
		assertEquals(widget, returnWidget);
		//verify default view state
		verify(mockView).setToolbarVisible(false);
		verify(mockView).setUploadWidgetVisible(false);
		//verify upload widget default state
		verify(mockUploadWidget).reset();
		verify(mockUploadWidget).configure(eq(WebConstants.DEFAULT_FILE_HANDLE_WIDGET_TEXT), any(CallbackP.class));
	}

	@Test
	public void testSetUploadButtonText() {
		String customUploadText = "Upload your consciousness";
		FileHandleList returnWidget = widget.setUploadButtonText(customUploadText);
		//verify fluent interface api
		assertEquals(widget, returnWidget);
		//verify custom button text
		verify(mockUploadWidget).configure(eq(customUploadText), any(CallbackP.class));
	}

	@Test
	public void testSetCanUpload() {
		FileHandleList returnWidget = widget.setCanUpload(true);
		assertEquals(widget, returnWidget);
		verify(mockView).setUploadWidgetVisible(true);
	}

	@Test
	public void testSetCanDelete() {
		FileHandleList returnWidget = widget.setCanDelete(true);
		assertEquals(widget, returnWidget);
		verify(mockView).setToolbarVisible(true);
	}

	@Test
	public void testAddFileLinkFileUpload() {
		String fileHandleId = "88888888";
		String fileName = "proof.pdf";
		when(mockFileUpload.getFileHandleId()).thenReturn(fileHandleId);
		when(mockFileMetadata.getFileName()).thenReturn(fileName);
		widget.configure(mockFileClickedCallback)
			.addFileLink(mockFileUpload);
		verify(mockFileHandleLink).configure(fileHandleId, fileName, mockFileClickedCallback);
		verify(mockFileHandleLink).setFileSelectCallback(any(Callback.class));
		verify(mockFileHandleLink).setSelectVisible(false);
	}

	@Test
	public void testAddFileLinkFileHandleIdAndFileName() {
		String fileHandleId = "88888888";
		String fileName = "proof.pdf";
		widget.configure(mockFileClickedCallback)
			.addFileLink(fileHandleId, fileName);
		verify(mockFileHandleLink).configure(fileHandleId, fileName, mockFileClickedCallback);
		verify(mockFileHandleLink).setFileSelectCallback(any(Callback.class));
		verify(mockFileHandleLink).setSelectVisible(false);
	}

	@Test
	public void testRefreshLinkUI() {
		when(mockFileHandleLink.isSelected()).thenReturn(true);
		widget.configure(mockFileClickedCallback)
			.setCanDelete(true);
		
		//add a single file
		widget.addFileLink("123", "f1");
		reset(mockView);
		widget.refreshLinkUI();
		
		verify(mockView).clearFileLinks();
		verify(mockView).addFileLink(any(Widget.class));
		//show toolbar since we can delete and we're showing a file.
		verify(mockView).setToolbarVisible(true);
		//show that we can delete, since the single file is telling us that it's selected
		verify(mockView).setCanDelete(true);
	}

	@Test
	public void testDeleteSelected() {
		//set up a single file that is selected, then tell it to delete the selected files
		when(mockFileHandleLink.isSelected()).thenReturn(true);
		widget.configure(mockFileClickedCallback)
			.setCanDelete(true);
	
		//add a single file
		widget.addFileLink("123", "f1");
		reset(mockView);
		widget.deleteSelected();
		//no files left.  do not add a file widget to the view, and hide the toolbar
		verify(mockView, never()).addFileLink(any(Widget.class));
		verify(mockView).setToolbarVisible(false);
	}

	@Test
	public void testCheckSelectionState() {
		//simulate that there's a single file, but it is not selected
		when(mockFileHandleLink.isSelected()).thenReturn(false);
		widget.configure(mockFileClickedCallback)
			.setCanDelete(true);
	
		//add the single file
		widget.addFileLink("123", "f1");
		reset(mockView);
		
		widget.checkSelectionState();
		//the delete button should not be enabled
		verify(mockView).setCanDelete(false);
	}

	@Test
	public void testSelectAll() {
		widget.configure(mockFileClickedCallback);
		//add 2 files
		widget.addFileLink("123", "f1");
		widget.addFileLink("456", "f2");
		
		//select all
		widget.selectAll();
		verify(mockFileHandleLink, times(2)).setSelected(true);
	}

	@Test
	public void testSelectNone() {
		widget.configure(mockFileClickedCallback);
		//add 2 files
		widget.addFileLink("123", "f1");
		widget.addFileLink("456", "f2");
		
		//select none
		widget.selectNone();
		verify(mockFileHandleLink, times(2)).setSelected(false);

	}

	@Test
	public void testGetFileHandleIds() {
		widget.configure(mockFileClickedCallback);
		String fileHandleId = "1977";
		when(mockFileHandleLink.getFileHandleId()).thenReturn(fileHandleId);
		widget.addFileLink(fileHandleId, "f1");
		
		assertEquals(Collections.singletonList(fileHandleId), widget.getFileHandleIds());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
