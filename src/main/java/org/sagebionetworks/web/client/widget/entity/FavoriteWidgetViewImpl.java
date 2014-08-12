package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.inject.Inject;

public class FavoriteWidgetViewImpl extends FlowPanel implements FavoriteWidgetView {
	
	private static String favoriteStarOffHtml;
	private static String favoriteStarHtml;
	
	private Presenter presenter;

	final IconsImageBundle icons;

	private Anchor favoriteAnchor;
	boolean isFavorite = false;
	private InlineHTML emptyDiv;
	private Tooltip tip;
	
	@Inject
	public FavoriteWidgetViewImpl(IconsImageBundle iconsImageBundle) {
		this.icons = iconsImageBundle;
		
		favoriteStarHtml = AbstractImagePrototype.create(iconsImageBundle.star16()).getHTML();
		favoriteStarOffHtml = AbstractImagePrototype.create(iconsImageBundle.starEmpty16()).getHTML();
		
		favoriteAnchor = new Anchor();
		favoriteAnchor.setHTML(favoriteStarOffHtml);
		favoriteAnchor.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				isFavorite = isFavorite ? false : true;
				setFavoriteIcon();
				presenter.setIsFavorite(isFavorite);
			}
		});
		add(favoriteAnchor);
		emptyDiv = new InlineHTML();
		add(emptyDiv);
		tip = new Tooltip(emptyDiv);
		tip.setText("Click the star to add this to your favorites!");
		tip.setTrigger(Trigger.MANUAL);
		tip.setPlacement(Placement.RIGHT);
		add(tip);
	}

	@Override
	public void showFavoritesReminder() {
		tip.show();
		Timer t = new Timer() {
			@Override
			public void run() {
				tip.hide();
			}
		};
		// Schedule the timer to run once in 5 seconds.
		t.schedule(10000);
	}
	
	@Override
	public void showIsFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
		setFavoriteIcon();		
	}

	private void setFavoriteIcon() {
		if(isFavorite)
			favoriteAnchor.setHTML(favoriteStarHtml);
		else 
			favoriteAnchor.setHTML(favoriteStarOffHtml);
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	

}
