/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author yawkat
 */
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class NameHistory {
    private static final Gson GSON = new Gson();

    public static NameHistory getHistory(UUID uuid) throws IOException {
        URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
        try (InputStream is = url.openStream()) {
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            return parseHistory(reader);
        }
    }

    public static NameHistory parseHistory(Reader reader) {
        return new NameHistory(Arrays.asList(GSON.fromJson(reader, Entry[].class)));
    }

    private final List<Entry> entries;

    public List<Entry> getHistory() {
        return Collections.unmodifiableList(entries);
    }

    @EqualsAndHashCode
    public static class Entry {
        @Getter private final String name;
        private final Long changeDate;

        public Entry(String name) {
            this.name = name;
            this.changeDate = null;
        }

        public Entry(String name, Instant changeDate) {
            this.name = name;
            this.changeDate = changeDate.toEpochMilli();
        }

        public Instant getChangeTime() {
            if (changeDate == null) {
                return null;
            }
            return Instant.ofEpochMilli(changeDate);
        }

        @Override
        public String toString() {
            if (changeDate != null) {
                return "Entry{" +
                       "name='" + name + '\'' +
                       ", changeDate=" + getChangeTime() +
                       '}';
            }
            return "Entry{" +
                   "name='" + name + '\'' +
                   '}';
        }
    }
}
