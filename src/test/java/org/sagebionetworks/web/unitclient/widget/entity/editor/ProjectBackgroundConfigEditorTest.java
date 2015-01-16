package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.AttachmentConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.ProjectBackgroundConfigEditor;
import org.sagebionetworks.web.client.widget.table.modal.upload.ContentTypeDelimiter;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.client.widget.upload.FileMetadata;
import org.sagebionetworks.web.shared.WikiPageKey;
public class ProjectBackgroundConfigEditorTest {
		
	ProjectBackgroundConfigEditor editor;
	AttachmentConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null, null);
	FileInputWidget mockFileInputWidget;
	String testFileName = "testing.txt";
	DialogCallback mockCallback;
	
	@Before
	public void setup(){
		mockFileInputWidget = mock(FileInputWidget.class);
		mockView = mock(AttachmentConfigView.class);
		mockCallback = mock(DialogCallback.class);
		editor = new ProjectBackgroundConfigEditor(mockView,mockFileInputWidget);
		when(mockFileInputWidget.getSelectedFileMetadata()).thenReturn(new FileMetadata[]{new FileMetadata(testFileName, ContentTypeDelimiter.TEXT.getContentType())});
	}
	
	@Test
	public void testConfigure() {
		Map<String,String> descriptor = new HashMap<String, String>();
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockView).showNote(anyString());
	}
}
