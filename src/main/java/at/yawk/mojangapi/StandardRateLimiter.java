/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author yawkat
 */
public class StandardRateLimiter implements RateLimiter {
    private final long intervalMillis;
    private final BlockingQueue<Slot> free = new LinkedBlockingQueue<>();

    public StandardRateLimiter(int entryCount, long clearInterval, TimeUnit unit, Mode mode) {
        this(entryCount, unit.toMillis(clearInterval), mode);
    }

    private StandardRateLimiter(int entryCount, long clearInterval, Mode mode) {
        int count;
        switch (mode) {
        case GREEDY:
            count = entryCount;
            intervalMillis = clearInterval;
            break;
        case CONSIDERATE:
            count = 1;
            intervalMillis = (long) Math.ceil((double) clearInterval / entryCount);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported mode " + mode);
        }

        Stream.generate(Slot::new).limit(count).forEach(free::add);
    }

    @Override
    public synchronized RateClaim claim() throws InterruptedException {
        Slot slot = free.poll(1, TimeUnit.DAYS);
        long remaining = slot.lastUse - System.currentTimeMillis() + intervalMillis;
        if (remaining > 0) {
            Thread.sleep(remaining);
        }
        return () -> {
            slot.lastUse = System.currentTimeMillis();
            free.offer(slot);
        };
    }

    private class Slot {
        private long lastUse;
    }

    public static enum Mode {
        GREEDY,
        CONSIDERATE,
    }
}
