package org.sagebionetworks.web.unitclient.widget.table.v2.results.facets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResult;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.repo.model.table.FacetColumnResultValueCount;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.FacetType;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SingleButtonView;
import org.sagebionetworks.web.client.widget.table.v2.results.TableQueryResultWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultDateRangeViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultSliderRangeViewImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetsWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.IsWidget;

@RunWith(MockitoJUnitRunner.class)
public class FacetsWidgetTest {
	public static final String TEST_SQL = "select * from syn1234";

	@Mock
	DivView mockView;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	CallbackP<FacetColumnRequest> mockFacetChangedHandler;
	@Mock
	ColumnModel mockColumnModel;
	@Mock
	FacetColumnResultRange mockFacetColumnResultRange;
	@Mock
	FacetColumnResultValues mockFacetColumnResultValues;
	@Mock
	FacetColumnResultValueCount mockFacetResultValueCount;

	@Mock
	FacetColumnResultValuesWidget mockFacetColumnResultValuesWidget;
	@Mock
	FacetColumnResultRangeWidget mockFacetColumnResultRangeWidget;
	@Mock
	FacetColumnResultRangeViewImpl mockFacetColumnResultRangeView;
	@Mock
	FacetColumnResultDateRangeViewImpl mockFacetColumnResultDateView;
	@Mock
	FacetColumnResultSliderRangeViewImpl mockFacetColumnResultSliderView;
	@Mock
	SingleButtonView mockClearFacetsButton;
	@Captor
	ArgumentCaptor<SingleButtonView.Presenter> clearButtonCallbackCaptor;
	@Mock
	Callback mockResetFacetsHandler;
	@Mock
	AsynchronousProgressWidget mockJobTrackingWidget;
	@Mock
	Query mockQuery;
	@Mock
	QueryResultBundle mockQueryResultBundle;
	@Mock
	SynapseAlert mockSynAlert;
	@Captor
	ArgumentCaptor<AsynchronousProgressHandler> asyncProgressHandlerCaptor;
	@Captor
	ArgumentCaptor<QueryBundleRequest> qbrCaptor;

	FacetsWidget widget;
	List<FacetColumnResult> facets;
	List<ColumnModel> columnModels;
	List<FacetColumnResultValueCount> facetValues;
	public static final String COLUMN_NAME = "a column";

	@Before
	public void setUp() throws Exception {
		when(mockGinInjector.creatNewAsynchronousProgressWidget()).thenReturn(mockJobTrackingWidget);
		when(mockGinInjector.getSynapseAlertWidget()).thenReturn(mockSynAlert);
		widget = new FacetsWidget(mockView, mockClearFacetsButton, mockGinInjector);
		verify(mockView).addStyleName(anyString());
		facets = new ArrayList<FacetColumnResult>();
		columnModels = new ArrayList<ColumnModel>();
		facetValues = new ArrayList<FacetColumnResultValueCount>();
		columnModels.add(mockColumnModel);
		when(mockFacetColumnResultValues.getFacetValues()).thenReturn(facetValues);
		when(mockFacetColumnResultValues.getFacetType()).thenReturn(FacetType.enumeration);
		when(mockFacetColumnResultRange.getFacetType()).thenReturn(FacetType.range);

		when(mockGinInjector.getFacetColumnResultValuesWidget()).thenReturn(mockFacetColumnResultValuesWidget);
		when(mockGinInjector.getFacetColumnResultRangeWidget()).thenReturn(mockFacetColumnResultRangeWidget);

		when(mockGinInjector.getFacetColumnResultRangeViewImpl()).thenReturn(mockFacetColumnResultRangeView);
		when(mockGinInjector.getFacetColumnResultSliderRangeViewImpl()).thenReturn(mockFacetColumnResultSliderView);
		when(mockGinInjector.getFacetColumnResultDateRangeViewImpl()).thenReturn(mockFacetColumnResultDateView);

		when(mockFacetColumnResultRange.getColumnMin()).thenReturn("3");
		when(mockFacetColumnResultRange.getColumnMax()).thenReturn("120");
		when(mockColumnModel.getName()).thenReturn(COLUMN_NAME);
		when(mockFacetColumnResultRange.getColumnName()).thenReturn(COLUMN_NAME);
		when(mockFacetColumnResultValues.getColumnName()).thenReturn(COLUMN_NAME);
		when(mockQueryResultBundle.getFacets()).thenReturn(facets);
		when(mockQueryResultBundle.getColumnModels()).thenReturn(columnModels);
		when(mockQuery.getSql()).thenReturn(TEST_SQL);
	}

