package org.sagebionetworks.web.test.helper;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncMockStubber {
    
	/**
	 * Create an answer that will call AsyncCallback.onSuccess();
	 * 
	 * @param data
	 * @return
	 */
    public static <T> Answer<T> createSuccessAnswer(final T data){
    	return new Answer<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Object[] args = invocationOnMock.getArguments();
                ((AsyncCallback<T>) args[args.length - 1]).onSuccess(data);
                return null;
            }
        };
    }
    
	/**
	 * Create  an Answer that will call AsyncCallback.onFailure();
	 * @param caught
	 * @return
	 */
	public static <T extends Throwable> Answer<T> createFailedAnswer(
			final T caught) {
		return new Answer<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Object[] args = invocationOnMock.getArguments();
                ((AsyncCallback<T>) args[args.length - 1]).onFailure(caught);
                return null;
            }
        };
	}
    /**
     * The resulting stubber will call the AsyncCallback.onSuccess() method in sequence for each provided value.
     * 
     * @param dataArray
     * @return
     */
    public static <T> Stubber callSuccessWith(final T...dataArray) {
    	if(dataArray == null || dataArray.length < 1){
    		// handle null
    		return  Mockito.doAnswer(createSuccessAnswer(null));
    	}
    	// each answer will be chained to the resulting stubber.
    	Stubber last = null;
    	// The rest are chained to this stubber
    	for(T data: dataArray){
    		if(last == null){
    			// Start the chain
    			last =  Mockito.doAnswer(createSuccessAnswer(data));
    		}else{
    			// extend the chain.
        		last = last.doAnswer(createSuccessAnswer(data));
    		}
    	}
    	return last;
    }
    
    /**
     * The resulting stubber will call the AsyncCallback.onFailure() method in sequence for each provided value.
     * @param caughtArray
     * @return
     */
    public static <T extends Throwable> Stubber callFailureWith(final T...caughtArray) {
    	if(caughtArray == null || caughtArray.length < 1){
    		// handle null
    		return Mockito.doAnswer(createFailedAnswer(null));
    	}
    	// each answer will be chained to the resulting stubber.
    	Stubber last = null;
    	// The rest are chained to this stubber
    	for(T caught: caughtArray){
    		if(last == null){
    			// Start the chain
    			last =  Mockito.doAnswer(createFailedAnswer(caught));
    		}else{
    			// extend the chain.
        		last = last.doAnswer(createFailedAnswer(caught));
    		}
    	}
    	return last;
    }
}
