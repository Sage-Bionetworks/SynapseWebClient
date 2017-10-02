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
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.dom.client.Node;
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
	Widget widget;
	Presenter presenter;
	@Inject
	public EntityActionControllerViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		deleteWikiButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onConfirmDeleteWiki();	
			}
		});
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
	public void showConfirmDialog(String title, String string, Callback callback) {
		DisplayUtils.showConfirmDialog(title, string, callback);
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
	public void showPromptDialog(String prompt, PromptCallback callback) {
		Bootbox.prompt(prompt, callback);
	}
	
	@Override
	public void showInfoDialog(String header, String message) {
		infoDialog.setTitle(header);
		infoDialogText.setText(message);
		infoDialog.show();
	}

	@Override
	public void addWidget(IsWidget w) {
		extraWidgetsContainer.add(w);
	}
	@Override
	public void showDeleteWikiModal(String wikiPageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap) {
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
