/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import java.util.UUID;

/**
 * @author yawkat
 */
public interface EndpointProvider {
    public static EndpointProvider getProvider() {
        return StandardEndpointProvider.getInstance();
    }

    Endpoint<UUID, NameHistory> nameHistory();

    Endpoint<String, Profile> profileByName();
}
