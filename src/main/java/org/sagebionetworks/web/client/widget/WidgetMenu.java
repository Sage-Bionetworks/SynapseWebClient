package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * A widget that renders the standard edit menu for widgets
 *
 * @author dburdick
 *
 */
public class WidgetMenu extends FlowPanel {

	public static int COG = 0x1;
	public static int ADD = 0x2;
	public static int EDIT = 0x4;
	public static int DELETE = 0x8;


	/**
	 * Constructor
	 */
	@Inject
	public WidgetMenu() {}

	/*
	 * Pseudo Presenter
	 */
	int mask = 0;
	private ClickHandler addHandler;
	private ClickHandler editHandler;
	private ClickHandler deleteHandler;
	boolean showCog = true;

	@Override
	public Widget asWidget() {
		if (showCog)
			mask = mask | COG;
		createMenu(mask);

		return this;
	}

	public void showAdd(ClickHandler handler) {
		this.addHandler = handler;
		mask = mask | ADD;
	}

	public void showEdit(ClickHandler handler) {
		this.editHandler = handler;
		mask = mask | EDIT;
	}

	public void showDelete(ClickHandler handler) {
		this.deleteHandler = handler;
		mask = mask | DELETE;
	}

	/**
	 * Show the cog menu which gives text versions of each icon option
	 * 
	 * @param show
	 */
	public void showCog(boolean show) {
		this.showCog = show;
	}

	public void addClicked(ClickEvent event) {
		addHandler.onClick(event);
	}

	public void editClicked(ClickEvent event) {
		editHandler.onClick(event);
	}

	public void deleteClicked(ClickEvent event) {
		deleteHandler.onClick(event);
	}

	public void createMenu(int mask) {
		// Add
		if ((mask & ADD) > 0) {
			Anchor anchor = new Anchor();
			anchor.setIcon(IconType.PLUS);
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);

		}

		// Edit
		if ((mask & EDIT) > 0) {
			Anchor anchor = new Anchor();
			anchor.setIcon(IconType.EDIT);
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					editClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);
		}

		// Delete
		if ((mask & DELETE) > 0) {
			Anchor anchor = new Anchor();
			anchor.setIcon(IconType.TIMES);
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					deleteClicked(event);
				}
			});
			anchor.addStyleName("margin-right-5");

			this.add(anchor);
		}
	}

}
