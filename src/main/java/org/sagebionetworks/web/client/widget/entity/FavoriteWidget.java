package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView.Presenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FavoriteWidget implements Presenter {

	private FavoriteWidgetView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	
	String entityId;
	
	public static final String FAVORITES_REMINDER = "FavoritesReminder";
	
	@Inject
	public FavoriteWidget(FavoriteWidgetView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}
	
	public void configure(String entityId) {
		this.entityId = entityId;
		configureIsFavorite();
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void favoriteClicked() {
		setIsFavorite(!isFavorite(entityId));
	}
	
	public void setIsFavorite(boolean favorite) {
		view.showLoading();
		setIsFavorite(entityId, favorite, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				updateIsFavoriteView();
			}
			@Override
			public void onFailure(Throwable caught) {
				updateIsFavoriteView();
				view.showErrorMessage(DisplayConstants.ERROR_SAVE_FAVORITE_MESSAGE);
			}
		});
	}
	
	public void configureIsFavorite() {
		if (!authenticationController.isLoggedIn()) {
			view.hideFavoriteAndLoading();
		} else if(globalApplicationState.getFavorites() != null) {
			updateIsFavoriteView();
		} else { 
			updateStoredFavorites(new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					updateIsFavoriteView();
				}
				@Override
				public void onFailure(Throwable caught) {
				}
			});
		}
	}

	public void updateIsFavoriteView() {
		view.hideFavoriteAndLoading();
		if (isFavorite(entityId))
			view.showIsFavorite();
		else
			view.showIsNotFavorite();
	}

	private void setIsFavorite(final String entityId,
			final boolean isFavorite, final AsyncCallback<Void> callback) {
		if(isFavorite) {
			synapseClient.addFavorite(entityId, new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {
					updateStoredFavorites(callback);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});
		} else {
			synapseClient.removeFavorite(entityId, new AsyncCallback<Void>() {
				@Override
				public void onSuccess(Void result) {
					updateStoredFavorites(callback);
				}
				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}
			});			
		}
	}

	private void updateStoredFavorites(final AsyncCallback<Void> callback) {
		synapseClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> favorites) {
				globalApplicationState.setFavorites(favorites);
				callback.onSuccess(null);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	private boolean isFavorite(String entityId) {
		List<EntityHeader> favorites = globalApplicationState.getFavorites();
		if(favorites != null) {
			for(EntityHeader eh : favorites) {
				if(eh.getId().equals(entityId))
					return true;
			}
		}
		return false;
	}

	
}
