package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Char data buffer that automatically calls given function when filled
 */
class AutoDataBuffer {
    private final Function<byte[], RC> out;
    private final byte[] buffer;
    private int filled;

    /**Char data buffer that automatically calls given function when filled
     * @param out function, that needs to be applied to data
     * @param size size of data buffer in bytes
     */
    public AutoDataBuffer(Function<byte[], RC> out, int size) {
        this.out = out;
        this.buffer = new byte[size];
    }

    /**Force flushes buffer
     * @return  Return Code object, which contains information about reason of the end of work
     */
    public RC flush() {
        byte[] data = Arrays.copyOf(buffer, filled);
        filled = 0;
        return out.apply(data);
    }

    /**Puts a given byte to buffer
     * @param val byte to put to buffer
     * @return Return Code object, which contains information about reason of the end of work
     */
    public RC write(byte val) {
        buffer[filled] = val;
        if (++filled == buffer.length) {
            return flush();
        }
        return RC.RC_SUCCESS;
    }
}
