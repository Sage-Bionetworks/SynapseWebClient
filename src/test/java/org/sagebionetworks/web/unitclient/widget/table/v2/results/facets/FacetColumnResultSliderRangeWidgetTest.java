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
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultSliderRangeView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultSliderRangeWidget;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import com.google.gwt.user.client.ui.Widget;

public class FacetColumnResultSliderRangeWidgetTest {

	@Mock
	FacetColumnResultSliderRangeView mockView;
	@Mock
	FacetColumnResultRange mockFacet;
	@Mock
	CallbackP<FacetColumnRequest> mockOnFacetRequest;
	
	@Captor
	ArgumentCaptor<FacetColumnRangeRequest> requestCaptor;
	@Captor
	ArgumentCaptor<Range> rangeCaptor;
	
	FacetColumnResultSliderRangeWidget widget;
	public static final String COLUMN_NAME = "col name";
	public static final Double COLUMN_MIN_DOUBLE = 1.0;
	public static final String COLUMN_MIN = Double.toString(COLUMN_MIN_DOUBLE);
	public static final Double COLUMN_MAX_DOUBLE = COLUMN_MIN_DOUBLE + 1000;
	public static final String COLUMN_MAX = Double.toString(COLUMN_MAX_DOUBLE);
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new FacetColumnResultSliderRangeWidget(mockView);
		when(mockFacet.getColumnName()).thenReturn(COLUMN_NAME);
		when(mockFacet.getColumnMin()).thenReturn(COLUMN_MIN);
		when(mockFacet.getColumnMax()).thenReturn(COLUMN_MAX);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}
	
	@Test
	public void testConfigureNoMinMaxSelected() {
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).setRange(any(Range.class));
		verify(mockView).setMin(COLUMN_MIN_DOUBLE);
		verify(mockView).setMax(COLUMN_MAX_DOUBLE);
	}
	
	@Test
	public void testConfigureMinMaxSelected() {
		Double minSelected = 22.0;
		when(mockFacet.getSelectedMin()).thenReturn(Double.toString(minSelected));
		Double maxSelected = 80.0;
		when(mockFacet.getSelectedMax()).thenReturn(Double.toString(maxSelected));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).setRange(rangeCaptor.capture());
		Range range = rangeCaptor.getValue();
		assertEquals(minSelected, range.getMinValue(), .0001);
		assertEquals(maxSelected, range.getMaxValue(), .0001);
	}
	
	@Test
	public void testStepSize() {
		when(mockFacet.getColumnMin()).thenReturn("0");
		when(mockFacet.getColumnMax()).thenReturn(Integer.toString(FacetColumnResultSliderRangeWidget.NUMBER_OF_STEPS));
		widget.configure(mockFacet, mockOnFacetRequest);
		verify(mockView).setSliderStepSize(anyDouble());
		
		double stepSize = widget.getStepSize(0, FacetColumnResultSliderRangeWidget.NUMBER_OF_STEPS);
		assertEquals(1.0, stepSize, .0001);
		stepSize = widget.getStepSize(0, FacetColumnResultSliderRangeWidget.NUMBER_OF_STEPS*2);
		assertEquals(2.0, stepSize, .0001);
		stepSize = widget.getStepSize(0, 2);
		assertEquals(1.0, stepSize, .0001);
	}
		
	@Test
	public void testOnFacetChange() {
		widget.configure(mockFacet, mockOnFacetRequest);
		Double newMin = 3.0;
		Double newMax = 6.0;
		Range newRange = new Range(newMin, newMax);
		widget.onFacetChange(newRange);
		verify(mockOnFacetRequest).invoke(requestCaptor.capture());
		FacetColumnRangeRequest request = requestCaptor.getValue();
		assertEquals(Double.toString(newMin), request.getMin());
		assertEquals(Double.toString(newMax), request.getMax());
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

}