	@Test
	public void testConfigureFromQuery() {
		// set up a single facet being returned
		facets.add(mockFacetColumnResultValues);
		facetValues.add(mockFacetResultValueCount);
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.ENTITYID);

		widget.configure(mockQuery, mockFacetChangedHandler, mockResetFacetsHandler);

		verify(mockView).add(mockJobTrackingWidget);
		verify(mockJobTrackingWidget).startAndTrackJob(eq(TableQueryResultWidget.RUNNING_QUERY_MESSAGE), eq(false), eq(AsynchType.TableQuery), qbrCaptor.capture(), asyncProgressHandlerCaptor.capture());
		// verify request
		QueryBundleRequest request = qbrCaptor.getValue();
		assertEquals(new Long(TableQueryResultWidget.BUNDLE_MASK_QUERY_FACETS | TableQueryResultWidget.BUNDLE_MASK_QUERY_COLUMN_MODELS), request.getPartMask());
		assertEquals(mockQuery, request.getQuery());

		// test on complete
		AsynchronousProgressHandler progressHandler = asyncProgressHandlerCaptor.getValue();
		progressHandler.onComplete(mockQueryResultBundle);
		verify(mockFacetColumnResultValuesWidget).configure(mockFacetColumnResultValues, ColumnType.ENTITYID, mockFacetChangedHandler);

		Exception ex = new Exception("an error");
		progressHandler.onFailure(ex);
		verify(mockView).add(mockSynAlert);
		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testClearFacets() {
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);
		verify(mockClearFacetsButton).setPresenter(clearButtonCallbackCaptor.capture());

		clearButtonCallbackCaptor.getValue().onClick();

		verify(mockResetFacetsHandler).invoke();
	}

	@Test
	public void testConfigureNoFacets() {
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testConfigureResultValuesFacetValueEmpty() {
		facets.add(mockFacetColumnResultValues);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView).add(mockClearFacetsButton);
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testConfigureResultValuesFacet() {
		facets.add(mockFacetColumnResultValues);
		facetValues.add(mockFacetResultValueCount);
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.ENTITYID);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView, times(2)).add(any(IsWidget.class));
		verify(mockFacetColumnResultValuesWidget).configure(mockFacetColumnResultValues, ColumnType.ENTITYID, mockFacetChangedHandler);
	}

	@Test
	public void testConfigureResultUserIdsValuesFacet() {
		facets.add(mockFacetColumnResultValues);
		facetValues.add(mockFacetResultValueCount);
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.USERID);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView, times(2)).add(any(IsWidget.class));
		verify(mockFacetColumnResultValuesWidget).configure(mockFacetColumnResultValues, ColumnType.USERID, mockFacetChangedHandler);
	}


	@Test
	public void testConfigureFacetColumnMissing() {
		facets.add(mockFacetColumnResultValues);
		facetValues.add(mockFacetResultValueCount);
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.USERID);
		columnModels.clear();
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView).add(mockClearFacetsButton);
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testRangeFacetColumnValuesUndefined() {
		when(mockFacetColumnResultRange.getColumnMin()).thenReturn(null);
		when(mockFacetColumnResultRange.getColumnMax()).thenReturn(null);
		facets.add(mockFacetColumnResultRange);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView).add(mockClearFacetsButton);
		verifyNoMoreInteractions(mockView);
	}

	@Test
	public void testRangeFacetColumnValuesInteger() {
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.INTEGER);
		facets.add(mockFacetColumnResultRange);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView, times(2)).add(any(IsWidget.class));
		verify(mockFacetColumnResultRangeWidget).configure(mockFacetColumnResultSliderView, mockFacetColumnResultRange, mockFacetChangedHandler);
	}

	@Test
	public void testRangeFacetColumnValuesDouble() {
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.DOUBLE);
		facets.add(mockFacetColumnResultRange);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView, times(2)).add(any(IsWidget.class));
		verify(mockFacetColumnResultRangeWidget).configure(mockFacetColumnResultRangeView, mockFacetColumnResultRange, mockFacetChangedHandler);
	}

	@Test
	public void testRangeFacetColumnValuesDate() {
		when(mockColumnModel.getColumnType()).thenReturn(ColumnType.DATE);
		facets.add(mockFacetColumnResultRange);
		widget.configure(facets, mockFacetChangedHandler, columnModels, mockResetFacetsHandler);

		verify(mockView).clear();
		verify(mockView, times(2)).add(any(IsWidget.class));
		verify(mockFacetColumnResultRangeWidget).configure(mockFacetColumnResultDateView, mockFacetColumnResultRange, mockFacetChangedHandler);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
