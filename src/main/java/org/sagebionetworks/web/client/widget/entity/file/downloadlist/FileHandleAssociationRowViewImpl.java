package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationRowViewImpl implements FileHandleAssociationRowView, IsWidget {
	Widget w;

	interface FileHandleAssociationRowViewImplUiBinder extends UiBinder<Widget, FileHandleAssociationRowViewImpl> {
	}

	@UiField
	Anchor fileNameLink;
	@UiField
	Span hasAccess;
	@UiField
	Span noAccess;
	@UiField
	Span tooLarge;
	@UiField
	Span externalLink;
	@UiField
	Span unsupportedFileLocation;
	@UiField
	Text createdBy;
	@UiField
	Text createdOn;
	@UiField
	Text fileSize;
	@UiField
	Anchor removeLink;
	@UiField
	Anchor requestAccessLink;
	Presenter presenter;
	private static FileHandleAssociationRowViewImplUiBinder uiBinder = GWT.create(FileHandleAssociationRowViewImplUiBinder.class);

	@Inject
	public FileHandleAssociationRowViewImpl() {
		w = uiBinder.createAndBindUi(this);
		removeLink.addClickHandler(event -> {
			presenter.onRemove();
		});
		w.addAttachHandler(event -> {
			if (event.isAttached()) {
				presenter.onViewAttached();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setCreatedBy(String value) {
		createdBy.setText(value);
	}

	@Override
	public void setCreatedOn(String value) {
		createdOn.setText(value);
	}

	@Override
	public void setFileSize(String value) {
		fileSize.setText(value);
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}

	@Override
	public void setFileName(String fileName, String entityId) {
		fileNameLink.setText(fileName);
		fileNameLink.setHref("#!Synapse:" + entityId);
	}

	@Override
	public void showHasUnmetAccessRequirements(String entityId) {
		hasAccess.setVisible(false);
		noAccess.setVisible(true);
		requestAccessLink.setVisible(true);
		requestAccessLink.setHref("#!AccessRequirements:TYPE=ENTITY&ID=" + entityId);
	}

	@Override
	public void showIsLink() {
		hasAccess.setVisible(false);
		externalLink.setVisible(true);
	}

	@Override
	public void showIsUnsupportedFileLocation() {
		hasAccess.setVisible(false);
		unsupportedFileLocation.setVisible(true);
	}

	@Override
	public void showTooLarge() {
		hasAccess.setVisible(false);
		tooLarge.setVisible(true);
	}
}
