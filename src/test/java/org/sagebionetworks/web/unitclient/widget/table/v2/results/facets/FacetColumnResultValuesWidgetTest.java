package org.sagebionetworks.web.unitclient.widget.table.v2.results.facets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.repo.model.table.TableConstants.NULL_VALUE_KEYWORD;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.repo.model.table.FacetColumnResultValueCount;
import org.sagebionetworks.repo.model.table.FacetColumnResultValues;
import org.sagebionetworks.repo.model.table.FacetColumnValuesRequest;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.UserIdCellRenderer;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesView;
import org.sagebionetworks.web.client.widget.table.v2.results.facets.FacetColumnResultValuesWidget;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

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
	@Mock
	PortalGinInjector mockPortalGinInjector;
	@Mock
	UserIdCellRenderer mockUserBadge;

	@Mock
	EntityIdCellRenderer mockEntityIdCellRenderer;
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
		widget = new FacetColumnResultValuesWidget(mockView, mockPortalGinInjector);
		when(mockFacet.getColumnName()).thenReturn(COLUMN_NAME);
		facetValues = new ArrayList<FacetColumnResultValueCount>();
		when(mockFacet.getFacetValues()).thenReturn(facetValues);

		when(nullValueCount.getValue()).thenReturn(NULL_VALUE_KEYWORD);
		when(nullValueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(nullValueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);

		when(emptyValueCount.getValue()).thenReturn("");
		when(emptyValueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(emptyValueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);

		when(valueCount.getValue()).thenReturn(VALUE);
		when(valueCount.getCount()).thenReturn(DEFAULT_COUNT);
		when(valueCount.getIsSelected()).thenReturn(DEFAULT_SELECTED);
		when(mockPortalGinInjector.getUserIdCellRenderer()).thenReturn(mockUserBadge);
		when(mockPortalGinInjector.getEntityIdCellRenderer()).thenReturn(mockEntityIdCellRenderer);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
	}

	@Test
	public void testConfigureNullValue() {
		facetValues.add(nullValueCount);
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).addValue(eq(DEFAULT_SELECTED), any(Widget.class), eq(DEFAULT_COUNT), eq(NULL_VALUE_KEYWORD));
		verify(mockView).setShowAllButtonVisible(false);
	}

	@Test
	public void testConfigureEmptyValue() {
		facetValues.add(emptyValueCount);
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).addValue(eq(DEFAULT_SELECTED), any(Widget.class), eq(DEFAULT_COUNT), eq(""));
		verify(mockView).setShowAllButtonVisible(false);
	}

	@Test
	public void testConfigureValue() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView).getSpanWithText(VALUE);
		verify(mockView).addValue(eq(DEFAULT_SELECTED), any(Widget.class), eq(DEFAULT_COUNT), eq(VALUE));
		verify(mockView).setShowAllButtonVisible(false);
	}

	@Test
	public void testConfigureUserIdValue() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, ColumnType.USERID, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockPortalGinInjector).getUserIdCellRenderer();
		verify(mockUserBadge).setValue(eq(VALUE), any(ClickHandler.class));
		verify(mockView).addValue(eq(DEFAULT_SELECTED), any(Widget.class), eq(DEFAULT_COUNT), eq(VALUE));
		verify(mockView).setShowAllButtonVisible(false);
	}


	@Test
	public void testConfigureEntityIdValue() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, ColumnType.ENTITYID, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockPortalGinInjector).getEntityIdCellRenderer();
		verify(mockEntityIdCellRenderer).setValue(eq(VALUE), any(ClickHandler.class), eq(true));
		verify(mockView).addValue(eq(DEFAULT_SELECTED), any(Widget.class), eq(DEFAULT_COUNT), eq(VALUE));
		verify(mockView).setShowAllButtonVisible(false);
	}

	@Test
	public void testOverflow() {
		int numberOfFacets = FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES + 20;
		for (int i = 0; i < numberOfFacets; i++) {
			FacetColumnResultValueCount valuesCount = Mockito.mock(FacetColumnResultValueCount.class);
			facetValues.add(valuesCount);
		}
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, times(FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES)).addValue(anyBoolean(), any(Widget.class), anyLong(), anyString());
		verify(mockView, times(numberOfFacets - FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES)).addValueToOverflow(anyBoolean(), any(Widget.class), anyLong(), anyString());
		verify(mockView).setShowAllButtonText(FacetColumnResultValuesWidget.SHOW_ALL + numberOfFacets);
		verify(mockView).setShowAllButtonVisible(true);
	}

	@Test
	public void testSelectedFacetInOverflow() {
		int numberOfFacets = FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES + 20;
		for (int i = 0; i < numberOfFacets; i++) {
			FacetColumnResultValueCount valuesCount = Mockito.mock(FacetColumnResultValueCount.class);
			// the FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES facet is selected (boundary case)
			when(valuesCount.getIsSelected()).thenReturn(i == FacetColumnResultValuesWidget.MAX_VISIBLE_FACET_VALUES);
			facetValues.add(valuesCount);
		}
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
		verify(mockView).setColumnName(COLUMN_NAME);
		verify(mockView, times(numberOfFacets)).addValue(anyBoolean(), any(Widget.class), anyLong(), anyString());
		verify(mockView).setShowAllButtonVisible(false);
	}

	@Test
	public void testOnFacetAdd() {
		facetValues.add(valueCount);
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
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
		widget.configure(mockFacet, ColumnType.INTEGER, mockOnFacetRequest);
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
