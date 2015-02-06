/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

/**
 * @author yawkat
 */
class UUIDTypeAdapter extends TypeAdapter<UUID> {
    @Override
    public void write(JsonWriter out, UUID value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(stripDashes(value));
        }
    }

    static String stripDashes(UUID value) {
        return value.toString().replace("-", "");
    }

    @Override
    public UUID read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return null;
        } else {
            StringBuilder s = new StringBuilder(in.nextString());
            s.insert(8 + 4 * 3, '-');
            s.insert(8 + 4 * 2, '-');
            s.insert(8 + 4, '-');
            s.insert(8, '-');
            return UUID.fromString(s.toString());
        }
    }
}
