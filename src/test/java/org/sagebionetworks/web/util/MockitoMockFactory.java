package org.sagebionetworks.web.util;

import com.gwtplatform.tester.MockFactory;
import org.mockito.Mockito;

public class MockitoMockFactory implements MockFactory {

  @Override
  public <T> T mock(Class<T> classToMock) {
    // We use Mockito to create all stubs.
    return Mockito.mock(classToMock);
  }
}
