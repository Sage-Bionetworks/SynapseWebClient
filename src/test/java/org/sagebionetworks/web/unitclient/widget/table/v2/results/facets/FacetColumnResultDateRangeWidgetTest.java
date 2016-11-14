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

import com.google.gwt.user.client.ui.Widget;

public class FacetColumnResultDateRangeWidgetTest {

	@Mock
	FacetColumnResultDateRangeView mockView;
	@Mock
	SynapseAlert mockSynapseAlert;
	@Mock
	FacetColumnResultRange mockFacet;
	@Mock
	CallbackP<FacetColumnRequest> mockOnFacetRequest;
	
	@Captor
	ArgumentCaptor<FacetColumnRangeRequest> requestCaptor;
	
	FacetColumnResultDateRangeWidget widget;
	public static final String COLUMN_NAME = "col name";
	public static final Long COLUMN_MIN_TIME = new Date().getTime();
	public static final String COLUMN_MIN = Long.toString(COLUMN_MIN_TIME);
	public static final Long COLUMN_MAX_TIME = new Date().getTime() + 60000;
	public static final String COLUMN_MAX = Long.toString(COLUMN_MAX_TIME);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FacetColumnResultDateRangeWidget(mockView, mockSynapseAlert);
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
		verify(mockView, never()).setMin(any(Date.class));
		verify(mockView, never()).setMax(any(Date.class));
	}
	
	@Test
	public void testConfigureMinSelected() {
		Date now = new Date();
		when(mockFacet.getSelectedMin()).thenReturn(Long.toString(now.getTime()));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockSynapseAlert).clear();
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).setMin(now);
		verify(mockView, never()).setMax(any(Date.class));
	}
	
	@Test
	public void testConfigureMaxSelected() {
		Date now = new Date();
		when(mockFacet.getSelectedMax()).thenReturn(Long.toString(now.getTime()));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockSynapseAlert).clear();
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, never()).setMin(any(Date.class));
		verify(mockView).setMax(now);
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
