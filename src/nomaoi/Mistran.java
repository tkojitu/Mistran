package nomaoi;

import javax.swing.JFrame;

public class Mistran implements Runnable {
    private SerialReceiver serial = new SerialReceiver();
    private MidiInControl midiIn = new MidiInControl();

    public void setup() {
        if (serial.setup()) {
            midiIn.setup(serial);
        }
    }

    public void createAndShowGui() {
        JFrame frame = new JFrame("Mistran");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.addKeyListener(midiIn.getKeyListener());
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    public static void main(String[] args) throws Exception {
        Mistran app = new Mistran();
        app.setup();
        javax.swing.SwingUtilities.invokeLater(app);
    }
}
