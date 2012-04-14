package nomaoi;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.*;

public class PCKeyboard implements KeyListener, MidiDevice {
    private PCKeyboardTransmitter transmitter = new PCKeyboardTransmitter();
    private boolean closing = true;
    private int channel = 0;
    private int lastNote = -1;
    private int velocity = 64;

    public PCKeyboard() {}

    @Override
    public void keyPressed(KeyEvent event) {
        if (closing) {
            return;
        }
        int code = event.getKeyCode();
        if (code == KeyEvent.VK_SPACE) {
            noteOff();
            return;
        }
        int note = keyCodeToNote(code);
        note = modifyNote(event, note);
        noteOff();
        noteOn(note);
    }

    private void noteOff() {
        if (lastNote < 0) {
            return;
        }
        try {
            transmitter.noteOff(channel, lastNote, velocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace(System.err);
        }
        lastNote = -1;
    }

    private void noteOn(int note) {
        if (note < 0) {
            return;
        }
        try {
            transmitter.noteOn(channel, note, velocity);
            lastNote = note;
        } catch (InvalidMidiDataException e) {
            e.printStackTrace(System.err);
            lastNote = -1;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {}

    @Override
    public void keyTyped(KeyEvent event) {}

    private int keyCodeToNote(int keyCode) {
        switch (keyCode) {
        case KeyEvent.VK_D:
            return 60;
        case KeyEvent.VK_F:
            return 62;
        case KeyEvent.VK_G:
            return 64;
        case KeyEvent.VK_H:
            return 65;
        case KeyEvent.VK_J:
            return 67;
        case KeyEvent.VK_K:
            return 69;
        case KeyEvent.VK_L:
            return 71;
        case KeyEvent.VK_SEMICOLON:
            return 72;
        default:
            return -1;
        }
    }

    private int modifyNote(KeyEvent event, int note) {
        int result = note;
        int modifiers = event.getModifiersEx();
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            result++;
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            return result + 12;
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            return result - 12;
        }
        return result;
    }

    @Override
    public void close() {
        if (lastNote >= 0) {
            try {
                transmitter.noteOff(channel, lastNote, velocity);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace(System.err);
            }
        }
        lastNote = -1;
        closing = true;
        transmitter.close();
    }

    @Override
    public MidiDevice.Info getDeviceInfo() {
        return new PCKeyboardInfo("nomaoi.PCKeyboard",
                                  "www.jitu.org",
                                  "PC keyboard as MIDI input device",
                                  "1");
    }

    @Override
    public int getMaxReceivers() {
        return 0;
    }

    @Override
    public int getMaxTransmitters() {
        return 1;
    }

    @Override
    public long getMicrosecondPosition() {
        return -1;
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        throw new MidiUnavailableException("PCKeyboard has no receivers.");
    }

    @Override
    public List<Receiver> getReceivers() {
        return new ArrayList<>();
    }

    @Override
    public Transmitter getTransmitter() {
        return transmitter;
    }

    @Override
    public List<Transmitter> getTransmitters() {
        List<Transmitter> results = new ArrayList<>();
        results.add(transmitter);
        return results;
    }

    @Override
    public boolean isOpen() {
        return closing;
    }

    @Override
    public void open() {
        closing = false;
    }

    class PCKeyboardInfo extends MidiDevice.Info {
        PCKeyboardInfo(String name, String vendor, String desc, String ver) {
            super(name, vendor, desc, ver);
        }
    }
}
