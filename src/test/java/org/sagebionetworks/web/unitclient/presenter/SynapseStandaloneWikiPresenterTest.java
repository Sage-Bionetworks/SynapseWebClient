package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.StandaloneWiki;
import org.sagebionetworks.web.client.presenter.SynapseStandaloneWikiPresenter;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class SynapseStandaloneWikiPresenterTest {

	SynapseStandaloneWikiPresenter presenter;
	SynapseStandaloneWikiView mockView;
	SynapseClientAsync mockSynapseClient;
	StandaloneWiki testPlace;
	String ownerId = "syn9";
	String token = WebConstants.CERTIFICATION;
	WikiPage targetWikiPage;
	String wikiPageMarkdown = "#H1\n##H2";
	WikiPageKey certificationWikiKey;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockView = mock(SynapseStandaloneWikiView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new SynapseStandaloneWikiPresenter(mockView, mockSynapseClient, mockSynapseJavascriptClient, mockSynAlert);

		testPlace = new StandaloneWiki(token);

		HashMap<String, WikiPageKey> pageName2WikiKeyMap = new HashMap<String, WikiPageKey>();

		String entityId = "syn1113";
		String wikiId = "44442";
		certificationWikiKey = new WikiPageKey(entityId, ObjectType.ENTITY.toString(), wikiId);
		pageName2WikiKeyMap.put(WebConstants.CERTIFICATION, certificationWikiKey);
		targetWikiPage = mock(WikiPage.class);
		when(targetWikiPage.getMarkdown()).thenReturn(wikiPageMarkdown);
		AsyncMockStubber.callSuccessWith(targetWikiPage).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(pageName2WikiKeyMap).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
	}

	@Test
	public void testSetPlace() {
		// happy case 1, from wiki alias
		// from the page place token, it has a valid wiki page alias that matches a result from the portal
		// properties
		// and it successfully asks for the target wiki page and hands the markdown to the view.
		presenter.setPlace(testPlace);
		verify(mockSynAlert, never()).showError(anyString());
		verify(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		verify(mockView).configure(eq(wikiPageMarkdown), any(WikiPageKey.class));
	}

	@Test
	public void testSetPlaceWithWikiKey() {
		// happy case 2, from wiki key
		String ownerId = "syn1113";
		String wikiId = "44442";
		String ownerType = ObjectType.ENTITY.toString();
		testPlace = new StandaloneWiki(ownerId, ownerType, wikiId);

		presenter.setPlace(testPlace);
		verify(mockSynAlert, never()).showError(anyString());
		verify(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockSynapseClient, never()).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		verify(mockView).configure(eq(wikiPageMarkdown), any(WikiPageKey.class));
	}

	/**
	 * Now test the many ways this can go wrong
	 */
	@Test
	public void testSetPlaceEmptyToken() {
		testPlace = new StandaloneWiki("");
		presenter.setPlace(testPlace);
		verify(mockSynAlert).showError(anyString());
	}

	@Test
	public void testSetPlaceFailedToGetPageNameToWikiKeyMap() {
		Exception error = new Exception("failed to get wikis aliases");
		AsyncMockStubber.callFailureWith(error).when(mockSynapseClient).getPageNameToWikiKeyMap(any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockSynAlert).handleException(error);
	}

	@Test
	public void testSetPlaceFailedToFindTargetWiki() {
		token = "invalid alias";
		testPlace = new StandaloneWiki(token);
		presenter.setPlace(testPlace);
		verify(mockSynAlert).showError(anyString());
	}

	@Test
	public void testSetPlaceFailedToGetWikiPage() {
		Exception error = new Exception("failed to get wiki");
		AsyncMockStubber.callFailureWith(error).when(mockSynapseJavascriptClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockSynAlert).handleException(error);
	}
}
