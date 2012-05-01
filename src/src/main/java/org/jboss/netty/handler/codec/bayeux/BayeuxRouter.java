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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * BayeuxRouter is another core part of Bayeux. It's a singleton class holding
 * all connections with routing table and their subscriptions.
 * 
 * @author daijun
 */
public class BayeuxRouter {

    private final Map<String, BayeuxConnection> connections = new HashMap<String, BayeuxConnection>();
    private final Map<String, List<BayeuxConnection>> subscriptions = new HashMap<String, List<BayeuxConnection>>();
    private static final BayeuxRouter instance = new BayeuxRouter();

    private BayeuxRouter() {
    }

    /**
     * Return the instance of BayeuxRouter.
     *
     * @return
     */
    public static synchronized BayeuxRouter getInstance() {
        return instance;
    }

    /**
     * Get a connection instance by client id. If router dose't exist a
     * connection by the cilent id, return null.
     *
     * @param clientId
     * @return
     */
    public BayeuxConnection getConnection(String clientId) {
        return connections.get(clientId);
    }

    /**
     * Clear router's connections and subscriptions.
     */
    public void clear() {
        subscriptions.clear();
        for (Entry<String, BayeuxConnection> entry : connections.entrySet()) {
            entry.getValue().close();
        }
        connections.clear();
    }

    /**
     * Add a connection to router.
     *
     * @param connection
     */
    public void addConnection(BayeuxConnection connection) {
        String clientId = null;
        do {
            clientId = BayeuxUtil.generateUUID();
        } while (connections.containsKey(clientId));
        connection.setClientId(clientId);
        connections.put(clientId, connection);
    }

    /**
     * Remove a connection from router as well as it's subscriptions.
     *
     * @param connection
     * @return
     */
    public boolean removeConnection(BayeuxConnection connection) {
        if (getConnection(connection.getClientId()) != null) {
            for (String subscription : connection.getSubscriptions()) {
                removeListener(subscription, connection);
            }
            connections.remove(connection.getClientId());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a new subscription to a connection. Return false if the connection
     * already subscribes it, and return true if not.
     *
     * @param subscription
     * @param connection
     * @return
     */
    public boolean addListener(String subscription, BayeuxConnection connection) {
        if (subscription.endsWith("/")) {
            subscription = subscription.substring(0, (subscription.length() - 1));
        }
        List<BayeuxConnection> listeners = subscriptions.get(subscription);
        if (listeners == null) {
            listeners = new ArrayList<BayeuxConnection>();
            subscriptions.put(subscription, listeners);
        }

        if (listeners.contains(connection)) {
            return false;
        }

        listeners.add(connection);
        return true;
    }

    /**
     * Remove a subscription from a connection and return true.
     *
     * @param subscription
     * @param connection
     * @return
     */
    public boolean removeListener(String subscription, BayeuxConnection connection) {
        for (String sub : BayeuxUtil.prefixMatch(subscription, subscriptions.keySet())) {
            List<BayeuxConnection> listeners = subscriptions.get(sub);
            if (listeners.contains(connection)) {
                listeners.remove(connection);
                if (listeners.isEmpty()) {
                    subscriptions.remove(subscription);
                }
            }
        }
        return true;
    }

    /**
     * Returns current number of connections.
     *
     * @return
     */
    public int countConnections() {
        return connections.size();
    }

    /**
     * Process PublishRequest and send its data to subscribing clients by router.
     * 
     * @deprecated use publish(BayeuxConnection publisher, DeliverEvent deliver) instead
     * @param publishRequest
     * @return
     */
    public boolean onPublish(PublishRequest publishRequest) {
        String subscription = publishRequest.getChannel();
        if (subscription == null || subscription.length() == 0) {
            return false;
        }
        List<BayeuxConnection> matchedConnections = new ArrayList<BayeuxConnection>();
        for (String sub : BayeuxUtil.prefixMatch(subscription, subscriptions.keySet())) {
            for (BayeuxConnection conn : subscriptions.get(sub)) {
                if (!matchedConnections.contains(conn)) {
                    matchedConnections.add(conn);
                }
            }
        }
        for (BayeuxConnection connection : matchedConnections) {
            DeliverEvent deliverEvent = new DeliverEvent(publishRequest);
            deliverEvent.setId(connection.getId());
            if (connection.getClientId().equals(publishRequest.getClientId())) {
                connection.setId(publishRequest.getId());
                connection.putToDownstream(deliverEvent);
            } else {
                connection.send(deliverEvent);
            }
        }
        return true;
    }

    /**
     * Process PublishRequest and send its data to subscribing clients by router in batch. It will return false, if publishing fails for once.
     *
     * @deprecated use publish(BayeuxConnection publisher, List<DeliverEvent> deliverList) instead
     * @param list
     * @return
     */
    public boolean onPublish(List<PublishRequest> list) {
        boolean result = true;
        for (PublishRequest publish : list) {
            if (!onPublish(publish)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Publish data to subscribing clients.
     *
     * @param publisher
     * @param deliver
     * @return
     */
    public boolean publish(BayeuxConnection publisher, DeliverEvent deliver) {
        if (!deliver.isValid()) {
            return false;
        }

        String subscription = deliver.getChannel();
        if (subscription == null || subscription.length() == 0) {
            return false;
        }

        List<BayeuxConnection> matchedConnections = new ArrayList<BayeuxConnection>();
        for (String sub : BayeuxUtil.prefixMatch(subscription, subscriptions.keySet())) {
            for (BayeuxConnection conn : subscriptions.get(sub)) {
                if (!matchedConnections.contains(conn)) {
                    matchedConnections.add(conn);
                }
            }
        }

        for (BayeuxConnection connection : matchedConnections) {
            if (connection == publisher) {
                connection.putToDownstream(deliver);
            } else {
                connection.send(deliver);
            }
        }

        return true;
    }

    /**
     * Publish data to subscribing clients in batch. It will return false, if publishing fails onece.
     *
     * @param publisher
     * @param deliverList
     * @return
     */
    public boolean publish(BayeuxConnection publisher, List<DeliverEvent> deliverList) {
        boolean result = true;
        for (DeliverEvent deliver : deliverList) {
            if (!publish(publisher, deliver)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Returns all the connections in router.
     *
     * @return
     */
    public Map<String, BayeuxConnection> getConnections() {
        return connections;
    }

    /**
     * Returns all the subscription relationships.
     *
     * @return
     */
    public Map<String, List<BayeuxConnection>> getSubscriptions() {
        return subscriptions;
    }
}
