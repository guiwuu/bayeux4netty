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
 * A Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_59">Subscribe Response</a>
 *
 * Required properties: channel, successful, clientId, subscription
 * Optional properties: error, advice, ext, id, timestamp
 *
 * @author daijun
 */
public class SubscribeResponse extends BayeuxMessage implements BayeuxInterface {

    public SubscribeResponse(SubscribeRequest subscribeRequest) {
        super(subscribeRequest);
        this.subscription = subscribeRequest.subscription;
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public SubscribeResponse(String clientId, String subscription, boolean successful) {
        this.channel = "/meta/subscribe";
        this.clientId = clientId;
        this.subscription = subscription;
        this.successful = successful;
        this.timestamp = BayeuxUtil.getCurrentTime();
    }

    public static boolean isValid(BayeuxMessage bayeux) {
        if (!bayeux.channel.equals("/meta/subscribe")) {
            return false;
        }
        if (bayeux.successful == null) {
            return false;
        }
        if (bayeux.clientId == null || bayeux.clientId.length() == 0) {
            return false;
        }
        if (bayeux.subscription == null || bayeux.subscription.length() == 0) {
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

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public Boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
