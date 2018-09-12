package org.sagebionetworks.web.client.widget.entity;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidgetView.Presenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FavoriteWidget implements Presenter, IsWidget {

	private FavoriteWidgetView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private AuthenticationController authenticationController;
	private SynapseJavascriptClient jsClient;
	String entityId;
	
	public static final String FAVORITES_REMINDER = "FavoritesReminder";
	
	@Inject
	public FavoriteWidget(FavoriteWidgetView view,
			SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.view.setPresenter(this);
		this.synapseClient = synapseClient;
		fixServiceEntryPoint(synapseClient);
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.jsClient = jsClient;
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
		view.setLoadingVisible(true);
		view.setFavoriteVisible(false);
		view.setNotFavoriteVisible(false);
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
		boolean isLoggedIn = authenticationController.isLoggedIn();
		view.setLoadingVisible(isLoggedIn);
		view.setFavWidgetContainerVisible(isLoggedIn);
		if (isLoggedIn) {
			if (globalApplicationState.getFavorites() != null) {
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
		
	}

	public void updateIsFavoriteView() {
		view.setLoadingVisible(false);
		if (isFavorite(entityId)) {
			view.setFavoriteVisible(true);
			view.setNotFavoriteVisible(false);
		} else {
			view.setFavoriteVisible(false);
			view.setNotFavoriteVisible(true);
		}
	}

	private void setIsFavorite(final String entityId,
			final boolean isFavorite, final AsyncCallback<Void> callback) {
		if(isFavorite) {
			synapseClient.addFavorite(entityId, new AsyncCallback<EntityHeader>() {
				@Override
				public void onSuccess(EntityHeader result) {
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
		jsClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
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

	public void setLoadingSize(int px) {
		view.setLoadingSize(px);
	}
	
}
