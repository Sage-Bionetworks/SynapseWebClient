package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.view.bootstrap.table.TableData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationRowViewImpl implements FileHandleAssociationRowView, IsWidget {
	Widget w;
	interface FileHandleAssociationRowViewImplUiBinder extends UiBinder<Widget, FileHandleAssociationRowViewImpl> {}
	@UiField
	Anchor fileNameLink;
	@UiField
	Span hasAccess;
	@UiField
	Span noAccess;
	@UiField
	Text createdBy;
	@UiField
	Text createdOn;
	@UiField
	Text fileSize;
	@UiField
	Anchor removeLink;
	Presenter presenter;
	private static FileHandleAssociationRowViewImplUiBinder uiBinder = GWT
			.create(FileHandleAssociationRowViewImplUiBinder.class);
	@Inject
	public FileHandleAssociationRowViewImpl() {
		w = uiBinder.createAndBindUi(this);
		removeLink.addClickHandler(event->{
			presenter.onRemove();
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
	public void setHasAccess(boolean value) {
		hasAccess.setVisible(value);
		noAccess.setVisible(!value);
//		requestAccessLink.setVisible(!value);
	}
	
//	@Override
//	public void setEntityId(String entityId) {
//		requestAccessLink.setHref("#!AccessRequirements:TYPE=ENTITY&ID=" + entityId);
//	}
	
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
		fileNameLink.setHref("#!Synapse:"+entityId);
	}
}
