/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import lombok.Getter;

/**
 * @author yawkat
 */
public class JavaHttpProvider implements HttpProvider {
    @Getter private static final HttpProvider instance = new JavaHttpProvider();

    @Override
    public InputStream get(URL url) throws IOException {
        return url.openStream();
    }

    @Override
    public InputStream post(URL url, byte[] payload) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        OutputStream os = connection.getOutputStream();
        os.write(payload);
        os.flush();
        return connection.getInputStream();
    }
}
