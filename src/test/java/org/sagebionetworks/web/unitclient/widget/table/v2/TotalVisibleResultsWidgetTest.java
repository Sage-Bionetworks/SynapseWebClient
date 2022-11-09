package org.sagebionetworks.web.unitclient.widget.table.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.sagebionetworks.repo.model.table.QueryOptions.BUNDLE_MASK_QUERY_COUNT;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityRef;
import org.sagebionetworks.repo.model.table.Dataset;
import org.sagebionetworks.repo.model.table.EntityView;
import org.sagebionetworks.repo.model.table.QueryBundleRequest;
import org.sagebionetworks.repo.model.table.QueryResultBundle;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidget;
import org.sagebionetworks.web.client.widget.table.v2.TotalVisibleResultsWidgetView;
import org.sagebionetworks.web.shared.asynch.AsynchType;

@RunWith(MockitoJUnitRunner.class)
public class TotalVisibleResultsWidgetTest {

  TotalVisibleResultsWidget widget;

  @Mock
  TotalVisibleResultsWidgetView mockView;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Mock
  AsynchronousJobTracker mockAsyncJobTracker;

  @Captor
  ArgumentCaptor<QueryBundleRequest> queryBundleRequestCaptor;

  @Captor
  ArgumentCaptor<UpdatingAsynchProgressHandler> asyncJobHandlerCaptor;

  Dataset datasetLatestVersion;
  Dataset datasetSnapshot;
  EntityView entityView;

  QueryResultBundle queryResultBundle;

  List<EntityRef> datasetItems;

  private static final String TABLE_ID = "syn123";

  @Before
  public void before() {
    widget =
      new TotalVisibleResultsWidget(
        mockView,
        mockPopupUtils,
        mockAsyncJobTracker
      );

    datasetItems =
      Arrays.asList(new EntityRef(), new EntityRef(), new EntityRef());

    datasetLatestVersion = new Dataset();
    datasetLatestVersion.setId(TABLE_ID);
    datasetLatestVersion.setItems(datasetItems);
    datasetLatestVersion.setIsLatestVersion(true);

    datasetSnapshot = new Dataset();
    datasetSnapshot.setId(TABLE_ID);
    datasetSnapshot.setItems(datasetItems);
    datasetSnapshot.setIsLatestVersion(false);

    entityView = new EntityView();
    entityView.setId(TABLE_ID);
    entityView.setIsLatestVersion(true);

    queryResultBundle = new QueryResultBundle();
  }

  @Test
  public void testDatasetWithAllItemsVisible() {
    // The count of all visible items is equal to the size of the items array
    final Long queryCount = (long) datasetItems.size();
    queryResultBundle.setQueryCount(queryCount);

    widget.configure(datasetLatestVersion);

    verify(mockAsyncJobTracker)
      .startAndTrack(
        eq(AsynchType.TableQuery),
        queryBundleRequestCaptor.capture(),
        anyInt(),
        asyncJobHandlerCaptor.capture()
      );

    // Verify that the query bundle request is correct
    assertEquals(queryBundleRequestCaptor.getValue().getEntityId(), TABLE_ID);
    assertEquals(
      queryBundleRequestCaptor.getValue().getPartMask().longValue(),
      BUNDLE_MASK_QUERY_COUNT
    );
    assertNotNull(queryBundleRequestCaptor.getValue().getQuery());

    // Invoke onSuccess
    asyncJobHandlerCaptor.getValue().onComplete(queryResultBundle);

    verify(mockView).setVisible(true);
    verify(mockView).setTotalNumberOfResults(datasetItems.size());
    // No hidden results, so should be invisible
    verify(mockView, times(2)).setNumberOfHiddenResultsVisible(false);
  }

