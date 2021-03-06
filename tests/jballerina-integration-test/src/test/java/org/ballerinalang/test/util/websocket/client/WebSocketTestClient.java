/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.ballerinalang.test.util.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslHandler;
import org.ballerinalang.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * WebSocket client class for test.
 */
public class WebSocketTestClient {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketTestClient.class);

    private Channel channel = null;
    private WebSocketTestClientHandler webSocketHandler;
    private final URI uri;
    private EventLoopGroup group;
    private boolean first = true;
    private boolean sslEnabled;

    public WebSocketTestClient(String url) throws URISyntaxException {
        this(url, new HashMap<>());
    }

    public WebSocketTestClient(String url, boolean sslEnabled) throws URISyntaxException {
        this(url, new HashMap<>());
        this.sslEnabled = sslEnabled;
    }

    public WebSocketTestClient(String url, Map<String, String> headers) throws URISyntaxException {
        this.uri = new URI(url);
        // Creating webSocketHandler
        URI uri = new URI(url);
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        headers.forEach(httpHeaders::add);
        webSocketHandler = new WebSocketTestClientHandler(WebSocketClientHandshakerFactory.
                newHandshaker(uri, WebSocketVersion.V13, null, true, httpHeaders));
    }

    public void setCountDownLatch(CountDownLatch countdownLatch) {
        webSocketHandler.setCountDownLatch(countdownLatch);
    }

    /**
     * Handshake with the remote server.
     */
    public void handshake() throws InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch)
                    throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
                           CertificateException {
                ChannelPipeline pipeline = ch.pipeline();
                if (sslEnabled) {
                    SSLEngine sslEngine = createSSLContextFromTruststores().createSSLEngine();
                    sslEngine.setUseClientMode(true);
                    pipeline.addLast(new SslHandler(sslEngine));
                }
                pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192),
                                 WebSocketClientCompressionHandler.INSTANCE, webSocketHandler);
            }
        });
        channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
        webSocketHandler.handshakeFuture().sync();
    }

    private SSLContext createSSLContextFromTruststores()
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException,
                   CertificateException {
        TrustManager[] trustManagers;
        KeyStore tks = TestUtils.getKeyStore(new File("src" + File.separator + "test" + File.separator + "resources" +
                                                              File.separator + "certsAndKeys" + File.separator +
                                                              "ballerinaTruststore.p12"));
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(tks);
        trustManagers = tmf.getTrustManagers();
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);
        return sslContext;
    }


    /**
     * Send text to the server.
     *
     * @param text text need to be sent.
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendText(String text) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new TextWebSocketFrame(text)).sync();
    }

    /**
     * Send text to the server.
     *
     * @param text    text to be sent.
     * @param isFinal whether the text is final
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendText(String text, boolean isFinal) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        if (!isFinal) {
            if (first) {
                channel.writeAndFlush(new TextWebSocketFrame(false, 0, text)).sync();
                first = false;
            } else {
                channel.writeAndFlush(new ContinuationWebSocketFrame(false, 0, text));
            }
        } else {
            channel.writeAndFlush(new ContinuationWebSocketFrame(true, 0, text));
            first = true;
        }
    }

    /**
     * Send binary to the server.
     *
     * @param buffer  buffer containing the data to be sent.
     * @param isFinal whether the text is final
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendBinary(ByteBuffer buffer, boolean isFinal) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        if (!isFinal) {
            if (first) {
                channel.writeAndFlush(new BinaryWebSocketFrame(false, 0, Unpooled.wrappedBuffer(buffer))).sync();
                first = false;
            } else {
                channel.writeAndFlush(new ContinuationWebSocketFrame(false, 0, Unpooled.wrappedBuffer(buffer)));
            }
        } else {
            channel.writeAndFlush(new ContinuationWebSocketFrame(true, 0, Unpooled.wrappedBuffer(buffer)));
            first = true;
        }
    }

    /**
     * Send binary data to server.
     *
     * @param buf buffer containing the data need to be sent.
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendBinary(ByteBuffer buf) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(buf))).sync();
    }

    /**
     * Send a ping message to the server.
     *
     * @param buf content of the ping message to be sent.
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendPing(ByteBuffer buf) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new PingWebSocketFrame(Unpooled.wrappedBuffer(buf))).sync();
    }

    /**
     * Send a pong message to the server.
     *
     * @param buf content of the pong message to be sent.
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendPong(ByteBuffer buf) throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new PongWebSocketFrame(Unpooled.wrappedBuffer(buf))).sync();
    }

    /**
     * Send corrupted frame to the server.
     *
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendCorruptedFrame() throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send text.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new ContinuationWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}))).sync();
    }

    /**
     * Sends a close frame without a close code.
     *
     * @throws InterruptedException if connection is interrupted while sending the message.
     */
    public void sendCloseFrameWithoutCloseCode() throws InterruptedException {
        if (channel == null) {
            logger.error("Channel is null. Cannot send close frame.");
            throw new IllegalArgumentException("Cannot find the channel to write");
        }
        channel.writeAndFlush(new CloseWebSocketFrame()).sync();
    }

    /**
     * @return the text received from the server.
     */
    public String getTextReceived() {
        return webSocketHandler.getTextReceived();
    }

    /**
     * @return the binary data received from the server.
     */
    public ByteBuffer getBufferReceived() {
        return webSocketHandler.getBufferReceived();
    }

    /**
     * Check whether the connection is opened or not.
     *
     * @return true if the connection is open.
     */
    public boolean isOpen() {
        return webSocketHandler.isOpen();
    }

    /**
     * Check whether webSocketHandler receives a ping.
     *
     * @return true if a ping is received.
     */
    public boolean isPing() {
        return webSocketHandler.isPing();
    }

    /**
     * Check whether webSocketHandler receives a pong.
     *
     * @return true if a pong is received.
     */
    public boolean isPong() {
        return webSocketHandler.isPong();
    }

    /**
     * Gets the header value from the response headers from the WebSocket handler.
     *
     * @param headerName the header name
     * @return the header value from the response headers.
     */
    public String getHeader(String headerName) {
        return webSocketHandler.getHeader(headerName);
    }

    /**
     * Retrieve the received close frame to the client.
     * <b>Note: Release the close frame after using it using CloseWebSocketFrame.release()</b>
     *
     * @return the close frame received to the client.
     */
    public CloseWebSocketFrame getReceivedCloseFrame() {
        return webSocketHandler.getReceivedCloseFrame();
    }

    /**
     * Shutdown the WebSocket Client.
     */
    public void shutDown() throws InterruptedException {
        channel.writeAndFlush(new CloseWebSocketFrame());
        channel.closeFuture().sync();
        group.shutdownGracefully();
    }

    /**
     * Shutdown the WebSocket Client.
     */
    public void shutDownWithoutCloseFrame() {
        channel.close();
        group.shutdownGracefully();
    }
}
