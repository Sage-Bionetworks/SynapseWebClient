package org.sagebionetworks.web.client.widget.entity.browse;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.SortEntityChildrenDropdownButton;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FilesBrowserViewImpl implements FilesBrowserView {

	public interface FilesBrowserViewImplUiBinder extends
			UiBinder<Widget, FilesBrowserViewImpl> {
	}

	private EntityTreeBrowser entityTreeBrowser;
	private Widget widget;
	private Presenter presenter;

	@UiField
	Div files;
	@UiField
	Div commandsContainer;
	@UiField
	AnchorListItem addToDownloadListLink;
	@UiField
	AnchorListItem programmaticOptionsLink;
	@UiField
	Div addToDownloadListContainer;
	SortEntityChildrenDropdownButton sortEntityChildrenDropdownButton;
	
	@Inject
	public FilesBrowserViewImpl(FilesBrowserViewImplUiBinder binder,
			EntityTreeBrowser entityTreeBrowser,
			SortEntityChildrenDropdownButton sortEntityChildrenDropdownButton) {
		widget = binder.createAndBindUi(this);
		this.entityTreeBrowser = entityTreeBrowser;
		Widget etbW = entityTreeBrowser.asWidget();
		etbW.addStyleName("margin-top-10");
		files.add(etbW);
		this.sortEntityChildrenDropdownButton = sortEntityChildrenDropdownButton;
		commandsContainer.add(sortEntityChildrenDropdownButton);
		sortEntityChildrenDropdownButton.setListener(entityTreeBrowser);
		sortEntityChildrenDropdownButton.setSortUI(EntityTreeBrowser.DEFAULT_SORT_BY, EntityTreeBrowser.DEFAULT_DIRECTION);
		programmaticOptionsLink.addClickHandler(event->{
			presenter.onProgrammaticDownloadOptions();
		});
		addToDownloadListLink.addClickHandler(event->{
			presenter.onAddToDownloadList();
		});
	}

	@Override
	public void configure(String entityId) {
		entityTreeBrowser.configure(entityId);
	}
	
	public void setEntitySelectedHandler(org.sagebionetworks.web.client.events.EntitySelectedHandler handler) {
		entityTreeBrowser.setEntitySelectedHandler(handler);
	};
	
	@Override
	public void setEntityClickedHandler(CallbackP<String> callback) {
		entityTreeBrowser.setEntityClickedHandler(entityId -> {
			entityTreeBrowser.setLoadingVisible(true);
			callback.invoke(entityId);
		});
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {

	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {
		entityTreeBrowser.clear();
	}
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	@Override
	public void setAddToDownloadList(IsWidget w) {
		addToDownloadListContainer.clear();
		addToDownloadListContainer.add(w);
	}
}
