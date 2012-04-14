package nomaoi;

import gnu.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class SerialReceiver implements AutoCloseable, Receiver, Runnable,
        SerialPortEventListener {
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 57600;
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyUSB0", // Linux
        "COM3", // Windows
    };

    private SerialPort serialPort;
    private OutputStream output;
    private BufferedReader reader;

    public boolean setup() {
        CommPortIdentifier portId = findPortId();
        if (portId == null) {
            System.err.println("Could not find COM port.");
            return false;
        }
        try {
            serialPort = (SerialPort)portId.open(this.getClass().getName(), TIME_OUT);
            serialPort.setSerialPortParams(DATA_RATE,
                                           SerialPort.DATABITS_8,
                                           SerialPort.STOPBITS_1,
                                           SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            output = serialPort.getOutputStream();
            setupReader();
            return true;
        } catch (TooManyListenersException ex) {
            ex.printStackTrace(System.err);
            return false;
        } catch (PortInUseException
                 | UnsupportedCommOperationException
                 | IOException ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }

    private CommPortIdentifier findPortId() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier)portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        return portId;
    }

    private void setupReader() throws IOException {
        InputStreamReader is = new InputStreamReader(serialPort.getInputStream());
        reader = new BufferedReader(is);
    }

    @Override
    public synchronized void close() {
        if (serialPort == null) {
            return;
        }
        serialPort.close();
    }

    @Override
    public void send(MidiMessage message, long timestamp) {
        if (!(message instanceof ShortMessage)) {
            return;
        }
        ShortMessage msg = (ShortMessage)message;
        try {
            output.write(msg.getChannel());
            output.write(msg.getCommand());
            output.write(msg.getData1());
            output.write(msg.getData2());
            output.flush();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return;
        }
        try {
            String line = reader.readLine();
            System.out.println(line);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
