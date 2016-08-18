package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.docker.DigestWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidgetView;

import com.google.gwt.user.client.ui.Widget;

public class DockerCommitRowWidgetTest {
	@Mock
	private DockerCommitRowWidgetView mockView;
	@Mock
	private DigestWidget mockDigestWidget;
	@Mock
	private CallbackP<DockerCommit> mockCallback;
	private DockerCommitRowWidget widget;

	DockerCommit commit;
	String tag;
	String digest;
	Date date;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new DockerCommitRowWidget(mockView, mockDigestWidget);
		commit = new DockerCommit();
		tag = "tag";
		digest = "digest";
		date = new Date();
		commit.setTag(tag);
		commit.setDigest(digest);
		commit.setCreatedOn(date);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setDigest(any(Widget.class));
	}

	@Test
	public void testConfigure() {
		widget.configure(commit);
		verify(mockView).setTag(tag);
		verify(mockDigestWidget).configure(digest);
		verify(mockView).setCreatedOn(date);
	}

	@Test
	public void testOnClick() {
		widget.configure(commit);
		widget.setOnClickCallback(mockCallback);
		widget.onClick();
		verify(mockCallback).invoke(commit);
	}
}
