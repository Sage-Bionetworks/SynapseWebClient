package org.sagebionetworks.web.unitclient.presenter;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.search.query.KeyRange;
import org.sagebionetworks.repo.model.search.query.KeyValue;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.DownloadCartPlace;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Search;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.DownloadCartPresenter;
import org.sagebionetworks.web.client.presenter.SearchPresenter;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DownloadCartPageView;
import org.sagebionetworks.web.client.view.SearchView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.shared.SearchQueryUtils;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class DownloadCartPresenterTest {

  DownloadCartPresenter presenter;

  @Mock
  DownloadCartPageView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  PopupUtilsView mockPopupUtilsView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  AccessControlListModalWidget mockACLModalWidget;

  @Mock
  Entity mockEntity;

  @Before
  public void setup() throws Exception {
    presenter =
      new DownloadCartPresenter(mockView, mockGinInjector, mockPopupUtilsView);
    when(mockGinInjector.getAccessControlListModalWidget())
      .thenReturn(mockACLModalWidget);
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    AsyncMockStubber
      .callSuccessWith(mockEntity)
      .when(mockJsClient)
      .getEntity(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(presenter);
  }

  @Test
  public void testSetPlace() {
    presenter.setPlace(new DownloadCartPlace(""));

    verify(mockView).render();
  }

  @Test
  public void testOnViewSharingSettingsClicked() {
    String testEntityId = "syn1";

    presenter.onViewSharingSettingsClicked(testEntityId);

    verify(mockJsClient).getEntity(eq(testEntityId), any(AsyncCallback.class));
    verify(mockACLModalWidget).configure(mockEntity, false);
    verify(mockACLModalWidget).showSharing(any(Callback.class));
  }

  @Test
  public void testOnViewSharingSettingsClickedFailure() {
    String testEntityId = "syn1";
    String errorMessage = "unable to get the benefactor entity";
    AsyncMockStubber
      .callFailureWith(new Exception(errorMessage))
      .when(mockJsClient)
      .getEntity(anyString(), any(AsyncCallback.class));

    presenter.onViewSharingSettingsClicked(testEntityId);

    verify(mockJsClient).getEntity(eq(testEntityId), any(AsyncCallback.class));
    verify(mockPopupUtilsView).showErrorMessage(errorMessage);
  }
}
