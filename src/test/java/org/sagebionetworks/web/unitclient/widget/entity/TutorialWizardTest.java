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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.TutorialWizard;
import org.sagebionetworks.web.client.widget.entity.TutorialWizardView;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for wiki attachments widget
 */
public class TutorialWizardTest {

	TutorialWizard presenter;
	TutorialWizardView mockView;
	SynapseClientAsync mockSynapseClient;
	List<WikiHeader> wikiHeadersList;
	WikiHeader testRootHeader, page1, page2;
	
	
	private WikiHeader createWikiHeader(String id, String parentId, String title) {
		WikiHeader header = new WikiHeader();
		header.setId(id);
		header.setParentId(parentId);
		header.setTitle(title);
		return header;
	}
	
	@Before
	public void before() throws JSONObjectAdapterException{
		mockSynapseClient = Mockito.mock(SynapseClientAsync.class);
		mockView = Mockito.mock(TutorialWizardView.class);
			
		PaginatedResults<WikiHeader> wikiHeaders = new PaginatedResults<WikiHeader>();
		wikiHeadersList = new ArrayList<WikiHeader>();
		testRootHeader = createWikiHeader("123",null,"my test root wiki header (page)");
		page1 = createWikiHeader("99999", "123", "Step 1");
		page2 = createWikiHeader("1", "123", "Step 2");
		
		wikiHeadersList.add(testRootHeader);
		wikiHeadersList.add(page2);
		wikiHeadersList.add(page1);
		
		wikiHeaders.setResults(wikiHeadersList);
		AsyncMockStubber.callSuccessWith(wikiHeaders).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		
		// setup the entity editor with 
		presenter = new TutorialWizard(mockView, mockSynapseClient);
	}

	@Test
	public void testConfigure() {
		presenter.configure("syn1234",new TutorialWizard.Callback() {
			
			@Override
			public void tutorialSkipped() {
			}
			
			@Override
			public void tutorialFinished() {
			}
		});
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(mockSynapseClient).getWikiHeaderTree(anyString(),  anyString(),  any(AsyncCallback.class));
		verify(mockView).showWizard(anyString(), captor.capture());
		List sortedHeaders = captor.getValue();
		
		//the root should not be passed on
		assertFalse(sortedHeaders.contains(testRootHeader));
		assertEquals(2, sortedHeaders.size());
		//and verify the order (page1 has a larger ID (added later) but it should be first because of it's title.
		assertEquals(page1, sortedHeaders.get(0));
	}
	
	@Test
	public void testConfigureFail() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getWikiHeaderTree(anyString(), anyString(), any(AsyncCallback.class));
		presenter.configure("syn1234", null);
		verify(mockView).showErrorMessage(anyString());
	}
}
