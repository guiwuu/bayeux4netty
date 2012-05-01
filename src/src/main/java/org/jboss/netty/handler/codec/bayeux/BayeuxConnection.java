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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * A Bayeux connection is the same as the Bayeux <a href="http://svn.cometd.org/trunk/bayeux/bayeux.html#toc_19">Channel</a> in protocol document.
 *
 * It's a connection between a client and a server, which can map many HTTP
 * connections between the two. And in another hand, it also provides user APIs
 * to develop Bayeux applications.
 * 
 * @author daijun
 */
public class BayeuxConnection {

    private static final InternalLogger logger =
            InternalLoggerFactory.getInstance(BayeuxEncoder.class.getName());
    private String clientId;
    private TYPE connectionType;
    private String jsonp;
    private String connectionId;//Currently it's value equals clientId's and isn't used.
    private STATE state;
    private Channel channel;
    private String id;//Message id of a connection
    private boolean isCommented = false;//Wrap response JSON string with comment
    private final LinkedList<BayeuxMessage> upstreamQueue = new LinkedList<BayeuxMessage>();//Receiving queue
    private final LinkedList<BayeuxMessage> downstreamQueue = new LinkedList<BayeuxMessage>();//Sending queue
    private final LinkedList<String> subscriptions = new LinkedList<String>();//Subscriptions, which are listenning to
    private String requestedUri;
    private String requestedHost;
    private SocketAddress clientAddress;
    private SocketAddress serverAddress;

    public enum TYPE {

        LONG_POLLING,
        LONG_POLLING_JSON_ENCODED,
        CALLBACK_POLLING,
        IFRAME,
        FLASH
    }

    public enum STATE {

        INITIAL,
        HANDSHAKED,
        CONNECTED,
        DISCONNECTED
    }

    public enum ERROR {

        UNKNOWN_ERROR,
        NO_CLIENT_ID,
        UNKNOWN_CLIENT_ID,
        UNKNOWN_CHANNEL,
        DENIED_SUBSCRIPTION,
        UNSUPPORTED_CONNECTION_TYPES,
        UNSUPPORTED_VERSION,
        REPEAT_SUBSCRIBE,
        CONN_LIMIT_EXCEEDED
    }

    /**
     * Initialize a BayeuxConnection with required properties.
     * 
     * @param channel
     */
    public BayeuxConnection() {
        this.state = STATE.INITIAL;
    }

    /**
     * Put a received Bayeux message to connection's upstream queue.
     *
     * @param bayeux
     */
    public void putToUpstream(BayeuxMessage bayeux) {
        upstreamQueue.add(bayeux);
    }

    /**
     * Put list of Bayeux request messages to connection's upstream queue
     *
     * @param list
     */
    public void putToUpstream(List<BayeuxMessage> list) {
        for (BayeuxMessage bayeux : list) {
            putToUpstream(bayeux);
        }
    }

    /**
     * Poll the first Bayeux request message from upstream queue, if queue is
     * empty returns null.
     *
     * @return
     */
    public BayeuxMessage getFromUpstream() {
        return upstreamQueue.pollFirst();
    }

    /**
     * If connection's downstream queue is not empty, write out all the messages
     * in it to client and clear it.
     */
    public synchronized void flush() {
        String response = null;
        if (!downstreamQueue.isEmpty()) {
            response = JSONParser.toJSON(downstreamQueue);
            if (isCommented) {
                response = "/*" + response + "*/";
            }
            if (jsonp != null && jsonp.length() > 0) {
                response = jsonp + "(" + response + ")";
            }
        }
        if (response != null && response.length() > 0 && channel.isWritable()) {
            ChannelFuture future = channel.write(response);
            future.addListener(ChannelFutureListener.CLOSE);
            downstreamQueue.clear();
        }
    }

    /**
     * Send a Bayeux message to connection's downstream queue, but it's
     * not sent to client immediatly. It with other messages in downstream queue
     * will be flush out, when flush() method is called for the next time.
     *
     * @param bayeux
     */
    public void putToDownstream(BayeuxMessage bayeux) {
        if (bayeux != null) {
            downstreamQueue.add(bayeux);
        }
    }

