package org.sagebionetworks.web.unitclient.widget.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidget.ActionHandler;
import org.sagebionetworks.web.client.widget.entity.WikiHistoryWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Unit test for the Wiki's history widget
 * 
 * @author hso
 *
 */
public class WikiHistoryWidgetTest {
	@Mock
	WikiHistoryWidgetView mockView;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	WikiHistoryWidget presenter;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		presenter = new WikiHistoryWidget(mockGlobalApplicationState, mockView, mockSynapseClient, mockAuthenticationController, mockSynapseJavascriptClient, mockSynAlert);

		PaginatedResults<JSONEntity> paginatedHistory = new PaginatedResults<JSONEntity>();
		paginatedHistory.setTotalNumberOfResults(1);
		List<JSONEntity> results = new ArrayList<JSONEntity>();
		V2WikiHistorySnapshot snapshot = new V2WikiHistorySnapshot();
		results.add(snapshot);
		paginatedHistory.setResults(results);
		// when(mockNodeModelCreator.createPaginatedResults(anyString(),
		// any(Class.class))).thenReturn(paginatedHistory);
		AsyncMockStubber.callSuccessWith(paginatedHistory).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), any(Long.class), any(Long.class), any(AsyncCallback.class));

		UserGroupHeaderResponsePage responsePage = new UserGroupHeaderResponsePage();
		responsePage.setChildren(new ArrayList<UserGroupHeader>());

		AsyncMockStubber.callSuccessWith(responsePage).when(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
	}

	@Test
	public void testAsWidget() {
		presenter.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testHideHistoryWidget() {
		presenter.hideHistoryWidget();
		verify(mockView).hideHistoryWidget();
	}

	@Test
	public void testShowHistoryWidget() {
		presenter.showHistoryWidget();
		verify(mockView).showHistoryWidget();
	}

	@Test
	public void testConfigure() {
		presenter.configure(new WikiPageKey("ownerId", ObjectType.ENTITY.toString(), null, null), true, new ActionHandler() {
			@Override
			public void previewClicked(Long versionToPreview, Long currentVersion) {}

			@Override
			public void restoreClicked(Long versionToRestore) {}
		});
		verify(mockView).configure(anyBoolean(), any(ActionHandler.class));
	}

	@Test
	public void testConfigureNextPageFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), any(Long.class), any(Long.class), any(AsyncCallback.class));
		presenter.configureNextPage(new Long(0), new Long(10));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testConfigureNextPageUnavailableFailure() {
		AsyncMockStubber.callFailureWith(new Exception(WikiHistoryWidget.NO_HISTORY_IS_FOUND_FOR_A_WIKI + " page id: 1298923")).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), any(Long.class), any(Long.class), any(AsyncCallback.class));
		presenter.configureNextPage(new Long(0), new Long(10));
		verify(mockView).hideLoadMoreButton();
	}

	@Test
	public void testConfigureNextPageEmptyResults() {
		PaginatedResults<JSONEntity> paginatedHistory = new PaginatedResults<JSONEntity>();
		paginatedHistory.setTotalNumberOfResults(0);
		paginatedHistory.setResults(new ArrayList<JSONEntity>());
		AsyncMockStubber.callSuccessWith(paginatedHistory).when(mockSynapseClient).getV2WikiHistory(any(WikiPageKey.class), any(Long.class), any(Long.class), any(AsyncCallback.class));
		presenter.configureNextPage(new Long(10), new Long(10));
		verify(mockView).hideLoadMoreButton();
	}


	@Test
	public void testConfigureNextPageFailure2() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		presenter.configureNextPage(new Long(0), new Long(10));
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testConfigureNextPage() {
		presenter.configureNextPage(new Long(0), new Long(10));
		verify(mockView).updateHistoryList(any(List.class));
		verify(mockView).buildHistoryWidget();

	}
}
