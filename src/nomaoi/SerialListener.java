package nomaoi;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;

public class SerialListener implements SerialPortEventListener {
    private SerialChunkHandler handler;
    private InputStream input;
    private byte[] inputBuffer = new byte[1];
    private byte[] outputBuffer = new byte[4];
    private int index = 0;

    public SerialListener(SerialChunkHandler handler) {
        this.handler = handler;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return;
        }
        try {
            int n;
            while ((n = input.read(inputBuffer)) > 0) {
                outputBuffer[index] = inputBuffer[0];
                ++index;
                if (index >= 4) {
                    index = 0;
                    handler.handleChunk(outputBuffer);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
