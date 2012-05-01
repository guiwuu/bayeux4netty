/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.bayeux;

import static org.jboss.netty.channel.Channels.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * BayeuxEncoder get BayeuxConnection instance from higher layers.
 * It first process every request in receiving queue, which still have not been
 * handled by higher layers.
 *
 * @author daijun
 */
@ChannelPipelineCoverage("one")
public class BayeuxEncoder implements ChannelDownstreamHandler {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(BayeuxEncoder.class.getName());

    @Override
    public void handleDownstream(
            ChannelHandlerContext ctx, ChannelEvent evt) throws Exception {
        if (!(evt instanceof MessageEvent)) {
            ctx.sendDownstream(evt);
            return;
        }

        MessageEvent e = (MessageEvent) evt;
        Object originalMessage = e.getMessage();
        Object encodedMessage = encode(ctx, e.getChannel(), originalMessage);
        if (originalMessage == encodedMessage) {
            ctx.sendDownstream(evt);
        } else if (encodedMessage != null) {
            write(ctx, e.getFuture(), encodedMessage, e.getRemoteAddress());
        }
    }

    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof BayeuxConnection) {
            BayeuxConnection connection = (BayeuxConnection) msg;
            BayeuxMessage bayeux=connection.getFromUpstream();
            while(bayeux!=null) {
                if (bayeux instanceof HandshakeRequest) {
                    connection.handshake((HandshakeRequest) bayeux);
                } else if (bayeux instanceof ConnectRequest) {
                    connection.connect((ConnectRequest) bayeux);
                } else if (bayeux instanceof DisconnectRequest) {
                    connection.disconnect((DisconnectRequest) bayeux);
                } else if (bayeux instanceof SubscribeRequest) {
                    connection.subscribe((SubscribeRequest) bayeux);
                } else if (bayeux instanceof UnsubscribeRequest) {
                    connection.unsubscribe((UnsubscribeRequest) bayeux);
                } else if (bayeux instanceof PublishRequest) {
                     connection.publish((PublishRequest) bayeux);
                }
                bayeux=connection.getFromUpstream();
            }
            connection.flush();
            return null;
        } else if (msg instanceof String) {
            String responseContent = (String) msg;
            logger.info("Response:" + responseContent);
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent, "utf-8");

            // Build response object.
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.setContent(buf);
            response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/json; charset=UTF-8");
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
            return response;
        } else {
            return msg;
        }
    }

}
