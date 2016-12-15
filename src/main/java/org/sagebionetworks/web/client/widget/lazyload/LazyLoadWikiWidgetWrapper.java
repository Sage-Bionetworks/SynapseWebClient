package org.sagebionetworks.web.client.widget.lazyload;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LazyLoadWikiWidgetWrapper implements IsWidget {
	// TOC is broken, and LoadMoreWidgetContainer always puts widget into block element (div). Change to use LazyLoadHelper, a small load icon, and put widget in a Span.
	private LazyLoadHelper lazyLoadHelper;
	private WidgetRendererPresenter wikiWidget;
	private WikiPageKey wikiKey; 
	private Map<String, String> widgetDescriptor; 
	private Callback widgetRefreshRequired; 
	private Long wikiVersionInView;
	private LazyLoadWikiWidgetWrapperView view;
	
	@Inject
	public LazyLoadWikiWidgetWrapper(LazyLoadWikiWidgetWrapperView view, LazyLoadHelper lazyLoadHelper) {
		this.lazyLoadHelper = lazyLoadHelper;
		this.view = view;
		this.lazyLoadHelper = lazyLoadHelper;
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				lazyLoad();
			}
		};
		
		lazyLoadHelper.configure(loadDataCallback, view);
	}
	public void configure(WidgetRendererPresenter wikiWidget, WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView){
		this.wikiWidget = wikiWidget;
		this.wikiKey = wikiKey;
		this.widgetDescriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		this.wikiVersionInView = wikiVersionInView;
		lazyLoadHelper.setIsConfigured();
		view.showLoading();
	}
	
	public void lazyLoad() {
		wikiWidget.configure(wikiKey, widgetDescriptor, widgetRefreshRequired, wikiVersionInView);
		view.showWidget(wikiWidget.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
