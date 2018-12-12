package org.eclipse.microprofile.fault.tolerance.tck.asyncretry.clientserver;
/*
 *******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Retry;

import javax.enterprise.context.RequestScoped;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

/**
 * A client to demonstrate the combination of the @Retry and @Asynchronous annotations.
 *
 * @author <a href="mailto:bbaptista@tomitribe.com">Bruno Baptista</a>
 */
@RequestScoped
public class AsyncRetryClient {

    private int countInvocationsServA = 0;
    private int countInvocationsServB = 0;
    private int countInvocationsServC = 0;
    private int countInvocationsServD = 0;
    private int countInvocationsServE = 0;
    private int countInvocationsServF = 0;
    private int countInvocationsServG = 0;

    /**
     * Service A will retry a method returning a CompletionStage and configured to always completeExceptionally.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 2)
    public CompletionStage<String> serviceA() {
        countInvocationsServA++;
        // always fail
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("Simulated error"));
        return future;
    }

    /**
     * Service A will retry a method returning a CompletionStage and configured to always completeExceptionally.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Retry(maxRetries = 2)// TODO discuss implications with @Async and without @Async
    public CompletionStage<String> serviceB(final CompletionStage future) {
        countInvocationsServB++;
        // always fail
        future.toCompletableFuture().completeExceptionally(new IOException("Simulated error"));
        return future;
    }

    /**
     * Service A will retry a method returning a CompletionStage and configured to completeExceptionally twice.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 3)
    public CompletionStage<String> serviceC() {
        countInvocationsServC++;

        CompletableFuture<String> future = new CompletableFuture<>();

        if (countInvocationsServC < 3) {
            // fail 2 first invocations
            future.completeExceptionally(new IOException("Simulated error"));
        } else {
            future.complete("Success");
        }
        return future;
    }

    /**
     * Service A will retry a method returning a chained, running sequentially, CompletionStage configured to completeExceptionally twice.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 3)
    public CompletionStage<String> serviceD() {
        countInvocationsServD++;

        if (countInvocationsServD < 3) {
            // fail 2 first invocations
            return CompletableFuture.supplyAsync(doTask(null))
                .thenCompose(s -> CompletableFuture.supplyAsync(doTask("Simulated error")));
        } else {
            return CompletableFuture.supplyAsync(doTask(null))
                .thenCompose(s -> CompletableFuture.supplyAsync(doTask(null)));
        }
    }

    /**
     * Service A will retry a method returning a chained, running sequentially,
     * CompletionStage configured to completeExceptionally on all calls.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 2)
    public CompletionStage<String> serviceE() {
        countInvocationsServE++;

        // always fail
        return CompletableFuture.supplyAsync(doTask(null))
            .thenCompose(s -> CompletableFuture.supplyAsync(doTask("Simulated error")));
    }

    /**
     * Service A will retry a method returning a parallel execution of 2 CompletionStages. One of them configured to
     * always fail.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 3)
    public CompletionStage<String> serviceF() {
        countInvocationsServF++;

        if (countInvocationsServF < 3) {
            // fail 2 first invocations
            return CompletableFuture.supplyAsync(doTask(null))
                .thenCombine(CompletableFuture.supplyAsync(doTask("Simulated error")),
                    (s, s2) -> s + " then " + s2);
        } else {
            return CompletableFuture.supplyAsync(doTask(null))
                .thenCombine(CompletableFuture.supplyAsync(doTask(null)),
                    (s, s2) -> s + " then " + s2);
        }


    }

    /**
     * Service A will retry a method returning a parallel execution of 2 CompletionStages. One of them configured to
     * fail twice.
     *
     * @return a {@link CompletionStage}
     * @throws IOException
     */
    @Asynchronous
    @Retry(maxRetries = 2)
    public CompletionStage<String> serviceG() {
        countInvocationsServG++;
        // always fail
        return CompletableFuture.supplyAsync(doTask(null))
            .thenCombine(CompletableFuture.supplyAsync(doTask("Simulated error")),
                (s, s2) -> s + " then " + s2);

    }

    public int getCountInvocationsServA() {
        return countInvocationsServA;
    }

    public int getCountInvocationsServB() {
        return countInvocationsServB;
    }

    public int getCountInvocationsServC() {
        return countInvocationsServC;
    }

    public int getCountInvocationsServD() {
        return countInvocationsServD;
    }

    public int getCountInvocationsServE() {
        return countInvocationsServE;
    }

    public int getCountInvocationsServF() {
        return countInvocationsServF;
    }

    public int getCountInvocationsServG() {
        return countInvocationsServG;
    }

    private Supplier<String> doTask(final String errorMessage) {
        return () -> {
            try {
                // simulate some processing.
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException("Unplanned error: " + e);
            }
            if (nonNull(errorMessage)) {
                throw new RuntimeException(errorMessage);
            } else {
                return "Success";
            }
        };
    }
}
