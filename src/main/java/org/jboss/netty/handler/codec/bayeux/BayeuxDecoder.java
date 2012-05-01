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

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * BayeuxDecoder should be used with HTTPDecoder, because it only suppots Bayeux
 * protocol transporting on HTTP now. When browser request with Bayeux messages,
 * BayuexDecoder only decode and validate them from content of HTTP request.
 * Then BayeuxDecoder create or map this request to a BayeuxConnection instance
 * and put the valid Bayeux messages to it. At last, BayeuxDecoder throw the
 * connection instance to higer layer, by which user can develop their
 * application logics.
 *
 * @author daijun
 */
@ChannelPipelineCoverage("one")
public class BayeuxDecoder extends OneToOneDecoder {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(BayeuxDecoder.class.getName());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof HttpRequest)) {
            return msg;
        }

        HttpRequest request = (HttpRequest) msg;
        HttpMethod method = request.getMethod();
        HttpVersion version = request.getProtocolVersion();
        StringBuilder json = new StringBuilder();
        StringBuilder jsonp = new StringBuilder();
        String paramString = null;
        if (HttpMethod.POST == method && HttpVersion.HTTP_1_1 == version && request.getContent().capacity() > 0) {//Callback polling connection type
            String charset = "utf-8";//Default unicode char encoding
            if (request.containsHeader(HttpHeaders.Names.CONTENT_TYPE)) {
                String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
                charset = contentType.indexOf("charset=") > -1 ? contentType.substring(contentType.indexOf("charset=") + 8) : charset;
                charset = isUnicode(charset) ? charset : "utf-8";
            }

            String httpContent = ((ChannelBuffer) request.getContent()).toString(charset);
            logger.debug("HTTP POST: " + httpContent);
            paramString = "?" + httpContent;
        } else if (HttpMethod.GET == method && request.getUri().length() > 0) {//Callback polling
            logger.debug("HTTP GET: " + request.getUri());
            paramString = request.getUri();

        } else {
            return msg;
        }
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(paramString);
        Map<String, List<String>> paramMaps = queryStringDecoder.getParameters();
        if (paramMaps.isEmpty() && HttpMethod.POST == method && HttpVersion.HTTP_1_1 == version) {
            json.append(paramString).deleteCharAt(0);
        } else if (!paramMaps.isEmpty()) {
            if (paramMaps.containsKey("message")) {
                for (String param : paramMaps.get("message")) {
                    json.append("&").append(param);
                }
                json.deleteCharAt(0);
            }
            if (paramMaps.containsKey("jsonp")) {
                for (String param : paramMaps.get("jsonp")) {
                	jsonp.append("&").append( param);
                }
                jsonp.deleteCharAt(0);
            }
        } else {
            return msg;
        }
        logger.info("Request:" + json);
        Object jsonObject = new JSONParser().parse(json.toString());
        if (jsonObject == null || !(jsonObject instanceof Object[]) || ((Object[]) jsonObject).length == 0) {
            return null;
        }

        Object[] jsonObjectArray = (Object[]) jsonObject;
        BayeuxMessageFactory factory = BayeuxMessageFactory.getInstance();
        BayeuxConnection connection = null;
        for (Object o : jsonObjectArray) {
            BayeuxMessage bayeux = factory.create((Map) o);
            connection = BayeuxRouter.getInstance().getConnection(bayeux.clientId);
            if (connection == null) {//New client, when handshakeing or publishing withoud connect before
                connection = new BayeuxConnection();
                connection.setClientAddress(channel.getRemoteAddress());
                connection.setServerAddress(channel.getLocalAddress());
                connection.setRequestedUri(request.getUri());
                String requestedHost = request.containsHeader(HttpHeaders.Names.HOST) ? request.getHeader(HttpHeaders.Names.HOST) : connection.getServerAddress().toString();
                connection.setRequestedHost(requestedHost);
            } else if (connection.getChannel() != channel) {//Client is polling. Replace the older HTTP connection with the new one.
                ConnectResponse[] responses = new ConnectResponse[1];
                responses[0] = new ConnectResponse(connection.getClientId(), true);
                responses[0].setId(connection.getId());
                responses[0].setTimestamp(BayeuxUtil.getCurrentTime());
                connection.send(JSONParser.toJSON(responses));
            }
            connection.setChannel(channel);
            connection.setId(bayeux.id);
            if (jsonp.length() > 0) {
                connection.setJsonp(jsonp.toString());
            }
            if (HandshakeRequest.isValid(bayeux)) {
                connection.putToUpstream(new HandshakeRequest(bayeux));
                BayeuxRouter.getInstance().addConnection(connection);
            } else if (ConnectRequest.isValid(bayeux)) {
                connection.putToUpstream(new ConnectRequest(bayeux));
            } else if (DisconnectRequest.isValid(bayeux)) {
                connection.putToUpstream(new DisconnectRequest(bayeux));
            } else if (SubscribeRequest.isValid(bayeux)) {
                connection.putToUpstream(new SubscribeRequest(bayeux));
            } else if (UnsubscribeRequest.isValid(bayeux)) {
                connection.putToUpstream(new UnsubscribeRequest(bayeux));
            } else if (PublishRequest.isValid(bayeux)) {
                connection.putToUpstream(new PublishRequest(bayeux));
            }
        }
        return connection;
    }

    private boolean isUnicode(String charset) {
        String unicodes[] = {"utf-8", "utf-16", "utf-16le", "utf-16be", "utf-32", "utf-32le", "utf-32be"};
        for (String unicode : unicodes) {
            if (unicode.equalsIgnoreCase(charset)) {
                return true;
            }
        }
        return false;
    }
}
