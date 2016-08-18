package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.docker.DigestWidget;
import org.sagebionetworks.web.client.widget.docker.DigestWidgetView;

public class DigestWidgetTest {

	@Mock
	DigestWidgetView mockView;
	DigestWidget widget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new DigestWidget(mockView);
	}

	@Test
	public void testConfigure() {
		String digest = "digest";
		widget.configure(digest);
		verify(mockView).setDigest(digest);
	}
}
