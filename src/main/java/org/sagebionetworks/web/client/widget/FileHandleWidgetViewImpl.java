package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Text;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleWidgetViewImpl implements FileHandleWidgetView {

	public interface Binder extends UiBinder<Widget, FileHandleWidgetViewImpl> {
	}

	@UiField
	LoadingSpinner loadingImage;
	@UiField
	Text errorText;
	@UiField
	Anchor anchor;

	Widget widget;

	@Inject
	public FileHandleWidgetViewImpl(Binder binder) {
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
	public void setErrorText(String fileName) {
		this.errorText.setText(fileName);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void setAnchor(String fileName, String createAnchorHref) {
		this.anchor.setText(fileName);
		this.anchor.setHref(createAnchorHref);
		this.anchor.setTarget("_self");
	}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

}
