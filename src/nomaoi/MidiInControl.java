package nomaoi;

import java.awt.event.KeyListener;
import javax.sound.midi.*;

public class MidiInControl implements AutoCloseable {
    private MidiDevice midiIn;
    private PCKeyboard keyboard = new PCKeyboard();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    @Override
    public void close() {
        if (midiIn != null) {
            midiIn.close();
        }
    }

    public boolean setup(Receiver recv) {
        try {
            midiIn = findMidiInDevice();
            Transmitter trans = midiIn.getTransmitter();
            trans.setReceiver(recv);
            midiIn.open();
            return true;
        } catch (MidiUnavailableException ex) {
            ex.printStackTrace(System.err);
            return false;
        }
    }

    private MidiDevice findMidiInDevice() throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        final String klass = "class com.sun.media.sound.MidiInDevice";
        for (int i = 0; i < infos.length; ++ i) {
            MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
            if (klass.equals(dev.getClass().toString())) {
                return dev;
            }
        }
        return keyboard;
    }

    public KeyListener getKeyListener() {
        return keyboard;
    }
}
