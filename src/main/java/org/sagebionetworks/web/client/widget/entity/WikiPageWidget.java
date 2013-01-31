package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.dialog.NameAndDescriptionEditorDialog;
import org.sagebionetworks.web.shared.WikiPageKeyWrapper;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

/**
 * Lightweight widget used to resolve markdown
 * 
 * @author Jay
 *
 */
public class WikiPageWidget extends LayoutContainer {
	
	private MarkdownWidget markdownWidget;
	private PagesBrowser pagesBrowser;
	private SynapseClientAsync synapseClient;
	private NodeModelCreator nodeModelCreator;
	private IconsImageBundle iconsImageBundle;
	private AdapterFactory adapterFactory;
	private Callback callback;
	public interface Callback{
		public void pageUpdated();
	}

	
	@Inject
	public WikiPageWidget(SynapseClientAsync synapseClient, MarkdownWidget markdownWidget, PagesBrowser pagesBrowser, NodeModelCreator nodeModelCreator, IconsImageBundle iconsImageBundle, AdapterFactory adapterFactory) {
		super();
		this.synapseClient = synapseClient;
		this.markdownWidget = markdownWidget;
		this.pagesBrowser = pagesBrowser;
		this.nodeModelCreator = nodeModelCreator;
		this.iconsImageBundle = iconsImageBundle;
		this.adapterFactory = adapterFactory;
	}
	
	public void configure(final String ownerId, final String ownerType, final String wikiPageId, final Boolean canEdit, Callback callback) {
		this.removeAll();
		
		//set up callback
		if (callback != null)
			this.callback = callback;
		else 
			this.callback = new Callback() {
				@Override
				public void pageUpdated() {
				}
			};
		
		//does this have a wiki page to show?  If not, just add a button.
		WikiPageKeyWrapper key = new WikiPageKeyWrapper(ownerId, ownerType, wikiPageId);
		synapseClient.getWikiPage(key, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					if (result == null || result.length() == 0) {
						//add button to create a root wiki page, if we were trying to show the root wiki
						if (canEdit && wikiPageId == null) {
							Button insertButton = new Button(DisplayConstants.CREATE_WIKI, AbstractImagePrototype.create(iconsImageBundle.add16()));
							insertButton.setWidth(115);
							insertButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
								@Override
								public void componentSelected(ButtonEvent ce) {
									NameAndDescriptionEditorDialog.showNameDialog(DisplayConstants.LABEL_NAME, new NameAndDescriptionEditorDialog.Callback() {					
										@Override
										public void onSave(String name, String description) {
											createRootWiki(name, ownerId, ownerType);
										}
									});
								}
							});
						}
					}
					else {
						WikiPage page = nodeModelCreator.createJSONEntity(result, WikiPage.class);
						markdownWidget.setMarkdown(page.getMarkdown(), ownerId, ownerType, false);
						pagesBrowser.configure(ownerId, ownerType, DisplayConstants.PAGES, canEdit, wikiPageId);
					}
					
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(DisplayConstants.ERROR_LOADING_WIKI_FAILED+caught.getMessage());
			}
		});
	}
	
	public void createRootWiki(final String name, final String ownerId, final String ownerType) {
		WikiPage page = new WikiPage();
		page.setTitle(name);
		String wikiPageJson;
		try {
			wikiPageJson = page.writeToJSONObject(adapterFactory.createNew()).toJSONString();
			synapseClient.createWikiPage(ownerId,  ownerType, wikiPageJson, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					//handler
					callback.pageUpdated();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					showErrorMessage(DisplayConstants.ERROR_PAGE_CREATION_FAILED);
				}
			});
		} catch (JSONObjectAdapterException e) {			
			showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);		
		}
	}

	
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
