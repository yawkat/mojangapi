/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class NameHistory {
    private final List<Entry> entries;

    public List<Entry> getHistory() {
        return Collections.unmodifiableList(entries);
    }

    @Value
    @RequiredArgsConstructor
    public static class Entry {
        @Getter private final String name;
        @Getter @Nullable private final Instant changeDate;

        public Entry(String name) {
            this(name, null);
        }
    }
}
