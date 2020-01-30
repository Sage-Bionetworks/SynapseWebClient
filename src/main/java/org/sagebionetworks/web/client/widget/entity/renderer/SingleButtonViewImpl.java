package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a single button.
 * 
 * @author jayhodgson
 *
 */
public class SingleButtonViewImpl implements SingleButtonView {

	public interface Binder extends UiBinder<Widget, SingleButtonViewImpl> {
	}

	private Presenter presenter;

	@UiField
	Button button;
	Widget widget;
	@UiField
	Div extraWidgetsContainer;

	@Inject
	public SingleButtonViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClick();
			}
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setButtonEnabled(boolean enabled) {
		button.setEnabled(enabled);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setButtonText(String string) {
		button.setText(string);
	}

	@Override
	public void setButtonType(ButtonType type) {
		button.setType(type);
	}

	@Override
	public void setButtonVisible(boolean visible) {
		button.setVisible(visible);
	}

	@Override
	public void setLoading(boolean loading) {
		if (loading) {
			this.button.state().loading();
		} else {
			this.button.state().reset();
		}
	}

	@Override
	public void setDataLoadingText(String loadingText) {
		button.setDataLoadingText(loadingText);
	}

	@Override
	public void showConfirmDialog(String message, Callback callback) {
		DisplayUtils.confirm(message, callback);
	}

	@Override
	public void addWidget(Widget widget) {
		extraWidgetsContainer.add(widget);
	}


	@Override
	public void setButtonSize(ButtonSize size) {
		button.setSize(size);
	}

	@Override
	public void clearWidgets() {
		extraWidgetsContainer.clear();
	}

	@Override
	public void addStyleNames(String styles) {
		button.addStyleName(styles);
	}

	@Override
	public void setButtonIcon(IconType icon) {
		button.setIcon(icon);
	}
}
