package org.sagebionetworks.web.client.widget.lazyload;

import java.util.Map;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LazyLoadWikiWidgetWrapper implements IsWidget {
	// TOC is broken, and LoadMoreWidgetContainer always puts widget into block element (div). Change to
	// use LazyLoadHelper, a small load icon, and put widget in a Span.
	private LazyLoadHelper lazyLoadHelper;
	private WikiPageKey wikiKey;
	private Map<String, String> widgetDescriptor;
	private Callback widgetRefreshRequired;
	private Long wikiVersionInView;
	private LazyLoadWikiWidgetWrapperView view;
	private SynapseJSNIUtils jsniUtils;
	public final static String LOADED_EVENT_NAME = "WikiWidgetLoaded";
	private String contentTypeKey;
	WidgetRegistrar widgetRegistrar;

	@Inject
	public LazyLoadWikiWidgetWrapper(LazyLoadWikiWidgetWrapperView view, LazyLoadHelper lazyLoadHelper, SynapseJSNIUtils jsniUtils, WidgetRegistrar widgetRegistrar) {
		this.lazyLoadHelper = lazyLoadHelper;
		this.view = view;
		this.jsniUtils = jsniUtils;
		this.widgetRegistrar = widgetRegistrar;
		Callback loadDataCallback = new Callback() {
			@Override
			public void invoke() {
				lazyLoad();
			}
		};

		lazyLoadHelper.configure(loadDataCallback, view);
	}

	public void configure(String contentTypeKey, WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.contentTypeKey = contentTypeKey;
		this.wikiKey = wikiKey;
		this.widgetDescriptor = widgetDescriptor;
		this.widgetRefreshRequired = widgetRefreshRequired;
		this.wikiVersionInView = wikiVersionInView;
		lazyLoadHelper.setIsConfigured();
		view.showLoading();
	}

	public void lazyLoad() {
		widgetRegistrar.getWidgetRendererForWidgetDescriptorAfterLazyLoad(contentTypeKey, new AsyncCallback<WidgetRendererPresenter>() {
			@Override
			public void onSuccess(WidgetRendererPresenter wikiWidget) {
				wikiWidget.configure(wikiKey, widgetDescriptor, widgetRefreshRequired, wikiVersionInView);
				// use the renderer class name as a css selector (for widget usage statistics, and possibly
				// automated UI testing)
				String widgetClassName = wikiWidget.getClass().getSimpleName();
				view.showWidget(wikiWidget.asWidget(), widgetClassName);
				jsniUtils.sendAnalyticsEvent(widgetClassName, LOADED_EVENT_NAME);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
			}
		});
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
