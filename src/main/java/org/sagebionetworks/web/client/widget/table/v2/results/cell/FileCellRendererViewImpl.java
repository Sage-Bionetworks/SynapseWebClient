package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererViewImpl implements FileCellRendererView {

	public interface Binder extends UiBinder<Widget, FileCellRendererViewImpl> {
	}

	@UiField
	Span loadingUI;
	@UiField
	Text errorText;
	@UiField
	Anchor anchor;
	@UiField
	Tooltip tooltip;
	Widget widget;

	@Inject
	public FileCellRendererViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingUI.setVisible(visible);
	}

	@Override
	public void setErrorText(String fileName) {
		this.errorText.setText(fileName);
		anchor.setVisible(false);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public void setAnchor(String fileName, String createAnchorHref) {
		anchor.setVisible(true);
		this.anchor.setText(fileName);
		this.anchor.setHref(createAnchorHref);
		this.anchor.setTarget("_self");
	}

	@Override
	public void setTooltip(Long contentSize) {
		String friendlySize = DisplayUtils.getFriendlySize(contentSize, true).replace(" ", "&nbsp;");
		tooltip.setTitle("<strong>Size:</strong>&nbsp;" + friendlySize);
	}
}
