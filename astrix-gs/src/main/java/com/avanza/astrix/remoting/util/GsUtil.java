/*
 * Copyright 2014 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.avanza.astrix.remoting.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.remoting.SpaceRemotingResult;

import com.avanza.astrix.beans.core.ListenableFutureAdapter;
import com.avanza.astrix.core.AstrixRemoteResult;
import com.avanza.astrix.core.CorrelationId;
import com.avanza.astrix.core.RemoteServiceInvocationException;
import com.avanza.astrix.core.ServiceInvocationException;
import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.internal.DefaultAsyncResult;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * @author joasah
 */
public class GsUtil {
	
	@Deprecated
	public static <T> List<AstrixRemoteResult<T>> convertToAstrixRemoteResults(SpaceRemotingResult<T>[] results) {
		List<AstrixRemoteResult<T>> converted = new ArrayList<AstrixRemoteResult<T>>(results.length);
		for (SpaceRemotingResult<T> result : results) {
			if (result.getException() != null) {
				converted.add(AstrixRemoteResult.<T>failure(toRuntimeException(result.getException()), CorrelationId.undefined()));
			} else {
				converted.add(AstrixRemoteResult.successful(result.getResult()));
			}
		}
		return converted;
	}
	
	private static ServiceInvocationException toRuntimeException(Throwable exception) {
		if (exception instanceof RuntimeException) {
			return (ServiceInvocationException) exception;
		} else {
			return new RemoteServiceInvocationException("Remote service threw exception: " + exception.getMessage(), exception.getClass().getName());
		}
	}
	
	public static <T> Observable<T> toObservable(final AsyncFuture<T> response) {
		if (AsyncFutureAdapter.class.equals(response.getClass())) {
			return ((AsyncFutureAdapter<T>) response).asObservable();
		}
		return Observable.create(new Observable.OnSubscribe<T>() {
			@Override
			public void call(final Subscriber<? super T> t1) {
				response.setListener(new AsyncFutureListener<T>() {
					@Override
					public void onResult(AsyncResult<T> result) {
						if (result.getException() == null) {
							t1.onNext(result.getResult());
							t1.onCompleted();
						} else {
							t1.onError(result.getException());
						}
					}
				});
			}
		});
	}
	
	public static <T> AsyncFuture<T> toAsyncFuture(Observable<T> asyncResult) {
		return AsyncFutureAdapter.create(asyncResult);
	}
	
	public static <T> void subscribe(final AsyncFuture<T> asyncFuture, final Subscriber<? super T> t1) {
		asyncFuture.setListener(new AsyncFutureListener<T>() {
			@Override
			public void onResult(AsyncResult<T> result) {
				if (result.getException() == null) {
					t1.onNext(result.getResult());
					t1.onCompleted();
				} else {
					t1.onError(result.getException());
				}
			}
		});
	}

	public static <T> Func1<List<AsyncResult<T>>, Observable<T>> asyncResultListToObservable() {
		return new Func1<List<AsyncResult<T>>, Observable<T>>() {
			@Override
			public Observable<T> call(final List<AsyncResult<T>> asyncRresults) {
				return Observable.create(new OnSubscribe<T>() {
					@Override
					public void call(Subscriber<? super T> subscriber) {
						for (AsyncResult<T> asyncInvocationResponse : asyncRresults) {
							if (asyncInvocationResponse.getException() != null) {
								subscriber.onError(asyncInvocationResponse.getException());
								return;
							}
							subscriber.onNext(asyncInvocationResponse.getResult());
						}
						subscriber.onCompleted();
					}
				});
			}};
	}
	
	public static class AsyncFutureAdapter<T> implements AsyncFuture<T> {
		
		private final ListenableFutureAdapter<T> listenableFuture;
		
		public AsyncFutureAdapter(Observable<T> observable) {
			this.listenableFuture = new ListenableFutureAdapter<>(observable);
		}
		
		static <T> AsyncFutureAdapter<T> create(Observable<T> observable) {
			return new AsyncFutureAdapter<>(observable);
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return listenableFuture.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return listenableFuture.isCancelled();
		}

		@Override
		public boolean isDone() {
			return listenableFuture.isDone();
		}

		@Override
		public T get() throws InterruptedException, ExecutionException {
			return listenableFuture.get();
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return this.listenableFuture.get(timeout, unit);
		}

		@Override
		public void setListener(AsyncFutureListener<T> listener) {
			this.listenableFuture.setFutureListener((futureResult) -> listener.onResult(
					new DefaultAsyncResult<T>(futureResult.getResult(), asException(futureResult.getException()))));
		}
		
		private static Exception asException(Throwable error) {
			if (error instanceof Exception) {
				return (Exception) error;
			}
			return new RuntimeException(error);
		}

		public Observable<T> asObservable() {
			return this.listenableFuture.asObservable();
		}
	}
	
}
