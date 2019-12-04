package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionDashboardPresenter.NO_RESULTS;
import static org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionDashboardPresenter.TITLE;
import java.util.Arrays;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmission;
import org.sagebionetworks.repo.model.dataaccess.OpenSubmissionPage;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionDashboardPlace;
import org.sagebionetworks.web.client.presenter.ACTDataAccessSubmissionDashboardPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.OpenSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ACTDataAccessSubmissionDashboardPresenterTest {

	@Mock
	private ACTDataAccessSubmissionDashboardPlace mockPlace;
	@Mock
	private PlaceView mockView;
	@Mock
	private PortalGinInjector mockGinInjector;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private DataAccessClientAsync mockDataAccessClient;
	@Mock
	private LoadMoreWidgetContainer mockLoadMoreContainer;
	@Mock
	private DivView mockNoResultsDiv;
	@Mock
	private OpenSubmissionWidget mockOpenSubmissionWidget;

	ACTDataAccessSubmissionDashboardPresenter presenter;
	String nextPageToken;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		presenter = new ACTDataAccessSubmissionDashboardPresenter(mockView, mockDataAccessClient, mockSynAlert, mockGinInjector, mockLoadMoreContainer, mockNoResultsDiv);
	}

	@Test
	public void testConstructor() {
		verify(mockView, times(3)).add(any(Widget.class));
		verify(mockView).addTitle(TITLE);
		verify(mockNoResultsDiv).setText(NO_RESULTS);
		verify(mockNoResultsDiv).addStyleName("min-height-400");
		verify(mockNoResultsDiv).setVisible(false);

		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLoadMoreContainer).configure(captor.capture());
		Callback callback = captor.getValue();
		callback.invoke();
		verify(mockSynAlert).clear();
		verify(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testSetPlace() {
		presenter.setPlace(mockPlace);
		verify(mockView).initHeaderAndFooter();
		verify(mockLoadMoreContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
	}

	@Test
	public void testLoadMoreFailure() {
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		presenter.loadMore();
		verify(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		InOrder inOrder = inOrder(mockSynAlert);
		inOrder.verify(mockSynAlert).clear();
		inOrder.verify(mockSynAlert).handleException(ex);
		verify(mockLoadMoreContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreSuccessNoResults() {
		OpenSubmissionPage page = new OpenSubmissionPage();
		page.setOpenSubmissionList(new LinkedList<OpenSubmission>());
		AsyncMockStubber.callSuccessWith(page).when(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		presenter.loadMore();
		verify(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockNoResultsDiv).setVisible(true);
		verify(mockLoadMoreContainer, never()).add(any(Widget.class));
		verifyZeroInteractions(mockGinInjector);
		verify(mockLoadMoreContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreSuccess() {
		OpenSubmissionPage page = new OpenSubmissionPage();
		OpenSubmission openSubmission = new OpenSubmission();
		page.setOpenSubmissionList(Arrays.asList(openSubmission));
		page.setNextPageToken("there is a next page");
		AsyncMockStubber.callSuccessWith(page).when(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		when(mockGinInjector.getOpenSubmissionWidget()).thenReturn(mockOpenSubmissionWidget);
		presenter.loadMore();
		verify(mockDataAccessClient).getOpenSubmissions(anyString(), any(AsyncCallback.class));
		verify(mockSynAlert).clear();
		verify(mockNoResultsDiv, atLeastOnce()).setVisible(false);
		verify(mockGinInjector).getOpenSubmissionWidget();
		verify(mockOpenSubmissionWidget).configure(openSubmission);
		verify(mockLoadMoreContainer).add(any(Widget.class));
		verify(mockLoadMoreContainer).setIsMore(true);
	}
}
