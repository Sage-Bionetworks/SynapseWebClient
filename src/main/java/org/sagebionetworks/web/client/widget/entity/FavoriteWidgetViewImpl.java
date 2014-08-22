package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class FavoriteWidgetViewImpl extends FlowPanel implements FavoriteWidgetView {
	
	public static final String favoriteStarOffHtml = "<span style=\"font-size:19px; \" class=\"glyphicon glyphicon-star-empty lightGreyText\"></span>";
	public static final String favoriteStarHtml = "<span style=\"font-size:19px;color:#f7d12b\" class=\"glyphicon glyphicon-star\"></span>";
	
	private Presenter presenter;

	private Anchor favoriteAnchor;
	boolean isFavorite = false;
	private Tooltip tip;
	
	@Inject
	public FavoriteWidgetViewImpl() {
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
		tip = new Tooltip(favoriteAnchor);
		tip.setText(DisplayConstants.FAVORITES_REMINDER_TOOLTIP_MESSAGE);
		tip.setTrigger(Trigger.HOVER);
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
		// Schedule the timer to hide in 5 seconds.
		t.schedule(5000);
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
