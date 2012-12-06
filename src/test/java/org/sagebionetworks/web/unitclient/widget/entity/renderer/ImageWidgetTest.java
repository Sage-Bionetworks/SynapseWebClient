package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.ImageWidgetView;

public class ImageWidgetTest {
		
	ImageWidget widget;
	ImageWidgetView mockView;
	
	@Before
	public void setup(){
		mockView = mock(ImageWidgetView.class);
		widget = new ImageWidget(mockView);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {
		ImageAttachmentWidgetDescriptor descriptor = new ImageAttachmentWidgetDescriptor();
		AttachmentData testImage = new AttachmentData();
		testImage.setName("test name");
		testImage.setTokenId("test token");
		testImage.setMd5("test md5");
		descriptor.setImage(testImage);
		widget.configure("", descriptor);
		verify(mockView).configure(anyString(), eq(testImage));
	}
}
