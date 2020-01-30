package org.sagebionetworks.web.client.widget.entity;

import java.util.List;
import java.util.Map;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiPageDeleteConfirmationDialogViewImpl implements WikiPageDeleteConfirmationDialogView {
	private Presenter presenter;

	public interface WikiPageDeleteConfirmationDialogViewImplUiBinder extends UiBinder<Widget, WikiPageDeleteConfirmationDialogViewImpl> {
	}

	@UiField
	Span wikiPageTitle;
	@UiField
	Div wikiHeaderTreeContainer;
	@UiField
	Button deleteWikiButton;
	@UiField
	Button cancelDeleteWikiButton;
	Modal modal;

	@Inject
	public WikiPageDeleteConfirmationDialogViewImpl(WikiPageDeleteConfirmationDialogViewImplUiBinder binder) {
		modal = (Modal) binder.createAndBindUi(this);

		deleteWikiButton.addClickHandler(event -> {
			presenter.onDeleteWiki();
			modal.hide();
		});
		cancelDeleteWikiButton.addClickHandler(event -> {
			modal.hide();
		});
		deleteWikiButton.addDomHandler(DisplayUtils.getPreventTabHandler(deleteWikiButton), KeyDownEvent.getType());
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showModal(String wikiPageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap) {
		// create wiki header tree (using unordered lists)
		wikiHeaderTreeContainer.clear();
		wikiPageTitle.setText(id2HeaderMap.get(wikiPageId).getTitle());
		UnorderedList ul = new UnorderedList();
		wikiHeaderTreeContainer.add(ul);
		addToDeleteListRecursive(wikiPageId, id2HeaderMap, id2ChildrenMap, ul);
		modal.show();
		cancelDeleteWikiButton.setFocus(true);
	}

	private void addToDeleteListRecursive(String pageId, Map<String, V2WikiHeader> id2HeaderMap, Map<String, List<V2WikiHeader>> id2ChildrenMap, UnorderedList parentUL) {
		V2WikiHeader header = id2HeaderMap.get(pageId);
		// add item to ul, and recursively add children
		ListItem li = new ListItem(header.getTitle());
		parentUL.add(li);
		if (id2ChildrenMap.containsKey(pageId)) {
			// has children
			UnorderedList ul = new UnorderedList();
			parentUL.add(ul);
			for (V2WikiHeader child : id2ChildrenMap.get(pageId)) {
				addToDeleteListRecursive(child.getId(), id2HeaderMap, id2ChildrenMap, ul);
			}
		}
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
}
