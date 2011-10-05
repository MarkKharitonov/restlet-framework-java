/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.ssl.internal;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableBufferedChannel;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.SelectionChannel;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends ReadableBufferedChannel implements
        TasksListener {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel.
     * @param connection
     *            The parent SSL connection.
     */
    public ReadableSslChannel(ReadableSelectionChannel source,
            SslConnection<?> connection) {
        super(null, new Buffer(connection.getPacketBufferSize(), connection
                .getHelper().isDirectBuffers()), source);
        this.connection = connection;
    }

    @Override
    public boolean canLoop(Buffer buffer, Object... args) {
        return getConnection().getInboundWay().canLoop(buffer, args)
                && ((getConnection().getSslEngineStatus() == Status.OK) || (getConnection()
                        .getSslEngineStatus() == Status.BUFFER_UNDERFLOW));
    }

    /**
     * Indicates if draining can be retried.
     * 
     * @return True if draining can be retried.
     */
    public boolean canRetry(int lastRead, ByteBuffer targetBuffer) {
        return ((lastRead > 0) || (getConnection().isSslHandshaking()
                && (getConnection().getSslEngineStatus() == Status.OK) && (getConnection()
                .getSslHandshakeStatus() == HandshakeStatus.NEED_UNWRAP)))
                && targetBuffer.hasRemaining();
    }

    @Override
    public boolean couldDrain(Buffer buffer, Object... args) {
        return (getConnection().getSslEngineStatus() != Status.CLOSED)
                && ((getConnection().getSslHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING) || (getConnection()
                        .getSslHandshakeStatus() == HandshakeStatus.NEED_UNWRAP));
    }

    /**
     * Returns the parent SSL connection.
     * 
     * @return The parent SSL connection.
     */
    protected SslConnection<?> getConnection() {
        return connection;
    }

    /**
     * Callback method invoked upon delegated tasks completion.
     */
    public void onCompleted() {
        if (getConnection().getInboundWay().getIoState() == IoState.IDLE) {
            getConnection().getInboundWay().setIoState(IoState.READY);
        }
    }

    /**
     * Drains the byte buffer. By default, it decrypts the SSL data and copies
     * as many byte as possible to the target buffer, with no modification.
     */
    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        ByteBuffer applicationBuffer = (ByteBuffer) args[0];
        int initialSize = buffer.remaining();
        SSLEngineResult sslResult = getConnection().getSslEngine().unwrap(
                buffer.getBytes(), applicationBuffer);
        getConnection().setSslResult(sslResult);
        return initialSize - buffer.remaining();
    }

    @Override
    public int read(ByteBuffer targetBuffer) throws IOException {
        return super.read(targetBuffer);
    }

}