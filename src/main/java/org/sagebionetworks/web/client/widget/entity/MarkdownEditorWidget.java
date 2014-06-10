package org.sagebionetworks.web.client.widget.entity;

import java.util.HashMap;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class MarkdownEditorWidget implements MarkdownEditorWidgetView.Presenter, SynapseWidgetPresenter {
	
	private SynapseClientAsync synapseClient;
	private CookieProvider cookies;
	private GWTWrapper gwt;
	public static WikiPageKey formattingGuideWikiPageKey;
	private MarkdownEditorWidgetView view;
	
	public interface CloseHandler{
		public void saveClicked();
		public void cancelClicked();
	}
	
	public interface ManagementHandler{
		public void attachmentsClicked();
		public void deleteClicked();
	}
	
	
	@Inject
	public MarkdownEditorWidget(MarkdownEditorWidgetView view, 
			SynapseClientAsync synapseClient,
			CookieProvider cookies,
			GWTWrapper gwt
			) {
		super();
		this.view = view;
		this.synapseClient = synapseClient;
		this.gwt = gwt;
		this.cookies = cookies;
		view.setPresenter(this);
	}
	
	/**
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @param markdownTextArea
	 * @param formPanel
	 * @param callback
	 * @param saveHandler if no save handler is specified, then a Save button is not shown.  If it is specified, then Save is shown and saveClicked is called when that button is clicked.
	 */
	public void configure(final WikiPageKey wikiKey,
			final TextArea markdownTextArea, 
			final LayoutContainer formPanel,
			final boolean showFieldLabel, 
			final boolean isWikiEditor,
			final WidgetDescriptorUpdatedHandler callback,
			final CloseHandler saveHandler,
			final ManagementHandler managementHandler) {
		
		if (formattingGuideWikiPageKey == null) {
			//get the page name to wiki key map
			getFormattingGuideWikiKey(new CallbackP<WikiPageKey>() {
				@Override
				public void invoke(WikiPageKey key) {
					formattingGuideWikiPageKey = key;
					view.configure(wikiKey, formattingGuideWikiPageKey, markdownTextArea, formPanel, showFieldLabel, isWikiEditor, callback, saveHandler, managementHandler);
				}
			});
		} else {
			view.configure(wikiKey, formattingGuideWikiPageKey, markdownTextArea, formPanel, showFieldLabel, isWikiEditor, callback, saveHandler, managementHandler);
		}
	}
	
	public void getFormattingGuideWikiKey(final CallbackP<WikiPageKey> callback) {
		synapseClient.getHelpPages(new AsyncCallback<HashMap<String,WikiPageKey>>() {
			@Override
			public void onSuccess(HashMap<String,WikiPageKey> result) {
				callback.invoke(result.get(WebConstants.FORMATTING_GUIDE));
			};
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
				callback.invoke(null);
			}
		});
		
	}
	
	@Override
	public void showPreview(String descriptionMarkdown, final boolean isWiki) {
	    //get the html for the markdown
	    synapseClient.markdown2Html(descriptionMarkdown, true, DisplayUtils.isInTestWebsite(cookies), gwt.getHostPrefix(), new AsyncCallback<String>() {
	    	@Override
			public void onSuccess(String result) {
	    		try {
					view.showPreviewHTML(result, isWiki);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				//preview failed
				view.showErrorMessage(DisplayConstants.PREVIEW_FAILED_TEXT + caught.getMessage());
			}
		});	
	}
	
	public void insertMarkdown(String md) {
		view.insertMarkdown(md);
	}
	
	public void deleteMarkdown(String md) {
		view.deleteMarkdown(md);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
