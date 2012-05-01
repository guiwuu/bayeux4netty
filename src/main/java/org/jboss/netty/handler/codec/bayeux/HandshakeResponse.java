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

import org.jboss.netty.handler.codec.bayeux.BayeuxConnection.TYPE;

/**
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_50">Handshake Response</a>
 *
 * Required properties: channel, clientId, successful
 * Optional properties: minimumVersion, advice, ext, id, version, supportedConnectionTypes, authSuccessful, error
 *
 * @author daijun
 */
public class HandshakeResponse extends BayeuxMessage {

    public HandshakeResponse(HandshakeRequest handshakeRequest) {
        super(handshakeRequest);
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public HandshakeResponse(String clientId, boolean successful){
        this.channel="/meta/handshake";
        this.clientId=clientId;
        this.successful=successful;
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public static boolean isValid(BayeuxMessage bayeux) {
        if (!bayeux.channel.equals("/meta/handshake")) {
            return false;
        }
        if (bayeux.successful == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isValid() {
        return isValid(this);
    }

    public BayeuxAdvice getAdvice() {
        return advice;
    }

    public void setAdvice(BayeuxAdvice advice) {
        this.advice = advice;
    }

    public Boolean isAuthSuccessful() {
        return authSuccessful;
    }

    public void setAuthSuccessful(Boolean authSuccessful) {
        this.authSuccessful = authSuccessful;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    public Boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public TYPE[] getSupportedConnectionTypes() {
        return supportedConnectionTypes;
    }

    public void setSupportedConnectionTypes(TYPE[] supportedConnectionTypes) {
        this.supportedConnectionTypes = supportedConnectionTypes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
