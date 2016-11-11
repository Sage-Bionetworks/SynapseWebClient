package org.sagebionetworks.web.unitclient.widget.table.v2.results.facets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultValueCount;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesWidget;

public class FacetColumnResultValuesWidgetTest {

	@Mock
	FacetColumnResultValuesView mockView;
	@Mock
	FacetColumnResultValues mockFacet;
	@Mock
	CallbackP<FacetColumnRequest> mockOnFacetRequest;
	
	@Mock
	FacetColumnResultValueCount nullValueCount;
	@Mock
	FacetColumnResultValueCount emptyValueCount;
	@Mock
	FacetColumnResultValueCount valueCount;
	
	public static final String VALUE = "column value";
	public static final Long DEFAULT_COUNT = 60L;
	public static final boolean DEFAULT_SELECTED = false;
	@Captor
	ArgumentCaptor<FacetColumnValuesRequest> requestCaptor;
	List<FacetColumnResultValueCount> facetValues;
	FacetColumnResultValuesWidget widget;
	public static final String COLUMN_NAME = "col name";
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FacetColumnResultValuesWidget(mockView);
		when(mockFacet.getColumnName()).thenReturn(COLUMN_NAME);
		facetValues = new ArrayList<FacetColumnResultValueCount>();
		when(mockFacet.getFacetValues()).thenReturn(facetValues);
		
		when(nullValueCount.getValue()).thenReturn(null);
		when(nullValueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(nullValueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);
		
		when(emptyValueCount.getValue()).thenReturn("");
		when(emptyValueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(emptyValueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);
		
		when(valueCount.getValue()).thenReturn(VALUE);
		when(valueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(valueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testConfigureNullValue() {
		facetValues.add(nullValueCount);
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).addValue(DEFAULT_SELECTED, FacetColumnResultValuesWidget.UNSPECIFIED, DEFAULT_COUNT, null);
		verify(mockView).setShowAllButtonVisible(false);
	}
	
	@Test
	public void testConfigureEmptyValue() {
		facetValues.add(emptyValueCount);
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).addValue(DEFAULT_SELECTED, FacetColumnResultValuesWidget.EMPTY_STRING, DEFAULT_COUNT, "");
		verify(mockView).setShowAllButtonVisible(false);
	}
	
	@Test
	public void testConfigureValue() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).addValue(DEFAULT_SELECTED, VALUE, DEFAULT_COUNT, VALUE);
		verify(mockView).setShowAllButtonVisible(false);
	}
	
	@Test
	public void testOverflow() {
		int numberOfFacets = FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES + 20;
		for (int i = 0; i < numberOfFacets; i++) {
			FacetColumnResultValueCount valuesCount = Mockito.mock(FacetColumnResultValueCount.class);
			facetValues.add(valuesCount);
		}
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, times(FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES)).addValue(anyBoolean(), anyString(), anyLong(), anyString());
		verify(mockView, times(numberOfFacets - FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES)).addValueToOverflow(anyBoolean(), anyString(), anyLong(), anyString());
		verify(mockView).setShowAllButtonText(FacetColumnResultValuesWidget.SHOW_ALL + numberOfFacets);
		verify(mockView).setShowAllButtonVisible(true);
	}
	
	@Test
	public void testOnFacetAdd() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, mockOnFacetRequest);
		widget.onFacetChange(VALUE);
		verify(mockOnFacetRequest).invoke(requestCaptor.capture());
		FacetColumnValuesRequest request = requestCaptor.getValue();
		assertEquals(1, request.getFacetValues().size());
		assertEquals(VALUE, request.getFacetValues().iterator().next());
	}
	
	@Test
	public void testOnFacetRemove() {
		when(valueCount.getIsSelected()).thenReturn(true);
		facetValues.add(valueCount);
		widget.configure(mockFacet, mockOnFacetRequest);
		widget.onFacetChange(VALUE);
		verify(mockOnFacetRequest).invoke(requestCaptor.capture());
		FacetColumnValuesRequest request = requestCaptor.getValue();
		assertEquals(0, request.getFacetValues().size());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
