/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.twist;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.model.Constants.KEY_TWIST_MESSAGE;
import static org.jreleaser.model.Constants.KEY_TWIST_TITLE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TwistAnnouncer implements Announcer<org.jreleaser.model.api.announce.TwistAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.TwistAnnouncer twist;

    public TwistAnnouncer(JReleaserContext context) {
        this.context = context;
        this.twist = context.getModel().getAnnounce().getTwist();
    }

    @Override
    public org.jreleaser.model.api.announce.TwistAnnouncer getAnnouncer() {
        return twist.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return twist.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        if (twist.getChannelId() == null && twist.getThreadId() == null) {
            throw new AnnounceException(RB.$("twist.error.channel.or.thread"));
        }

        try {
            TwistSdk sdk = TwistSdk.builder(context.asImmutable())
                .accessToken(twist.getAccessToken())
                .connectTimeout(twist.getConnectTimeout())
                .readTimeout(twist.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();

            TemplateContext props = context.fullProps();
            applyTemplates(context.getLogger(), props, twist.resolvedExtraProperties());

            String message = getResolvedMessage(props);
            context.getLogger().info(RB.$("twist.message.sending"));
            context.getLogger().debug(RB.$("twist.message.content"), message);

            if (twist.getThreadId() != null) {
                sdk.createComment(twist.getThreadId(), message);
            } else {
                String title = getResolvedTitle(props);
                context.getLogger().debug(RB.$("twist.message.title"), title);
                sdk.createThread(twist.getChannelId(), title, message);
            }
        } catch (TwistSdkException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }

    private String getResolvedMessage(TemplateContext props) {
        String message;
        if (isNotBlank(twist.getMessage())) {
            message = twist.getResolvedMessage(context);
        } else {
            TemplateContext templateProps = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(templateProps, context);
            message = twist.getResolvedMessageTemplate(context, templateProps);
        }

        props.set(KEY_TWIST_MESSAGE, message);
        return applyTemplate(context.getLogger(), message, props);
    }

    private String getResolvedTitle(TemplateContext props) {
        String title = twist.getResolvedTitle(context);
        props.set(KEY_TWIST_TITLE, title);
        return applyTemplate(context.getLogger(), title, props);
    }
}
