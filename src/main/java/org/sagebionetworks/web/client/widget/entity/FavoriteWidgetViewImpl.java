package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FavoriteWidgetViewImpl implements FavoriteWidgetView {
	public interface Binder extends UiBinder<Widget, FavoriteWidgetViewImpl> {}
	
	private Presenter presenter;

	@UiField
	Tooltip tip;
	
	@UiField
	Anchor favoriteIcon;
	@UiField
	Image loadingUI;
	
	private Widget widget;
	
	@Inject
	public FavoriteWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		
		favoriteIcon.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.favoriteClicked();
			}
		});
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
	public void showIsFavorite() {
		favoriteIcon.removeStyleName("greyText-imp");
		favoriteIcon.addStyleName("favoriteIcon");
	}
	
	@Override
	public void showIsNotFavorite() {
		favoriteIcon.removeStyleName("favoriteIcon");
		favoriteIcon.addStyleName("greyText-imp");
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
		favoriteIcon.setVisible(false);
		loadingUI.setVisible(true);
	}
	
	@Override
	public void hideLoading() {
		loadingUI.setVisible(false);
		favoriteIcon.setVisible(true);
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
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
