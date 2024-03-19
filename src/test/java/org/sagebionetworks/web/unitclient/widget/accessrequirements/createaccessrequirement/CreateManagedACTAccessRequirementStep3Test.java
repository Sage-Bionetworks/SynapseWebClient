package org.sagebionetworks.web.unitclient.widget.accessrequirements.createaccessrequirement;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3;
import org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep3View;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;

@RunWith(MockitoJUnitRunner.class)
public class CreateManagedACTAccessRequirementStep3Test {

  CreateManagedACTAccessRequirementStep3 widget;

  @Mock
  ModalPresenter mockModalPresenter;

  @Mock
  CreateManagedACTAccessRequirementStep3View mockView;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  ManagedACTAccessRequirement mockACTAccessRequirement;

  public static final Long AR_ID = 8765L;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    widget = new CreateManagedACTAccessRequirementStep3(mockView, mockJsClient);
    widget.setModalPresenter(mockModalPresenter);
    when(mockACTAccessRequirement.getId()).thenReturn(AR_ID);
  }

  @Test
  public void testConstruction() {
    verify(mockView).setPresenter(widget);
  }
}
