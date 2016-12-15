package org.sagebionetworks.web.unitclient.widget.lazyload;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapper;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperView;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;

public class LazyLoadWikiWidgetWrapperTest {
	LazyLoadWikiWidgetWrapper widget;
	@Mock
	LazyLoadWikiWidgetWrapperView mockView;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	
	@Mock
	WidgetRendererPresenter mockWikiWidget;
	@Mock
	WikiPageKey mockWikiKey;
	@Mock
	Map<String, String> mockWidgetDescriptor;
	@Mock
	Callback mockWidgetRefreshRequired;
	Long wikiVersionInView = 20L;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new LazyLoadWikiWidgetWrapper(mockView, mockLazyLoadHelper);
	}
	
	private void simulateLazyLoadEvent() {
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
	}
	
	@Test
	public void testHappyCase() {
		//configure
		widget.configure(mockWikiWidget, mockWikiKey, mockWidgetDescriptor, mockWidgetRefreshRequired, wikiVersionInView);
		verify(mockLazyLoadHelper).setIsConfigured();
		verify(mockView).showLoading();
		
		simulateLazyLoadEvent();
		verify(mockWikiWidget).configure(mockWikiKey, mockWidgetDescriptor, mockWidgetRefreshRequired, wikiVersionInView);
		verify(mockView).showWidget(any(Widget.class));
	}


}
