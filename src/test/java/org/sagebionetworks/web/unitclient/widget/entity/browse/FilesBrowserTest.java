package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;

@RunWith(MockitoJUnitRunner.class)
public class FilesBrowserTest {

  @Mock
  FilesBrowserView mockView;

  FilesBrowser filesBrowser;

  @Before
  public void before() throws JSONObjectAdapterException {
    filesBrowser = new FilesBrowser(mockView);
  }

  @Test
  public void testConfigure() {
    String entityId = "syn123";
    filesBrowser.configure(entityId);
    verify(mockView).configure(entityId);
  }
}
