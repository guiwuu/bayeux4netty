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

import java.util.Map;

/**
 * A singleton BayeuxMessage factory, which creates BayeuxMessage instances from
 * Map.
 * 
 * @author daijun
 */
public class BayeuxMessageFactory {

    private static final BayeuxMessageFactory instance = new BayeuxMessageFactory();

    private BayeuxMessageFactory() {
    }

    public static BayeuxMessageFactory getInstance() {
        return instance;
    }

    public BayeuxMessage create(Map<String, Object> map) {
        String channel = (String) map.get("channel");
        Object[] objs = (Object[]) map.get("supportedConnectionTypes");
        BayeuxConnection.TYPE[] supportedConnectionTypes = null;
        if (objs != null && objs.length != 0) {
            supportedConnectionTypes = new BayeuxConnection.TYPE[objs.length];
            for (int i = 0; i < objs.length; i++) {
                supportedConnectionTypes[i] = BayeuxConnection.getTypeOfValue((String) objs[i]);
            }
        }

        String clientId = map.get("clientId") == null ? null : map.get("clientId").toString();
        String connectionId = map.get("connectionId") == null ? null : map.get("connectionId").toString();
        String minimumVersion = map.get("minimumVersion") == null ? null : map.get("minimumVersion").toString();
        Boolean successful = (Boolean) map.get("successful");
        String version = map.get("version") == null ? null : map.get("version").toString();
        String subscription = (String) map.get("subscription");
        String error = (String) map.get("error");
        String connectionType = (String) map.get("connectionType");
        String id = map.get("id") == null ? null : map.get("id").toString();
        String timestamp = map.get("timestamp") == null ? null : map.get("timestamp").toString();
        BayeuxData data = map.get("data") instanceof Map ? new BayeuxData((Map) map.get("data")) : null;
        BayeuxExt ext = map.get("ext") instanceof Map ? new BayeuxExt((Map) map.get("ext")) : null;
        BayeuxAdvice advice = map.get("advice") instanceof Map ? new BayeuxAdvice((Map) map.get("advice")) : null;

        BayeuxMessage bayeux = new BayeuxMessage();
        bayeux.channel = channel;
        bayeux.supportedConnectionTypes = supportedConnectionTypes;
        bayeux.clientId = clientId;
        bayeux.connectionId = connectionId;
        bayeux.successful = successful;
        bayeux.version = version;
        bayeux.minimumVersion = minimumVersion;
        bayeux.subscription = subscription;
        bayeux.error = error;
        bayeux.connectionType = BayeuxConnection.getTypeOfValue(connectionType);
        bayeux.id = id;
        bayeux.timestamp = timestamp;
        bayeux.ext = ext;
        bayeux.advice = advice;
        bayeux.data = data;
        return bayeux;
    }
}