  @Test
  public void testDatasetWithNullItems() {
    // It's possible for items to be null.
    datasetLatestVersion.setItems(null);
    final Long queryCount = 0L;
    queryResultBundle.setQueryCount(queryCount);

    widget.configure(datasetLatestVersion);

    verify(mockAsyncJobTracker)
      .startAndTrack(
        eq(AsynchType.TableQuery),
        queryBundleRequestCaptor.capture(),
        anyInt(),
        asyncJobHandlerCaptor.capture()
      );

    // Verify that the query bundle request is correct
    assertEquals(queryBundleRequestCaptor.getValue().getEntityId(), TABLE_ID);
    assertEquals(
      queryBundleRequestCaptor.getValue().getPartMask().longValue(),
      BUNDLE_MASK_QUERY_COUNT
    );
    assertNotNull(queryBundleRequestCaptor.getValue().getQuery());

    // Invoke onSuccess
    asyncJobHandlerCaptor.getValue().onComplete(queryResultBundle);

    verify(mockView).setVisible(true);
    verify(mockView)
      .setHelpMarkdown(TotalVisibleResultsWidget.DATASETS_CURRENT_VERSION_HELP);
    verify(mockView).setTotalNumberOfResults(0);
    // No hidden results, so should be invisible
    verify(mockView, times(2)).setNumberOfHiddenResultsVisible(false);
  }

  @Test
  public void testDatasetWithItemsUnavailable() {
    // The count of all visible items is less than the size of the items array
    final Long queryCount = (long) (datasetItems.size() - 1);
    queryResultBundle.setQueryCount(queryCount);

    widget.configure(datasetLatestVersion);

    verify(mockAsyncJobTracker)
      .startAndTrack(
        eq(AsynchType.TableQuery),
        queryBundleRequestCaptor.capture(),
        anyInt(),
        asyncJobHandlerCaptor.capture()
      );

    // Verify that the query bundle request is correct
    assertEquals(queryBundleRequestCaptor.getValue().getEntityId(), TABLE_ID);
    assertEquals(
      queryBundleRequestCaptor.getValue().getPartMask().longValue(),
      BUNDLE_MASK_QUERY_COUNT
    );
    assertNotNull(queryBundleRequestCaptor.getValue().getQuery());

    // Invoke onSuccess
    asyncJobHandlerCaptor.getValue().onComplete(queryResultBundle);

    verify(mockView).setVisible(true);
    verify(mockView)
      .setHelpMarkdown(TotalVisibleResultsWidget.DATASETS_CURRENT_VERSION_HELP);
    verify(mockView).setTotalNumberOfResults(datasetItems.size());
    verify(mockView)
      .setNumberOfHiddenResults(datasetItems.size() - queryCount.intValue());
    verify(mockView).setNumberOfHiddenResultsVisible(true);
  }

  @Test
  public void testDatasetSnapshotItemsUnavailable() {
    // The count of all visible items is less than the size of the items array
    final Long queryCount = (long) (datasetItems.size() - 1);
    queryResultBundle.setQueryCount(queryCount);

    widget.configure(datasetSnapshot);

    verify(mockAsyncJobTracker)
      .startAndTrack(
        eq(AsynchType.TableQuery),
        queryBundleRequestCaptor.capture(),
        anyInt(),
        asyncJobHandlerCaptor.capture()
      );

    // Verify that the query bundle request is correct
    assertEquals(queryBundleRequestCaptor.getValue().getEntityId(), TABLE_ID);
    assertEquals(
      queryBundleRequestCaptor.getValue().getPartMask().longValue(),
      BUNDLE_MASK_QUERY_COUNT
    );
    assertNotNull(queryBundleRequestCaptor.getValue().getQuery());

    // Invoke onSuccess
    asyncJobHandlerCaptor.getValue().onComplete(queryResultBundle);

    verify(mockView).setVisible(true);
    verify(mockView)
      .setHelpMarkdown(TotalVisibleResultsWidget.DATASETS_SNAPSHOT_HELP);
    verify(mockView).setTotalNumberOfResults(datasetItems.size());
    verify(mockView)
      .setNumberOfHiddenResults(datasetItems.size() - queryCount.intValue());
    verify(mockView).setNumberOfHiddenResultsVisible(true);
  }

  @Test
  public void testEntityView() {
    // We have no way to determine the maximum number of items that should be available.
    widget.configure(entityView);

    verify(mockView, times(2)).setVisible(false);
    verifyZeroInteractions(mockAsyncJobTracker);
  }
}
