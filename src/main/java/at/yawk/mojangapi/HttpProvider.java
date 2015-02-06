/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author yawkat
 */
public interface HttpProvider {
    InputStream get(URL url) throws IOException;

    InputStream post(URL url, byte[] payload) throws IOException;
}
