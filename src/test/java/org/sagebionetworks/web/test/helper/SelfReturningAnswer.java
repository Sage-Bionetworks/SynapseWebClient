package org.sagebionetworks.web.test.helper;

import static org.mockito.Mockito.RETURNS_DEFAULTS;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Used to create a mock that returns itself when invoked, particularly useful for
 * mocking a builder. This feature is built-in to Mockito 2+, so this can be removed
 * on upgrade and replaced with the built-in solution.
 *
 * https://stackoverflow.com/questions/8501920/how-to-mock-a-builder-with-mockito#8530200
 */
public class SelfReturningAnswer implements Answer<Object> {

  public static final SelfReturningAnswer RETURNS_SELF =
    new SelfReturningAnswer();

  public Object answer(InvocationOnMock invocation) throws Throwable {
    Object mock = invocation.getMock();
    if (invocation.getMethod().getReturnType().isInstance(mock)) {
      return mock;
    }
    return RETURNS_DEFAULTS.answer(invocation);
  }
}
