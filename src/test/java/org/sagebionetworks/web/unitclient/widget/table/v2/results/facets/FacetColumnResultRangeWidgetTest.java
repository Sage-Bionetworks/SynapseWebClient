package org.sagebionetworks.web.unitclient.widget.table.v2.results.facets;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.FacetColumnRangeRequest;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultRange;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultDateRangeView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultDateRangeWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultRangeWidget;

import com.google.gwt.user.client.ui.Widget;

public class FacetColumnResultRangeWidgetTest {

	@Mock
	FacetColumnResultRangeView mockView;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	FacetColumnResultRange mockFacet;
	@Mock
	CallbackP<FacetColumnRequest> mockOnFacetRequest;
	
	@Captor
	ArgumentCaptor<FacetColumnRangeRequest> requestCaptor;
	
	FacetColumnResultRangeWidget widget;
	public static final String COLUMN_NAME = "col name";
	public static final Double COLUMN_MIN_DOUBLE = 1.0000002;
	public static final String COLUMN_MIN = Double.toString(COLUMN_MIN_DOUBLE);
	public static final Double COLUMN_MAX_DOUBLE = COLUMN_MIN_DOUBLE + 1000;
	public static final String COLUMN_MAX = Double.toString(COLUMN_MAX_DOUBLE);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FacetColumnResultRangeWidget(mockView, mockSynapseAlert);
		when(mockFacet.getColumnName()).thenReturn(COLUMN_NAME);
		when(mockFacet.getColumnMin()).thenReturn(COLUMN_MIN);
		when(mockFacet.getColumnMax()).thenReturn(COLUMN_MAX);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setSynAlert(any(Widget.class));
	}
	
	@Test
	public void testConfigureNoMinMaxSelected() {
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockSynapseAlert).clear();
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, never()).setMin(anyString());
		verify(mockView, never()).setMax(anyString());
	}
	
	@Test
	public void testConfigureMinSelected() {
		Double minSelected = 22.01;
		when(mockFacet.getSelectedMin()).thenReturn(Double.toString(minSelected));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockSynapseAlert).clear();
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).setMin(Double.toString(minSelected));
		verify(mockView, never()).setMax(anyString());
	}
	
	@Test
	public void testConfigureMaxSelected() {
		Double maxSelected = 500.01;
		when(mockFacet.getSelectedMax()).thenReturn(Double.toString(maxSelected));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockSynapseAlert).clear();
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, never()).setMin(anyString());
		verify(mockView).setMax(Double.toString(maxSelected));
	}

	@Test
	public void testOnFacetChange() {
		widget.configure(mockFacet, mockOnFacetRequest);
		
		widget.onFacetChange();
		verify(mockOnFacetRequest).invoke(requestCaptor.capture());
		FacetColumnRangeRequest request = requestCaptor.getValue();
		assertEquals(COLUMN_MIN, request.getMin());
		assertEquals(COLUMN_MAX, request.getMax());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
