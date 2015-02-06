/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author yawkat
 */
public interface Endpoint<I, O> {
    O call(I input) throws IOException, InterruptedException;

    @SuppressWarnings("unchecked")
    default List<O> callBatch(Executor executor, List<I> inputs) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(inputs.size());

        O[] out = (O[]) new Object[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            int index = i;
            I input = inputs.get(i);
            executor.execute(() -> {
                try {
                    O value = call(input);
                    out[index] = value;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        return Arrays.asList(out);
    }

    default List<O> callBatch(List<I> inputs) throws IOException, InterruptedException {
        List<O> result = new ArrayList<>(inputs.size());
        for (I input : inputs) {
            result.add(call(input));
        }
        return result;
    }
}
