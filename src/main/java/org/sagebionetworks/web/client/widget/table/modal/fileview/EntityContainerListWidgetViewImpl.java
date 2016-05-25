package org.sagebionetworks.web.client.widget.table.modal.fileview;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Hr;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class EntityContainerListWidgetViewImpl extends Div implements EntityContainerListWidgetView {
	Presenter presenter;
	Div synAlertContainer = new Div();
	Div entitiesContainer = new Div();
	Span noContainers = new Span("<div><i>- Empty</i></div>");
	Button addButton = new Button("Add container", IconType.PLUS, new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			presenter.onAddProject();
		}
	});
	
	public EntityContainerListWidgetViewImpl() {
		add(synAlertContainer);
		add(entitiesContainer);
		noContainers.setVisible(false);
		add(noContainers);
		addButton.setVisible(false);
		addButton.addStyleName("margin-top-10");
		addButton.setSize(ButtonSize.SMALL);
		add(addButton);
	}
	
	@Override
	public void clear() {
		entitiesContainer.clear();
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void addEntity(final String id, String name, boolean showDeleteButton) {
		final Div row = new Div();
		row.addStyleName("padding-5 light-border-bottom");
		Anchor entityLink = new Anchor(name, DisplayUtils.getSynapseHistoryToken(id));
		entityLink.setTarget("_blank");
		row.add(entityLink);
		
		if (showDeleteButton) {
			Icon deleteButton = new Icon(IconType.TIMES);
			deleteButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					entitiesContainer.remove(row);
					presenter.onRemoveProject(id);
				}
			});
			deleteButton.addStyleName("imageButton text-danger margin-left-5");
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
