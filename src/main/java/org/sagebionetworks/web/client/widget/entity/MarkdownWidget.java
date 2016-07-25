package org.sagebionetworks.web.client.widget.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.MarkdownIt;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.cache.markdown.MarkdownCacheKey;
import org.sagebionetworks.web.client.widget.cache.markdown.MarkdownCacheValue;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownWidget implements MarkdownWidgetView.Presenter, IsWidget {
	
	private SynapseClientAsync synapseClient;
	private MarkdownIt markdownIt;
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private CookieProvider cookies;
	AuthenticationController authenticationController;
	GWTWrapper gwt;
	private SessionStorage sessionStorage;
	PortalGinInjector ginInjector;
	private ResourceLoader resourceLoader;
	private String md;
	private MarkdownWidgetView view;
	private SynapseAlert synAlert;
	private WikiPageKey wikiKey;
	private Long wikiVersionInView;
	@Inject
	public MarkdownWidget(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			CookieProvider cookies,
			ResourceLoader resourceLoader, 
			GWTWrapper gwt,
			PortalGinInjector ginInjector,
			MarkdownWidgetView view,
			SynapseAlert synAlert,
			SessionStorage sessionStorage,
			MarkdownIt markdownIt) {
		super();
		this.synapseClient = synapseClient;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.widgetRegistrar = widgetRegistrar;
		this.cookies = cookies;
		this.resourceLoader = resourceLoader;
		this.gwt = gwt;
		this.ginInjector = ginInjector;
		this.view = view;
		this.synAlert = synAlert;
		this.sessionStorage = sessionStorage;
		this.markdownIt = markdownIt;
		view.setSynAlertWidget(synAlert.asWidget());
	}
	
	/**
	 * Configure this widget using markdown only.  Note that if no wiki key is given, then some Synapse widgets may not work properly (widgets that depend on wiki attachments for example).
	 * @param md
	 */
	public void configure(final String md) {
		configure(md, null, null);
	}
	
	@Override
	public void configure(final String md, final WikiPageKey wikiKey, final Long wikiVersionInView) {
		clear();
		this.md = md;
		this.wikiKey = wikiKey;
		this.wikiVersionInView = wikiVersionInView;
		final String uniqueSuffix = new Date().getTime() + "" + gwt.nextRandomInt();
//		boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);
//		String hostPrefix = gwt.getHostPrefix();
//		final String key = getKey(md, hostPrefix, isInTestWebsite);
		//avoid cache for new md processor until it is in good shape.
//		final MarkdownCacheValue cachedValue = getValueFromCache(key);
//		if(cachedValue == null) {
			view.callbackWhenAttached(new Callback() {
				@Override
				public void invoke() {
					try {
						String result = markdownIt.markdown2Html(SafeHtmlUtils.htmlEscapeAllowEntities(md), uniqueSuffix);
						//avoid cache for new md processor until it is in good shape.
//						sessionStorage.setItem(key, getValueToCache(uniqueSuffix, result));
						loadHtml(uniqueSuffix, result);
					} catch (RuntimeException e) { //JavaScriptException
						synAlert.showError(e.getMessage());
					}
				}
			});
//		} else {
//			//used cached value
//			view.callbackWhenAttached(new Callback() {
//				@Override
//				public void invoke() {
//					loadHtml(cachedValue.getUniqueSuffix(), cachedValue.getHtml());
//				}
//			});
//		}
	}
	
	public String getKey(String md, String hostPrefix, boolean isInTestWebsite) {
		MarkdownCacheKey key = ginInjector.getMarkdownCacheKey();
		key.init(md, hostPrefix, isInTestWebsite);
		return key.toJSON();
	}
	
	public String getValueToCache(String uniqueSuffix, String html) {
		MarkdownCacheValue value = ginInjector.getMarkdownCacheValue();
		value.init(uniqueSuffix, html);
		return value.toJSON();
	}
	
	public MarkdownCacheValue getValueFromCache(String key) {
		String value = sessionStorage.getItem(key);
		if (value != null) {
			MarkdownCacheValue cacheValue = ginInjector.getMarkdownCacheValue();
			cacheValue.init(value);
			return cacheValue;
		}
		return null;
	}
	public void loadHtml(String uniqueSuffix, String result) {
		if(result != null && !result.isEmpty()) {
			view.setEmptyVisible(false);
			view.setMarkdown(result);
			boolean isInTestWebsite = DisplayUtils.isInTestWebsite(cookies);

			//TODO: remove highlightCodeBlocks call once markdown-it has replaced the server-side processor
			// (because code highlighting is does in the new parser)
			if (!isInTestWebsite) {
				synapseJSNIUtils.highlightCodeBlocks();
			}
				
			loadMath(uniqueSuffix);
			loadWidgets(wikiKey, wikiVersionInView, uniqueSuffix);	
			loadTableSorters();
		} else {
			view.setEmptyVisible(true);
		}
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		view.clearMarkdown();
		view.setEmptyVisible(false);
	}
	
	public void loadTableSorters() {
		synapseJSNIUtils.loadTableSorters();
	}
	
	public void loadMath(String suffix) {
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = WidgetConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
		ElementWrapper el = view.getElementById(currentWidgetDiv);
		while (el != null) {
			final Element loadElement = el.getElement();
			final AsyncCallback<Void> mathjaxLoadedCallback = new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					synapseJSNIUtils.processWithMathJax(loadElement);
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			};
			
			AsyncCallback<Void> mathjaxInitializedCallback = new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					resourceLoader.requires(ClientProperties.MATHJAX_LOADER_JS, mathjaxLoadedCallback);
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			};
			if (resourceLoader.isLoaded(ClientProperties.MATHJAX_JS))
				//already loaded
				synapseJSNIUtils.processWithMathJax(loadElement);
			else
				resourceLoader.requires(ClientProperties.MATHJAX_JS, mathjaxInitializedCallback);
			i++;
			currentWidgetDiv = WidgetConstants.DIV_ID_MATHJAX_PREFIX + i + suffix;
			el = view.getElementById(currentWidgetDiv);
		}
	}
	
	public Set<String> loadWidgets(WikiPageKey wikiKey, Long wikiVersionInView, String suffix) {
		Set<String> contentTypes = new HashSet<String>();
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		ElementWrapper el = view.getElementById(currentWidgetDiv);
		while (el != null) {
				el.removeAllChildren();
				//based on the contents of the element, create the correct widget descriptor and renderer
				String innerText = el.getAttribute("widgetParams");
				if (innerText != null) {
					try {
						innerText = innerText.trim();
						String contentType = widgetRegistrar.getWidgetContentType(innerText);
						Map<String, String> widgetDescriptor = widgetRegistrar.getWidgetDescriptor(innerText);
						WidgetRendererPresenter presenter = widgetRegistrar.getWidgetRendererForWidgetDescriptor(wikiKey, contentType, widgetDescriptor, 
								new Callback() {
									@Override
									public void invoke() {
										refresh();
									}
						}, wikiVersionInView);
						if (presenter == null)
							throw new IllegalArgumentException("Unable to render widget from the specified markdown.");
						view.addWidget(presenter.asWidget(), currentWidgetDiv);
						contentTypes.add(contentType);
					}catch(Throwable e) {
						//try our best to load all of the widgets. if one fails to load, then fail quietly.
						String message = innerText;
						if (e.getMessage() != null)
							message += ": " + e.getMessage();
						view.addWidget(new HTMLPanel(DisplayUtils.getMarkdownWidgetWarningHtml(message)), currentWidgetDiv);
					}
				}
			
			i++;
			currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
			el = view.getElementById(currentWidgetDiv);
		}
		return contentTypes;
	}
	
	public void loadMarkdownFromWikiPage(final WikiPageKey wikiKey, final boolean isIgnoreLoadingFailure) {
		synAlert.clear();
		//get the wiki page
		synapseClient.getV2WikiPageAsV1(wikiKey, new AsyncCallback<WikiPage>() {
			@Override
			public void onSuccess(WikiPage page) {
				wikiKey.setWikiPageId(page.getId());
				configure(page.getMarkdown(), wikiKey, null);
			}
			@Override
			public void onFailure(Throwable caught) {
				if (!isIgnoreLoadingFailure)
					synAlert.showError(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
			}
		});				
	}

	
	
	
	public void refresh() {
		configure(md, wikiKey, wikiVersionInView);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
