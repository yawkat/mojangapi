/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

/**
 * @author yawkat
 */
public interface RateLimiter {
    RateClaim claim() throws InterruptedException;

    default void claimNow() throws InterruptedException {
        claim().close();
    }

    public static interface RateClaim extends AutoCloseable {
        @Override
        void close();
    }
}
