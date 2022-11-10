package org.sagebionetworks.web.unitclient.widget.entity.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderWidget;
import org.sagebionetworks.web.client.widget.entity.controller.EntityRefProvEntryView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceEntry;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidget;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceType;
import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceURLDialogWidget;
import org.sagebionetworks.web.client.widget.entity.controller.URLProvEntryView;
import org.sagebionetworks.web.test.helper.SelfReturningAnswer;

@RunWith(MockitoJUnitRunner.class)
public class ProvenanceListWidgetTest {

  @Mock
  ProvenanceListWidgetView mockView;

  @Mock
  PortalGinInjector mockInjector;

  EntityFinderWidget.Builder mockEntityFinderBuilder;

  @Mock
  EntityFinderWidget mockEntityFinder;

  @Mock
  ProvenanceURLDialogWidget mockUrlDialog;

  @Mock
  Reference mockRef;

  @Mock
  EntityRefProvEntryView mockEntityProvEntry;

  @Mock
  URLProvEntryView mockURLProvEntry;

  ProvenanceListWidget presenter;

  @Captor
  ArgumentCaptor<EntityFinderWidget.SelectedHandler<List<Reference>>> captor;

  String urlName = "test";
  String urlAddress = "test.com";
  String targetId = "syn123";
  Long version = 1L;
  List<ProvenanceEntry> rows = new LinkedList<ProvenanceEntry>();

  @Before
  public void setup() {
    mockEntityFinderBuilder =
      mock(EntityFinderWidget.Builder.class, new SelfReturningAnswer());
    when(mockEntityFinderBuilder.build()).thenReturn(mockEntityFinder);

    presenter =
      new ProvenanceListWidget(mockView, mockInjector, mockEntityFinderBuilder);
    presenter.setURLDialog(mockUrlDialog);
    when(mockInjector.getEntityRefEntry()).thenReturn(mockEntityProvEntry);
    when(mockInjector.getURLEntry()).thenReturn(mockURLProvEntry);
    when(mockRef.getTargetId()).thenReturn(targetId);
    when(mockRef.getTargetVersionNumber()).thenReturn(version);
    when(mockUrlDialog.getURLName()).thenReturn(urlName);
    when(mockUrlDialog.getURLAddress()).thenReturn(urlAddress);
    rows.add(mockEntityProvEntry);
    rows.add(mockURLProvEntry);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(presenter);
  }

  @Test
  public void testConfigure() {
    presenter.configure(rows, ProvenanceType.USED);
    verify(mockView, times(2))
      .addRow(
        AdditionalMatchers.or(eq(mockEntityProvEntry), eq(mockURLProvEntry))
      );
    verify(mockEntityProvEntry).setRemoveCallback(any(Callback.class));
    verify(mockURLProvEntry).setRemoveCallback(any(Callback.class));

    verify(mockEntityFinderBuilder)
      .setVersionSelection(EntityFinderWidget.VersionSelection.TRACKED);
    verify(mockEntityFinderBuilder).setSelectedMultiHandler(captor.capture());
    captor
      .getValue()
      .onSelected(Collections.singletonList(mockRef), mockEntityFinder);
    verify(mockEntityProvEntry).configure(targetId, version.toString());
    verify(mockEntityProvEntry).setAnchorTarget(anyString());
    verify(mockEntityProvEntry, times(2))
      .setRemoveCallback(any(Callback.class));
    verify(mockView, times(2)).addRow(mockEntityProvEntry);
    verify(mockEntityFinder).hide();
  }

  @Test
  public void testAddEntityRow() {
    presenter.addEntityRow();
    verify(mockEntityFinder).clearState();
    verify(mockEntityFinder).show();
  }

  @Test
  public void testAddURLRow() {
    presenter.addURLRow();
    verify(mockUrlDialog).show();
    ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
    verify(mockUrlDialog).configure(captor.capture());
    captor.getValue().invoke();
    verify(mockInjector).getURLEntry();
    verify(mockUrlDialog).getURLName();
    verify(mockUrlDialog).getURLAddress();
    verify(mockInjector).getURLEntry();
    verify(mockURLProvEntry).configure(urlName, urlAddress);
    verify(mockURLProvEntry).setAnchorTarget(urlAddress);
    verify(mockURLProvEntry).setRemoveCallback(any(Callback.class));
    verify(mockView).addRow(mockURLProvEntry);
    verify(mockUrlDialog).hide();
  }
}
