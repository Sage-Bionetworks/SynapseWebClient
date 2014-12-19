package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererViewImpl implements FileCellRendererView {
	
	public interface Binder extends UiBinder<Widget, FileCellRendererViewImpl> {}
	
	@UiField
	Image loadingImage;
	@UiField
	Text fileName;
	
	Widget widget;

	@Inject
	public FileCellRendererViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingImage.setVisible(visible);
	}

	@Override
	public void setFileName(String fileName) {
		this.fileName.setText(fileName);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

}
