package com.example.nionetty.buffer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Example of a simple custom buffer wrapper around java.nio.ByteBuffer.
 * In a real scenario, this would be much more complex like Netty's ByteBuf.
 * Often, directly using ByteBuffer might be sufficient for simpler mocks.
 */
public class CustomBuffer {

    private final ByteBuffer buffer;

    public CustomBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public static CustomBuffer allocate(int capacity) {
        return new CustomBuffer(ByteBuffer.allocate(capacity));
    }

    public static CustomBuffer wrap(byte[] array) {
        return new CustomBuffer(ByteBuffer.wrap(array));
    }

    public int readableBytes() {
        return buffer.remaining();
    }

    public int writableBytes() {
        // This is simplistic; assumes capacity is the limit for writing
        return buffer.capacity() - buffer.position();
    }

    public byte readByte() {
        return buffer.get();
    }

    public void writeByte(byte b) {
        ensureWritable(1);
        buffer.put(b);
    }

    public void writeBytes(byte[] src) {
        ensureWritable(src.length);
        buffer.put(src);
    }

     public void readBytes(byte[] dst) {
         buffer.get(dst);
     }

     public void readBytes(ByteBuffer dst) {
         // Read remaining bytes from internal buffer into dst
         dst.put(buffer);
     }

    public void writeBytes(ByteBuffer src) {
        ensureWritable(src.remaining());
        buffer.put(src);
    }

    public ByteBuffer nioBuffer() {
        return buffer;
    }

    // Prepare buffer for reading after writing
    public void flip() {
        buffer.flip();
    }

    // Prepare buffer for writing after reading
    public void compact() {
        buffer.compact();
    }

    public void clear() {
        buffer.clear();
    }

     public String toString(Charset charset) {
         // Read remaining bytes as string without changing buffer state
         int pos = buffer.position();
         int limit = buffer.limit();
         byte[] bytes = new byte[readableBytes()];
         buffer.get(bytes);
         // Restore buffer state
         buffer.position(pos);
         buffer.limit(limit);
         return new String(bytes, charset);
     }

    @Override
    public String toString() {
         return toString(StandardCharsets.UTF_8);
    }

    // Very basic ensure writable - real buffer pools would resize/reallocate
    private void ensureWritable(int size) {
        if (buffer.remaining() < size) {
             // In a real buffer, you might reallocate a larger buffer and copy data.
             // Here, we'll just throw an exception for simplicity.
            throw new IndexOutOfBoundsException(
                String.format("Buffer cannot write %d bytes, only %d remaining.", size, buffer.remaining())
            );
             // Or maybe auto-expand if it's dynamically allocated...
             // ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
             // buffer.flip(); // Prepare for reading
             // newBuffer.put(buffer); // Copy old data
             // this.buffer = newBuffer;
        }
    }
}