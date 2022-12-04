import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class StringLengthHeaderSerializer extends AbstractByteArraySerializer {

    /**
     * Default length-header field, allows for data up to 2**31-1 bytes.
     */
    public static final int HEADER_SIZE_INT = 4; // default

    private final int headerSize;

    private final Log logger = LogFactory.getLog(this.getClass());

    /**
     * Constructs the serializer using {@link #HEADER_SIZE_INT}
     */
    public StringLengthHeaderSerializer() {
        this(HEADER_SIZE_INT);
    }

    /**
     * Constructs the serializer using the supplied header size. Valid header
     * sizes are {@link #HEADER_SIZE_INT} (default),
     * {@link #HEADER_SIZE_UNSIGNED_BYTE} and
     * {@link #HEADER_SIZE_UNSIGNED_SHORT}
     *
     * @param headerSize The header size.
     */
    public StringLengthHeaderSerializer(int headerSize) {
        if (headerSize != HEADER_SIZE_INT) {
            throw new IllegalArgumentException("Illegal header size:" + headerSize);
        }
        this.headerSize = headerSize;
    }

    /**
     * Reads the header from the stream and then reads the provided length from
     * the stream and returns the data in a byte[]. Throws an IOException if the
     * length field exceeds the maxMessageSize. Throws a
     * {@link SoftEndOfStreamException} if the stream is closed between
     * messages.
     *
     * @param inputStream The input stream.
     * @throws IOException Any IOException.
     */
    @Override
    public byte[] deserialize(InputStream inputStream) throws IOException {
        byte[] messageLengthPart = this.readHeader(inputStream);
        String msgLength = new String(messageLengthPart);
        int messageLength = Integer.parseInt(msgLength);
        this.logger.info("Message length is " + messageLength);
        byte[] messagePart = null;
        try {
            if (messageLength > this.getMaxMessageSize()) {
                throw new IOException(
                    "Message length " + messageLength + " exceeds max message length: " + this.getMaxMessageSize());
            }
            messagePart = new byte[messageLength];
            read(inputStream, messagePart, false);
            return messagePart;
        } catch (IOException e) {
            publishEvent(e, messagePart, -1);
            throw e;
        } catch (RuntimeException e) {
            publishEvent(e, messagePart, -1);
            throw e;
        }
    }

    /**
     * Writes the byte[] to the output stream, preceded by a 4 byte length in
     * network byte order (big endian).
     *
     * @param bytes The bytes.
     * @param outputStream The output stream.
     */
    @Override
    public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
        this.writeHeader(outputStream, bytes.length);
        outputStream.write(bytes);
    }

    /**
     * Reads data from the socket and puts the data in buffer. Blocks until
     * buffer is full or a socket timeout occurs.
     *
     * @param inputStream The input stream.
     * @param buffer the buffer into which the data should be read
     * @param header true if we are reading the header
     * @return {@code < 0} if socket closed and not in the middle of a message
     * @throws IOException Any IOException.
     */
    protected int read(InputStream inputStream, byte[] buffer, boolean header) throws IOException {
        int lengthRead = 0;
        int needed = buffer.length;
        while (lengthRead < needed) {
            int len;
            len = inputStream.read(buffer, lengthRead, needed - lengthRead);
            if (len < 0 && header && lengthRead == 0) {
                return len;
            }
            if (len < 0) {
                throw new IOException("Stream closed after " + lengthRead + " of " + needed);
            }
            lengthRead += len;
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Read " + len + " bytes, buffer is now at " + lengthRead + " of " + needed);
            }
        }

        return 0;
    }

    /**
     * Writes the header, according to the header format.
     *
     * @param outputStream The output stream.
     * @param length The length.
     * @throws IOException Any IOException.
     */
    protected void writeHeader(OutputStream outputStream, int length) throws IOException {
        ByteBuffer lengthPart = ByteBuffer.allocate(this.headerSize);
        switch (this.headerSize) {
            case HEADER_SIZE_INT:
                lengthPart.put((String.format("%04d", length)).getBytes());
                break;
            default:
                throw new IllegalArgumentException("Bad header size:" + this.headerSize);
        }
        outputStream.write(lengthPart.array());
    }

    /**
     * Reads the header and returns the length of the data part.
     *
     * @param inputStream The input stream.
     * @return The length of the data part.
     * @throws IOException Any IOException.
     * @throws SoftEndOfStreamException if socket closes before any length data read.
     */
    protected byte[] readHeader(InputStream inputStream) throws IOException {
        byte[] lengthPart = new byte[this.headerSize];
        try {
            int status = read(inputStream, lengthPart, true);
            if (status < 0) {
                throw new SoftEndOfStreamException("Stream closed between payloads");
            }
            int messageLength;
            switch (this.headerSize) {
                case HEADER_SIZE_INT:
                    String s = new String(lengthPart);
                    this.logger.info("헤더길이 " + s);
                    messageLength = Integer.parseInt(s);
                    if (messageLength < 0) {
                        throw new IllegalArgumentException("Length header:" + messageLength + " is negative");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Bad header size:" + this.headerSize);
            }
            return lengthPart;
        } catch (SoftEndOfStreamException e) {
            throw e;
        } catch (IOException e) {
            publishEvent(e, lengthPart, -1);
            throw e;
        } catch (RuntimeException e) {
            publishEvent(e, lengthPart, -1);
            throw e;
        }
    }
}