    /**
     * Send Bayeux messages list to connection's downstream queue and will be
     * sent out later like sendToQueue(BayeuxMessage bayeux).
     *
     * @param bayeux
     */
    public void putToDownstream(List<BayeuxMessage> bayeuxes) {
        for (BayeuxMessage bayeux : bayeuxes) {
            putToDownstream(bayeux);
        }
    }

    /**
     * Send a Bayeux message to client immediatly.
     *
     * @param bayeux
     */
    public void send(BayeuxMessage bayeux) {
        putToDownstream(bayeux);
        flush();
    }

    /**
     * Send Bayeux messages list to client immediatly.
     *
     * @param bayeuxes
     */
    public void send(List<BayeuxMessage> bayeuxes) {
        for (BayeuxMessage bayeux : bayeuxes) {
            putToDownstream(bayeux);
        }
        flush();
    }

    /**
     * Send a normal string immediatly to client.
     * 
     * @param response
     */
    public void send(String response) {
        if (channel.isWritable()) {
            ChannelFuture future = channel.write(response);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Clear upstream and downstream queue both.
     */
    public void clear() {
        upstreamQueue.clear();
        downstreamQueue.clear();
    }

    /**
     * Close connection.
     */
    public void close() {
        channel.close();
    }

    /**
     * Handshake protocol version and connection type with server
     *
     * @param handshakeRequest
     */
    public void handshake(HandshakeRequest handshakeRequest) {
        BayeuxExt ext = handshakeRequest.getExt();
        if (ext != null && ext.contains("json-comment-filtered")) {
            this.isCommented = (Boolean) ext.get("json-comment-filtered");
        }

        HandshakeResponse handshakeResponse = new HandshakeResponse(handshakeRequest);
        handshakeResponse.setClientId(this.clientId);

        //Handshake connection type
        TYPE[] clientSupportedConnectTypeList = handshakeRequest.getSupportedConnectionTypes();
        List<TYPE> matchedConnectTypeList = new ArrayList<TYPE>();
        Map<TYPE, TYPE> serverSupportedConnectTypeList = new HashMap();
        serverSupportedConnectTypeList.put(TYPE.LONG_POLLING, TYPE.LONG_POLLING);
        serverSupportedConnectTypeList.put(TYPE.CALLBACK_POLLING, TYPE.CALLBACK_POLLING);

        for (int i = 0; i < clientSupportedConnectTypeList.length; i++) {
            if (serverSupportedConnectTypeList.get(clientSupportedConnectTypeList[i]) != null) {
                matchedConnectTypeList.add(clientSupportedConnectTypeList[i]);
            }
        }

        if (matchedConnectTypeList.isEmpty()) {
            handshakeResponse.setSuccessful(false);
            handshakeResponse.setError(getValueOfError(ERROR.UNSUPPORTED_CONNECTION_TYPES, JSONParser.toJSON(clientSupportedConnectTypeList)));
            handshakeResponse.setSupportedConnectionTypes((new ArrayList<TYPE>(serverSupportedConnectTypeList.values())).toArray(new TYPE[0]));
            putToDownstream(handshakeResponse);
            BayeuxRouter.getInstance().removeConnection(this);
            return;
        } else {
            handshakeResponse.setSuccessful(true);
            handshakeResponse.setSupportedConnectionTypes(matchedConnectTypeList.toArray(new TYPE[0]));
        }

        //Handshake Bayeux version
        String clientMinimumVersion = handshakeRequest.getMinimumVersion();
        String clientVersion = handshakeRequest.getVersion();
        String serverMinimumVersion = "1.0beta";
        String serverVersion = "1.0beta";
        if (serverMinimumVersion.equalsIgnoreCase(clientMinimumVersion)) {
            handshakeResponse.setMinimumVersion(serverMinimumVersion);
            handshakeResponse.setSuccessful(true);
        } else if (compareVersion(serverMinimumVersion, clientMinimumVersion)) {
            handshakeResponse.setMinimumVersion(serverMinimumVersion);
            handshakeResponse.setSuccessful(!compareVersion(serverMinimumVersion, clientVersion));
        } else {
            handshakeResponse.setMinimumVersion(clientMinimumVersion);
            handshakeResponse.setSuccessful(!compareVersion(clientMinimumVersion, serverVersion));
        }

        if (handshakeResponse.isSuccessful()) {
            handshakeResponse.setVersion(compareVersion(serverVersion, clientVersion) ? clientVersion : serverVersion);
            this.state = STATE.HANDSHAKED;
        } else {
            handshakeResponse.setMinimumVersion(serverMinimumVersion);
            handshakeResponse.setVersion(serverVersion);
            handshakeResponse.setError(getValueOfError(ERROR.UNSUPPORTED_VERSION, clientMinimumVersion + "," + clientVersion));
            BayeuxRouter.getInstance().removeConnection(this);
        }
        putToDownstream(handshakeResponse);
    }

    /**
     * Connect to server.
     *
     * @param connectRequest
     */
    public void connect(ConnectRequest connectRequest) {
        if (this.state == STATE.HANDSHAKED) {
            this.connectionType = connectRequest.getConnectionType();
            this.state = STATE.CONNECTED;
            ConnectResponse connectResponse = new ConnectResponse(connectRequest);
            connectResponse.setSuccessful(true);
            putToDownstream(connectResponse);
        } else if (this.state == STATE.CONNECTED) {
            return;
        } else {
            ConnectResponse connectResponse = new ConnectResponse(connectRequest);
            connectResponse.setSuccessful(false);
            connectResponse.setAdvice(new BayeuxAdvice("handshake", 0, false));
            connectResponse.setError(getValueOfError(ERROR.UNKNOWN_ERROR, null));
            putToDownstream(connectResponse);
            this.state = STATE.DISCONNECTED;
            BayeuxRouter.getInstance().removeConnection(this);
        }
    }

    /**
     * Disconnect from server.
     *
     * @param disconnectRequest
     */
    public void disconnect(DisconnectRequest disconnectRequest) {
        boolean successful = BayeuxRouter.getInstance().removeConnection(this);
        subscriptions.clear();
        DisconnectResponse disconnectResponse = new DisconnectResponse(disconnectRequest);
        disconnectResponse.setSuccessful(successful);
        if (!successful) {
            disconnectResponse.setError(getValueOfError(ERROR.UNKNOWN_CLIENT_ID, disconnectResponse.getClientId()));
        }
        putToDownstream(disconnectResponse);
    }

    /**
     * Subscribe to channel.
     *
     * @param subscribeRequest
     */
    public void subscribe(SubscribeRequest subscribeRequest) {
        String subscription = subscribeRequest.getSubscription();
        boolean successful = BayeuxRouter.getInstance().addListener(subscription, this);
        SubscribeResponse subscribeResponse = new SubscribeResponse(subscribeRequest);
        subscribeResponse.setSuccessful(successful);
        if (successful) {
            subscriptions.add(subscription);
        } else {
            subscribeResponse.setAdvice(new BayeuxAdvice("retry", 0, false));
            subscribeResponse.setError(getValueOfError(ERROR.REPEAT_SUBSCRIBE, subscribeRequest.getClientId() + "," + subscribeRequest.getSubscription()));
        }
        putToDownstream(subscribeResponse);
    }

    /**
     * Unsubscribe from channel.
     *
     * @param unsubscribeRequest
     */
    public void unsubscribe(UnsubscribeRequest unsubscribeRequest) {
        String subscription = unsubscribeRequest.getSubscription();
        boolean successful = BayeuxRouter.getInstance().removeListener(subscription, this);
        UnsubscribeResponse unsubscribeResponse = new UnsubscribeResponse(unsubscribeRequest);
        unsubscribeResponse.setSuccessful(successful);
        if (successful) {
            subscriptions.remove(subscription);
        } else {
            unsubscribeResponse.setAdvice(new BayeuxAdvice("retry", 0, false));
            unsubscribeResponse.setError(getValueOfError(ERROR.UNKNOWN_CHANNEL, unsubscribeRequest.getClientId() + "," + unsubscribeRequest.getSubscription()));
        }
        putToDownstream(unsubscribeResponse);
    }

    /**
     * Publish data to a channel
     *
     * @param publishRequest
     */
    public void publish(PublishRequest publishRequest) {
        DeliverEvent deliver=new DeliverEvent(publishRequest);
        deliver.setClientId(this.clientId);
        deliver.setId(this.id);
        boolean successful = BayeuxRouter.getInstance().publish(this, deliver);
        PublishResponse publishResponse = new PublishResponse(publishRequest);
        publishResponse.setSuccessful(successful);
        if (!successful) {
            publishResponse.setError(getValueOfError(ERROR.UNKNOWN_CHANNEL, publishRequest.getClientId() + "," + publishRequest.getChannel()));
        }
        putToDownstream(publishResponse);
    }

    /**
     * Compare two versions in format of String, returns true if version1 > version, and verse.
     *
     * @param version1
     * @param version2
     * @return
     */
    private boolean compareVersion(String version1, String version2) {
        for (int i = 0; i < Math.min(version1.length(), version2.length()); i++) {
            if (version1.charAt(i) != version2.charAt(i)) {
                return version1.charAt(i) > version2.charAt(i);
            }
        }
        return false;
    }

    public static TYPE getTypeOfValue(String connection_type) {
        if ("long-polling".equalsIgnoreCase(connection_type)) {
            return TYPE.LONG_POLLING;
        } else if ("long-polling-json-encoded".equalsIgnoreCase(connection_type)) {
            return TYPE.LONG_POLLING_JSON_ENCODED;
        } else if ("callback-polling".equalsIgnoreCase(connection_type)) {
            return TYPE.CALLBACK_POLLING;
        } else if ("iframe".equalsIgnoreCase(connection_type)) {
            return TYPE.IFRAME;
        } else if ("flash".equalsIgnoreCase(connection_type)) {
            return TYPE.FLASH;
        } else {
            return null;
        }
    }

    public static String getValueOfType(TYPE connectionType) {
        switch (connectionType) {
            case LONG_POLLING:
                return "long-polling";
            case LONG_POLLING_JSON_ENCODED:
                return "long-polling-json-encoded";
            case CALLBACK_POLLING:
                return "callback-polling";
            case IFRAME:
                return "iframe";
            case FLASH:
                return "flash";
            default:
                return "";
        }
    }

    public static String getValueOfError(ERROR error, String msg) {
        switch (error) {
            case NO_CLIENT_ID:
                return "401::No Client ID";
            case UNKNOWN_CLIENT_ID:
                return "402:" + msg + ":Unknown Client ID";
            case DENIED_SUBSCRIPTION:
                return "403:" + msg + ":Subscription denied";
            case UNKNOWN_CHANNEL:
                return "404:" + msg + ":Unknown Channel";
            case UNSUPPORTED_CONNECTION_TYPES:
                return "405:" + msg + ":Unsupported Connection Types";
            case UNSUPPORTED_VERSION:
                return "406:" + msg + ":Unsupported version";
            case REPEAT_SUBSCRIBE:
                return "406:" + msg + ":Repeat subscribe";
            case CONN_LIMIT_EXCEEDED:
                return "407::Exceed connections limit "+msg;
            default:
                return "400::Unknown Error";
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public TYPE getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(TYPE connectionType) {
        this.connectionType = connectionType;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public String getJsonp() {
        return jsonp;
    }

    public void setJsonp(String jsonp) {
        this.jsonp = jsonp;
    }

    public LinkedList<BayeuxMessage> getDownstreamQueue() {
        return downstreamQueue;
    }

    public LinkedList<String> getSubscriptions() {
        return subscriptions;
    }

    public LinkedList<BayeuxMessage> getUpstreamQueue() {
        return upstreamQueue;
    }

    public boolean isIsCommented() {
        return isCommented;
    }

    public void setIsCommented(boolean isCommented) {
        this.isCommented = isCommented;
    }

    public String getRequestedHost() {
        return requestedHost;
    }

    public void setRequestedHost(String requestedHost) {
        this.requestedHost = requestedHost;
    }

    public String getRequestedUri() {
        return requestedUri;
    }

    public void setRequestedUri(String requestedUri) {
        this.requestedUri = requestedUri;
    }

    public SocketAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(SocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public SocketAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(SocketAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    
}
