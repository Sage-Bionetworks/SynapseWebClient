package org.sagebionetworks.web.unitclient.widget.lazyload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapper;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadWikiWidgetWrapperView;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class LazyLoadWikiWidgetWrapperTest {
	LazyLoadWikiWidgetWrapper widget;
	@Mock
	LazyLoadWikiWidgetWrapperView mockView;
	@Mock
	LazyLoadHelper mockLazyLoadHelper;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Mock
	WidgetRendererPresenter mockWikiWidget;
	@Mock
	WikiPageKey mockWikiKey;
	@Mock
	Map<String, String> mockWidgetDescriptor;
	@Mock
	Callback mockWidgetRefreshRequired;
	@Captor
	ArgumentCaptor<AsyncCallback<WidgetRendererPresenter>> callbackCaptor;
	@Mock
	WidgetRegistrar mockWidgetRegistrar;
	Long wikiVersionInView = 20L;
	public static final String WIDGET_CONTENT_TYPE = "image";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new LazyLoadWikiWidgetWrapper(mockView, mockLazyLoadHelper, mockSynapseJSNIUtils, mockWidgetRegistrar);
	}

	private void simulateLazyLoadEvent() {
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockLazyLoadHelper).configure(captor.capture(), eq(mockView));
		captor.getValue().invoke();
	}

	@Test
	public void testHappyCase() {
		// configure
		widget.configure(WIDGET_CONTENT_TYPE, mockWikiKey, mockWidgetDescriptor, mockWidgetRefreshRequired, wikiVersionInView);
		verify(mockLazyLoadHelper).setIsConfigured();
		verify(mockView).showLoading();

		simulateLazyLoadEvent();
		verify(mockWidgetRegistrar).getWidgetRendererForWidgetDescriptorAfterLazyLoad(eq(WIDGET_CONTENT_TYPE), callbackCaptor.capture());
		callbackCaptor.getValue().onSuccess(mockWikiWidget);

		verify(mockWikiWidget).configure(mockWikiKey, mockWidgetDescriptor, mockWidgetRefreshRequired, wikiVersionInView);
		String className = mockWikiWidget.getClass().getSimpleName();
		verify(mockView).showWidget(any(Widget.class), eq(className));
		verify(mockSynapseJSNIUtils).sendAnalyticsEvent(className, LazyLoadWikiWidgetWrapper.LOADED_EVENT_NAME);
	}


}
