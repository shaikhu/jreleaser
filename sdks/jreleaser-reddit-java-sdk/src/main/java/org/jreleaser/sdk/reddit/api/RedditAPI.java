/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.sdk.reddit.api;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.jreleaser.infra.nativeimage.annotations.ProxyConfig;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
@ProxyConfig
public interface RedditAPI {
    @RequestLine("POST /api/v1/access_token")
    @Headers({
        "Content-Type: application/x-www-form-urlencoded",
        "User-Agent: {userAgent}"
    })
    @Body("grant_type=password&username={username}&password={password}")
    AccessTokenResponse getAccessToken(@Param("userAgent") String userAgent,
                                       @Param("username") String username,
                                       @Param("password") String password);

    @RequestLine("POST /api/submit")
    @Headers({
        "Content-Type: application/x-www-form-urlencoded",
        "User-Agent: {userAgent}",
        "Authorization: bearer {accessToken}"
    })
    @Body("api_type={api_type}&kind={kind}&sr={sr}&title={title}&text={text}&url={url}")
    SubmissionResponse submit(@Param("userAgent") String userAgent,
                              @Param("accessToken") String accessToken,
                              @Param("api_type") String apiType,
                              @Param("kind") String kind,
                              @Param("sr") String subreddit,
                              @Param("title") String title,
                              @Param("text") String text,
                              @Param("url") String url);
}