package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.ImageConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ImageLinkConfigEditor;
import org.sagebionetworks.web.shared.WikiPageKey;

public class ImageLinkConfigEditorTest {

	@Mock
	ImageConfigEditor mockImageConfigEditor;
	@Mock
	DialogCallback mockCallback;

	ImageLinkConfigEditor editor;
	WikiPageKey wikiKey;
	Map<String, String> descriptor;


	@Before
	public void setup() {
		descriptor = new HashMap<String, String>();
		MockitoAnnotations.initMocks(this);
		editor = new ImageLinkConfigEditor(mockImageConfigEditor);
	}

	@Test
	public void testConfigure() {
		editor.configure(wikiKey, descriptor, mockCallback);
		verify(mockImageConfigEditor).configureWithoutUpload(eq(wikiKey), eq(descriptor), eq(mockCallback));
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockImageConfigEditor).asWidget();
	}

	@Test
	public void testTextToInsert() {
		editor.getTextToInsert();
		verify(mockImageConfigEditor).getTextToInsert();
	}

	@Test
	public void testUpdateDescriptorFromView() {
		editor.updateDescriptorFromView();
		verify(mockImageConfigEditor).updateDescriptorFromView();
	}
}
