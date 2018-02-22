package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Pre;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.PromptModalView;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerViewImpl implements
		EntityActionControllerView {

	public interface Binder extends
			UiBinder<Widget, EntityActionControllerViewImpl> {
	}
	Binder binder;
	@UiField
	Modal infoDialog;
	@UiField
	Pre infoDialogText;
	@UiField
	Div extraWidgetsContainer;
	@UiField
	Modal deleteWikiDialog;
	@UiField
	Span wikiPageTitle;
	@UiField
	Div wikiHeaderTreeContainer;
	@UiField
	Button deleteWikiButton;
	Span widget = new Span();
	Presenter presenter;
	Widget viewWidget = null;
	PromptModalView promptModalView;
	PortalGinInjector ginInjector;
	@Inject
	public EntityActionControllerViewImpl(Binder binder, PortalGinInjector ginInjector){
		this.binder = binder;
		this.ginInjector = ginInjector;
	}

	private void lazyConstruct() {
		if (viewWidget == null) {
			viewWidget = binder.createAndBindUi(this);
			widget.add(viewWidget);
			deleteWikiButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					deleteWikiDialog.hide();
					presenter.onConfirmDeleteWiki();	
				}
			});
			promptModalView = ginInjector.getPromptModal();
			widget.add(promptModalView);
		}
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showConfirmDeleteDialog(String message, Callback callback) {
		DisplayUtils.confirmDelete(message, callback);
	}

	@Override
	public void showInfo(String tile, String message) {
		DisplayUtils.showInfo(tile, message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void showPromptDialog(String title, PromptCallback callback) {
		lazyConstruct();
		promptModalView.configure(title, "", "OK", "");
		promptModalView.setPresenter(() -> {
			promptModalView.hide();
			callback.callback(promptModalView.getValue());
		});
		promptModalView.show();
	}
	
	@Override
	public void showInfoDialog(String header, String message) {
		lazyConstruct();
		infoDialog.setTitle(header);
		infoDialogText.setText(message);
		infoDialog.show();
	}

	@Override
	public void addWidget(IsWidget w) {
		lazyConstruct();
		extraWidgetsContainer.add(w);
	}
	@Override
	public void showDeleteWikiModal(String wikiPageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap) {
		lazyConstruct();
		//create wiki header tree (using unordered lists)
		wikiHeaderTreeContainer.clear();
		wikiPageTitle.setText(id2HeaderMap.get(wikiPageId).getTitle());
		UnorderedList ul = new UnorderedList();
		wikiHeaderTreeContainer.add(ul);
		addToDeleteListRecursive(wikiPageId, id2HeaderMap, id2ChildrenMap, ul);
		deleteWikiDialog.show();
	}
	
	private void addToDeleteListRecursive(String pageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap, UnorderedList parentUL) {
		V2WikiHeader header = id2HeaderMap.get(pageId);
		//add item to ul, and recursively add children
		ListItem li = new ListItem(header.getTitle());
		parentUL.add(li);
		if (id2ChildrenMap.containsKey(pageId)) {
			//has children
			UnorderedList ul = new UnorderedList();
			parentUL.add(ul);
			for (V2WikiHeader child : id2ChildrenMap.get(pageId)) {
				addToDeleteListRecursive(child.getId(), id2HeaderMap, id2ChildrenMap, ul);
			}
		}
	}
}
