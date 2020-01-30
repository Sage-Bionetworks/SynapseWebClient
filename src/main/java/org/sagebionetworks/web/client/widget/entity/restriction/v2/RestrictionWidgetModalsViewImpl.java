package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.InlineRadio;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidgetView.Presenter;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestrictionWidgetModalsViewImpl implements IsWidget {
	public interface Binder extends UiBinder<Widget, RestrictionWidgetModalsViewImpl> {
	}

	Span widget = new Span();

	@UiField
	Modal imposeRestrictionModal;
	@UiField
	InlineRadio yesHumanDataRadio;
	@UiField
	InlineRadio noHumanDataRadio;
	@UiField
	Alert notSensitiveHumanDataMessage;

	@UiField
	Button imposeRestrictionOkButton;
	@UiField
	Button imposeRestrictionCancelButton;

	Presenter presenter;

	Binder binder;
	Widget viewWidget;

	@Inject
	public RestrictionWidgetModalsViewImpl(Binder binder) {
		this.binder = binder;
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void lazyConstruct() {
		if (viewWidget == null) {
			viewWidget = binder.createAndBindUi(this);
			widget.add(viewWidget);
			yesHumanDataRadio.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.yesHumanDataClicked();
				}
			});
			noHumanDataRadio.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.notHumanDataClicked();
				}
			});

			imposeRestrictionOkButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.imposeRestrictionOkClicked();
				}
			});

			imposeRestrictionCancelButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.imposeRestrictionCancelClicked();
				}
			});
		}
	}

	public void resetImposeRestrictionModal() {
		if (viewWidget != null) {
			yesHumanDataRadio.setValue(false);
			noHumanDataRadio.setValue(false);
			notSensitiveHumanDataMessage.setVisible(false);
			imposeRestrictionOkButton.setEnabled(true);
		}
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}
