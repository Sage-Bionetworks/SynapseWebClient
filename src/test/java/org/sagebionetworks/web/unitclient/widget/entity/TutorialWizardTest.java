package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.TutorialWizard;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for wiki attachments widget
 */
@RunWith(MockitoJUnitRunner.class)
public class TutorialWizardTest {

	TutorialWizard presenter;
	@Mock
	TutorialWizardView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	List<V2WikiHeader> wikiHeadersList;
	V2WikiHeader testRootHeader, page1, page2;


	private V2WikiHeader createWikiHeader(String id, String parentId, String title) {
		V2WikiHeader header = new V2WikiHeader();
		header.setId(id);
		header.setParentId(parentId);
		header.setTitle(title);
		return header;
	}

	@Before
	public void before() throws JSONObjectAdapterException {
		wikiHeadersList = new ArrayList<V2WikiHeader>();
		testRootHeader = createWikiHeader("123", null, "my test root wiki header (page)");
		page1 = createWikiHeader("99999", "123", "Step 1");
		page2 = createWikiHeader("1", "123", "Step 2");

		wikiHeadersList.add(testRootHeader);
		wikiHeadersList.add(page2);
		wikiHeadersList.add(page1);

		AsyncMockStubber.callSuccessWith(wikiHeadersList).when(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));

		// setup the entity editor with
		presenter = new TutorialWizard(mockView, mockJsClient);
	}

	@Test
	public void testConfigure() {
		presenter.configure("syn1234", new TutorialWizard.Callback() {

			@Override
			public void tutorialSkipped() {}

			@Override
			public void tutorialFinished() {}
		});
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		verify(mockView).showWizard(anyString(), captor.capture());
		List sortedHeaders = captor.getValue();

		// the root should not be passed on
		assertFalse(sortedHeaders.contains(testRootHeader));
		assertEquals(2, sortedHeaders.size());
		// and verify the order (page1 has a larger ID (added later) but it should be first because of it's
		// title.
		assertEquals(page1, sortedHeaders.get(0));
	}

	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockJsClient).getV2WikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("syn1234", null);
		verify(mockView).showErrorMessage(anyString());
	}
}
