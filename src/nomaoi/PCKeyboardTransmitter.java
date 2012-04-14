package nomaoi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

class PCKeyboardTransmitter implements Transmitter {
    private Receiver receiver;
    private ShortMessage msgNoteOn = new ShortMessage();
    private ShortMessage msgNoteOff = new ShortMessage();

    PCKeyboardTransmitter() {}

    void noteOn(int channel, int note, int velocity) throws InvalidMidiDataException {
        msgNoteOn.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
        receiver.send(msgNoteOn, -1);
    }

    void noteOff(int channel, int note, int velocity) throws InvalidMidiDataException {
        msgNoteOff.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
        receiver.send(msgNoteOff, -1);
    }

    @Override
    public void close() {}

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public void setReceiver(Receiver arg) {
        receiver = arg;
    }
}
