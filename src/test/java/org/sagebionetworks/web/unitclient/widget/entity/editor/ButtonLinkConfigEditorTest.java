package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.ButtonLinkConfigView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.dev.util.collect.HashMap;

public class ButtonLinkConfigEditorTest {

	ButtonLinkConfigEditor editor;
	@Mock
	ButtonLinkConfigView mockView;
	@Mock
	WikiPageKey mockWikiPageKey;
	@Mock
	DialogCallback mockDialogCallback;
	Map<String, String> widgetDescriptor;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		editor = new ButtonLinkConfigEditor(mockView);
		widgetDescriptor = new HashMap<>();
		editor.configure(mockWikiPageKey, widgetDescriptor, mockDialogCallback);
	}

	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		verify(mockView).configure(mockWikiPageKey, widgetDescriptor);
	}

	@Test
	public void testUpdateDescriptorFromViewNoHighlight() {
		String buttonText = "a button";
		String url = "a url";
		boolean isHighlight = false;
		when(mockView.getName()).thenReturn(buttonText);
		when(mockView.getLinkUrl()).thenReturn(url);
		when(mockView.isHighlightButtonStyle()).thenReturn(isHighlight);
		String oldKeyValue = "garbage";
		widgetDescriptor.put(oldKeyValue, oldKeyValue);

		editor.updateDescriptorFromView();

		verify(mockView).checkParams();
		// verify old key values have been cleared
		assertFalse(widgetDescriptor.containsKey(oldKeyValue));
		assertEquals(buttonText, widgetDescriptor.get(WidgetConstants.TEXT_KEY));
		assertEquals(url, widgetDescriptor.get(WidgetConstants.LINK_URL_KEY));
		assertFalse(widgetDescriptor.containsKey(WebConstants.HIGHLIGHT_KEY));
	}

	@Test
	public void testUpdateDescriptorFromViewWithButtonHighlight() {
		boolean isHighlight = true;
		when(mockView.isHighlightButtonStyle()).thenReturn(isHighlight);

		editor.updateDescriptorFromView();

		assertEquals(Boolean.toString(true), widgetDescriptor.get(WebConstants.HIGHLIGHT_KEY));
	}
}
