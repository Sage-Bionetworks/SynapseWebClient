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
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.resources.ResourceLoader;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.dom.client.Element;
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
	private SynapseJSNIUtils synapseJSNIUtils;
	private WidgetRegistrar widgetRegistrar;
	private CookieProvider cookies;
	AuthenticationController authenticationController;
	GWTWrapper gwt;
	PortalGinInjector ginInjector;
	private ResourceLoader resourceLoader;
	private String md;
	private WikiPageKey wikiKey;
	private Long wikiVersionInView;
	private MarkdownWidgetView view;
	private SynapseAlert synAlert;
	
	@Inject
	public MarkdownWidget(SynapseClientAsync synapseClient,
			SynapseJSNIUtils synapseJSNIUtils, WidgetRegistrar widgetRegistrar,
			CookieProvider cookies,
			ResourceLoader resourceLoader, 
			GWTWrapper gwt,
			PortalGinInjector ginInjector,
			MarkdownWidgetView view,
			SynapseAlert synAlert) {
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
		view.setSynAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public void configure(final String md, final WikiPageKey wikiKey, final Long wikiVersionInView) {
		clear();
		this.md = md;
		this.wikiKey = wikiKey;
		this.wikiVersionInView = wikiVersionInView;
		final String uniqueSuffix = new Date().getTime() + "-" + gwt.nextRandomInt();
		synapseClient.markdown2Html(md, uniqueSuffix, DisplayUtils.isInTestWebsite(cookies), gwt.getHostPrefix(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(final String result) {
				view.callbackWhenAttached(new Callback() {
					@Override
					public void invoke() {
						if(result != null && !result.isEmpty()) {
							view.setEmptyVisible(false);
							view.setMarkdown(result);
							loadMath(wikiKey, uniqueSuffix);
							loadWidgets(wikiKey, uniqueSuffix);
							loadTableSorters();
						} else {
							view.setEmptyVisible(true);
						}
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void clear() {
		synAlert.clear();
		view.clearMarkdown();
		view.setEmptyVisible(false);
	}
	
	
	public void loadTableSorters() {
		String id = WidgetConstants.MARKDOWN_TABLE_ID_PREFIX;
		int i = 0;
		ElementWrapper table = view.getElementById(id + i);
		while (table != null) {
			synapseJSNIUtils.tablesorter(id+i);
			i++;
			table = view.getElementById(id + i);
		}
	}
	
	public void loadMath(WikiPageKey wikiKey, String suffix) {
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
	
	public Set<String> loadWidgets(WikiPageKey wikiKey, String suffix) {
		Set<String> contentTypes = new HashSet<String>();
		//look for every element that has the right format
		int i = 0;
		String currentWidgetDiv = org.sagebionetworks.markdown.constants.WidgetConstants.DIV_ID_WIDGET_PREFIX + i + suffix;
		ElementWrapper el = view.getElementById(currentWidgetDiv);
		while (el != null) {
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
							message += "<br>" + e.getMessage();
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
		configure(md, wikiKey, null);
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
}
