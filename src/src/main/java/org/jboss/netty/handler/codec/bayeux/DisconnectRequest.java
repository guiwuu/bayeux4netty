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

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_55">Disconnect Request</a>
 *
 * Required properties: channel, clientId
 * Optional properties: ext, id
 *
 * @author daijun
 */
public class DisconnectRequest extends BayeuxMessage implements BayeuxInterface {

    public DisconnectRequest(BayeuxMessage bayeux) {
        super(bayeux);
    }

    public DisconnectRequest(String clientId) {
        this.channel="/meta/disconnect";
        this.clientId = clientId;
    }

    public static boolean isValid(BayeuxMessage bayeux) {
        if (!bayeux.channel.equals("/meta/disconnect")) {
            return false;
        }
        if (bayeux.clientId == null || bayeux.clientId.length() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isValid() {
        return isValid(this);
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public BayeuxExt getExt() {
        return ext;
    }

    public void setExt(BayeuxExt ext) {
        this.ext = ext;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
