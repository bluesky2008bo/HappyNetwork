package me.xiaopan.networkeasy;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class HttpRequestRunnable implements Runnable {
    private final AbstractHttpClient httpClient;
    private final HttpContext httpContext;
    private final HttpUriRequest httpUriRequest;
    private final HttpResponseHandler httpResponseHandler;

    public HttpRequestRunnable(AbstractHttpClient client, HttpContext context, HttpUriRequest request, HttpResponseHandler responseHandler) {
        this.httpClient = client;
        this.httpContext = context;
        this.httpUriRequest = request;
        this.httpResponseHandler = responseHandler;
    }

    @Override
    public void run() {
        try {
            if(httpResponseHandler != null){
                httpResponseHandler.onStart();
            }

            if(!Thread.currentThread().isInterrupted()) {
            	try {
            		HttpResponse httpResponse = httpClient.execute(httpUriRequest, httpContext);
            		if(!Thread.currentThread().isInterrupted()) {
            			if(httpResponseHandler != null) {
            				httpResponseHandler.onHandleResponse(httpResponse);
            			}
            		}
            	} catch (IOException e) {
            		if(!Thread.currentThread().isInterrupted()) {
            			throw e;
            		}
            	}
            }
        } catch (Throwable e) {
            if(httpResponseHandler != null) {
                httpResponseHandler.onException(e);
            }
        }
    }

//    private void makeRequest() throws IOException {
//        if(!Thread.currentThread().isInterrupted()) {
//        	try {
//        		HttpResponse response = httpClient.execute(httpUriRequest, httpContext);
//        		if(!Thread.currentThread().isInterrupted()) {
//        			if(httpResponseHandler != null) {
//        				httpResponseHandler.sendResponseMessage(response);
//        			}
//        		} else{
//        			//TODO: should raise InterruptedException? this block is reached whenever the request is cancelled before its response is received
//        		}
//        	} catch (IOException e) {
//        		if(!Thread.currentThread().isInterrupted()) {
//        			throw e;
//        		}
//        	}
//        }
//    }
//
//    private int executionCount;
//    private void makeRequestWithRetries() throws ConnectException {
//        // This is an additional layer of retry logic lifted from droid-fu
//        // See: https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
//        boolean retry = true;
//        IOException cause = null;
//        HttpRequestRetryHandler retryHandler = httpClient.getHttpRequestRetryHandler();
//        while (retry) {
//            try {
//                makeRequest();
//                return;
//            } catch (UnknownHostException e) {
//		        if(httpResponseHandler != null) {
//		            httpResponseHandler.sendFailureMessage(e, "can't resolve host");
//		        }
//	        	return;
//            }catch (SocketException e){
//                // Added to detect host unreachable
//                if(httpResponseHandler != null) {
//                    httpResponseHandler.sendFailureMessage(e, "can't resolve host");
//                }
//                return;
//            }catch (SocketTimeoutException e){
//                if(httpResponseHandler != null) {
//                    httpResponseHandler.sendFailureMessage(e, "socket time out");
//                }
//                return;
//            } catch (IOException e) {
//                cause = e;
//                retry = retryHandler.retryRequest(cause, ++executionCount, httpContext);
//            } catch (NullPointerException e) {
//                // there's a bug in HttpClient 4.0.x that on some occasions causes
//                // DefaultRequestExecutor to throw an NPE, see
//                // http://code.google.com/p/android/issues/detail?id=5255
//                cause = new IOException("NPE in HttpClient" + e.getMessage());
//                retry = retryHandler.retryRequest(cause, ++executionCount, httpContext);
//            }
//        }
//
//        // no retries left, crap out with exception
//        ConnectException ex = new ConnectException();
//        ex.initCause(cause);
//        throw ex;
//    }
}