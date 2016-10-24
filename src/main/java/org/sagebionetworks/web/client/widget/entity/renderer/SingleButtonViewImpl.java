package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.Callback;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * View that contains a single button.
 * @author jayhodgson
 *
 */
public class SingleButtonViewImpl implements SingleButtonView {
	
	public interface Binder extends	UiBinder<Widget, SingleButtonViewImpl> {}

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
		if(loading){
			this.button.state().loading();
		}else{
			this.button.state().reset();
		}
	}
	
	@Override
	public void showConfirmDialog(String message, ConfirmCallback callback) {
		Bootbox.confirm(message, callback);
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
}
