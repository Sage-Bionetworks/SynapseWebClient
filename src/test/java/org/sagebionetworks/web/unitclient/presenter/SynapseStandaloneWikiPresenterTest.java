package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.WikiByTitle;
import org.sagebionetworks.web.client.presenter.SynapseStandaloneWikiPresenter;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.WikiPaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class SynapseStandaloneWikiPresenterTest {
	
	SynapseStandaloneWikiPresenter presenter;
	SynapseStandaloneWikiView mockView;
	SynapseClientAsync mockSynapseClient;
	WikiByTitle testPlace;
	WikiPaginatedResults results;
	String ownerId = "syn9";
	String wikiPageTitleMinusSpaces="AWikiPageTitle";
	String wikiPageTitle="A Wiki Page Title";
	List<WikiHeader> wikiHeaders;
	WikiPage targetWikiPage;
	String wikiPageMarkdown = "#H1\n##H2";
	
	@Before
	public void setup(){
		mockView = mock(SynapseStandaloneWikiView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		presenter = new SynapseStandaloneWikiPresenter(mockView, mockSynapseClient);
		verify(mockView).setPresenter(presenter);
		
		testPlace = Mockito.mock(WikiByTitle.class);
		when(testPlace.toToken()).thenReturn(wikiPageTitleMinusSpaces);
		
		results = new WikiPaginatedResults();
		results.setOwnerId(ownerId);
		results.setOwnerType(ObjectType.ENTITY);
		PaginatedResults<WikiHeader> pageHeaders = new PaginatedResults<WikiHeader>();
		pageHeaders.setTotalNumberOfResults(1L);
		WikiHeader header = new WikiHeader();
		header.setId("wikiid");
		header.setParentId(null);
		header.setTitle(wikiPageTitle);
		wikiHeaders = new ArrayList<WikiHeader>();
		wikiHeaders.add(header);
		pageHeaders.setResults(wikiHeaders);
		results.setPageHeaders(pageHeaders);
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getStandaloneWikis(any(AsyncCallback.class));
		
		targetWikiPage = mock(WikiPage.class);
		when(targetWikiPage.getMarkdown()).thenReturn(wikiPageMarkdown);
		AsyncMockStubber.callSuccessWith(targetWikiPage).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
	}	
	
	@Test
	public void testSetPlace() {
		//happy case.
		//from the page place token, it has a valid wiki page title that matches a result from synapseClient.getStandaloneWikis() (after removing spaces),
		//and it successfully asks for the target wiki page and hands the markdow to the view.
		presenter.setPlace(testPlace);
		verify(mockView).showLoading();
		verify(mockView, never()).showErrorMessage(anyString());
		verify(mockSynapseClient).getStandaloneWikis(any(AsyncCallback.class));
		verify(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		verify(mockView).configure(eq(wikiPageMarkdown), any(WikiPageKey.class));
	}
	
	/**
	 * Now test the many ways this can go wrong
	 */
	@Test
	public void testSetPlaceEmptyToken() {
		when(testPlace.toToken()).thenReturn("");
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(anyString());
	}
	@Test
	public void testSetPlaceNullToken() {
		when(testPlace.toToken()).thenReturn(null);
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testSetPlaceFailedToGetStandaloneWikis() {
		String error = "failed to get standalone wikis";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getStandaloneWikis(any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(error);
	}
	
	@Test
	public void testSetPlaceFailedToFineTargetWiki() {
		wikiHeaders.clear();
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(anyString());
	}
	@Test
	public void testSetPlaceFailedToGetWikiPage() {
		String error = "failed to get wiki";
		AsyncMockStubber.callFailureWith(new Exception(error)).when(mockSynapseClient).getV2WikiPageAsV1(any(WikiPageKey.class), any(AsyncCallback.class));
		presenter.setPlace(testPlace);
		verify(mockView).showErrorMessage(anyString());
	}
}
