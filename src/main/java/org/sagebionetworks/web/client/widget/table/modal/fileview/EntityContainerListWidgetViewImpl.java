package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityContainerListWidgetViewImpl implements EntityContainerListWidgetView {
	public interface Binder extends UiBinder<Widget, EntityContainerListWidgetViewImpl> {
	}

	Widget widget;
	Presenter presenter;
	@UiField
	Div synAlertContainer;
	@UiField
	Div entitiesContainer;
	@UiField
	Span noContainers;
	@UiField
	Button addButton;

	@Inject
	public EntityContainerListWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onAddProject();
			}
		});
	}

	@Override
	public void clear() {
		entitiesContainer.clear();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void addEntity(final String id, String name, boolean showDeleteButton) {
		final Div row = new Div();
		row.addStyleName("padding-5 light-border-bottom");
		Anchor entityLink = new Anchor(name, DisplayUtils.getSynapseHistoryToken(id));
		entityLink.setTarget("_blank");
		row.add(entityLink);
		Span entityIdSpan = new Span();
		entityIdSpan.setMarginLeft(3);
		entityIdSpan.setMarginRight(6);
		entityIdSpan.add(new Text("(" + id + ")"));
		row.add(entityIdSpan);

		if (showDeleteButton) {
			Icon deleteButton = new Icon(IconType.TIMES);
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					entitiesContainer.remove(row);
					presenter.onRemoveProject(id);
				}
			});
			deleteButton.addStyleName("imageButton text-danger");
			row.add(deleteButton);
		}
		entitiesContainer.add(row);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setAddButtonVisible(boolean visible) {
		addButton.setVisible(visible);
	}

	@Override
	public void setNoContainers(boolean visible) {
		noContainers.setVisible(visible);
	}
}
