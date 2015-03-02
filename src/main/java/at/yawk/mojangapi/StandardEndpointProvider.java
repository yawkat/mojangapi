/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package at.yawk.mojangapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class StandardEndpointProvider implements EndpointProvider {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    @Getter private static final EndpointProvider instance = create(JavaHttpProvider.getInstance());

    private final HttpProvider httpProvider;
    private final RateLimiter limiterNameHistory;
    private final RateLimiter limiterProfile;
    private final RateLimiter limiterProfileBatch;

    public static EndpointProvider create(HttpProvider httpProvider) {
        return create(
                httpProvider,
                new StandardRateLimiter(600, 10, TimeUnit.MINUTES, StandardRateLimiter.Mode.CONSIDERATE),
                new StandardRateLimiter(600, 10, TimeUnit.MINUTES, StandardRateLimiter.Mode.CONSIDERATE),
                new StandardRateLimiter(600, 10, TimeUnit.MINUTES, StandardRateLimiter.Mode.CONSIDERATE)
        );
    }

    public static EndpointProvider create(HttpProvider httpProvider,
                                          RateLimiter limiterNameHistory,
                                          RateLimiter limiterProfile,
                                          RateLimiter limiterProfileBatch) {
        Objects.requireNonNull(httpProvider);
        return new StandardEndpointProvider(httpProvider, limiterNameHistory, limiterProfile, limiterProfileBatch);
    }

    @Override
    public Endpoint<UUID, NameHistory> nameHistory() {
        return input -> {
            try (RateLimiter.RateClaim ignored = limiterNameHistory.claim()) {
                URL url = new URL(
                        "https://api.mojang.com/user/profiles/" + UUIDTypeAdapter.stripDashes(input) + "/names");
                try (InputStream is = httpProvider.get(url)) {
                    NameHistory.Entry[] entries = GSON.fromJson(new InputStreamReader(is, CHARSET),
                                                                NameHistory.Entry[].class);
                    return new NameHistory(Arrays.asList(entries));
                }
            }
        };
    }

    @Override
    public Endpoint<String, Profile> profileByName() {
        return new Endpoint<String, Profile>() {
            @Override
            public Profile call(String input) throws IOException, InterruptedException {
                List<Profile> batch = callBatch(Collections.singletonList(input));
                return batch.isEmpty() ? null : batch.get(0);
            }

            @Override
            public List<Profile> callBatch(Executor executor, List<String> inputs) throws InterruptedException {
                List<Profile> result = new ArrayList<>(inputs.size());
                CountDownLatch latch = new CountDownLatch((inputs.size() - 1) / 100 + 1);
                for (int i = 0; i < inputs.size(); i += 100) {
                    // let's just assume subList is concurrently readable :)
                    List<String> section = inputs.subList(i, Math.min(i + 100, inputs.size()));
                    executor.execute(() -> {
                        try {
                            List<Profile> batch = callBatch(section);
                            synchronized (result) {
                                result.addAll(batch);
                            }
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
                return result;
            }

            @Override
            public List<Profile> callBatch(List<String> inputs) throws IOException, InterruptedException {
                List<Profile> result = new ArrayList<>(inputs.size());
                for (int i = 0; i < inputs.size(); i += 100) {
                    try (RateLimiter.RateClaim ignored = limiterProfileBatch.claim()) {
                        List<String> section = inputs.subList(i, Math.min(i + 100, inputs.size()));
                        try (InputStream is = httpProvider.post(
                                new URL("https://api.mojang.com/profiles/minecraft"),
                                GSON.toJson(section).getBytes(CHARSET))) {
                            result.addAll(Arrays.asList(GSON.fromJson(
                                    new InputStreamReader(is, CHARSET),
                                    Profile[].class
                            )));
                        }
                    }
                }
                return result;
            }
        };
    }

    @Override
    public Endpoint<String, Profile> profileByName(Instant time) {
        return input -> {
            try (RateLimiter.RateClaim ignored = limiterProfile.claim()) {
                URL url = new URL(
                        "https://api.mojang.com/users/profiles/minecraft/" + URLEncoder.encode(input, "UTF-8") +
                        "?at=" + time.getEpochSecond()
                );
                try (InputStream is = httpProvider.get(url)) {
                    return GSON.fromJson(new InputStreamReader(is, CHARSET), Profile.class);
                }
            }
        };
    }
}
