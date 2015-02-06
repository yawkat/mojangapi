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

/**
 * @author yawkat
 */
class InstantTypeAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toEpochMilli());
        }
    }

    @Override
    public Instant read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return null;
        } else {
            return Instant.ofEpochMilli(in.nextLong());
        }
    }
}
