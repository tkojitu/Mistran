package nomaoi;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Mistran implements ActionListener, Runnable, SerialChunkHandler {
    private final static String[] INST_NAMES = {
        "1 Acoustic Grand Piano",
        "2 Bright Acoustic Piano",
        "3 Electric Grand Piano",
        "4 Honky-tonk Piano",
        "5 Electric Piano 1",
        "6 Electric Piano 2",
        "7 Harpsichord",
        "8 Clavi",
        "9 Celesta",
        "10 Glockenspiel",
        "11 Music Box",
        "12 Vibraphone",
        "13 Marimba",
        "14 Xylophone",
        "15 Tubular Bells",
        "16 Dulcimer",
        "17 Drawbar Organ",
        "18 Percussive Organ",
        "19 Rock Organ",
        "20 Church Organ",
        "21 Reed Organ",
        "22 Accordion",
        "23 Harmonica",
        "24 Tango Accordion",
        "25 Acoustic Guitar (nylon)",
        "26 Acoustic Guitar (steel)",
        "27 Electric Guitar (jazz)",
        "28 Electric Guitar (clean)",
        "29 Electric Guitar (muted)",
        "30 Overdriven Guitar",
        "31 Distortion Guitar",
        "32 Guitar Harmonics",
        "33 Acoustic Bass",
        "34 Electric Bass (finger)",
        "35 Electric Bass (pick)",
        "36 Fretless Bass",
        "37 Slap Bass 1",
        "38 Slap Bass 2",
        "39 Synth Bass 1",
        "40 Synth Bass 2",
        "41 Violin",
        "42 Viola",
        "43 Cello",
        "44 Contrabass",
        "45 Tremolo Strings",
        "46 Pizzicato Strings",
        "47 Orchestral Harp",
        "48 Timpani",
        "49 String Ensembles 1",
        "50 String Ensembles 2",
        "51 Synth Strings 1",
        "52 Synth Strings 2",
        "53 Choir Aahs",
        "54 Voice Oohs",
        "55 Synth Voice",
        "56 Orchestra Hit",
        "57 Trumpet",
        "58 Trombone",
        "59 Tuba",
        "60 Muted Trumpet",
        "61 French Horn",
        "62 Brass Section",
        "63 Synth Brass 1",
        "64 Synth Brass 2",
        "65 Soprano Sax",
        "66 Alto Sax",
        "67 Tenor Sax",
        "68 Baritone Sax",
        "69 Oboe",
        "70 English Horn",
        "71 Bassoon",
        "72 Clarinet",
        "73 Piccolo",
        "74 Flute",
        "75 Recorder",
        "76 Pan Flute",
        "77 Blown Bottle",
        "78 Shakuhachi",
        "79 Whistle",
        "80 Ocarina",
        "81 Square Lead (Lead 1)",
        "82 Saw Lead (Lead)",
        "83 Calliope Lead (Lead 3)",
        "84 Chiff Lead (Lead 4)",
        "85 Charang Lead (Lead 5)",
        "86 Voice Lead (Lead 6)",
        "87 Fifths Lead (Lead 7)",
        "88 Bass + Lead (Lead 8)",
        "89 New Age (Pad 1)",
        "90 Warm Pad (Pad 2)",
        "91 Polysynth (Pad 3)",
        "92 Choir (Pad 4)",
        "93 Bowed (Pad 5)",
        "94 Metallic (Pad 6)",
        "95 Halo (Pad 7)",
        "96 Sweep (Pad 8)",
        "97 Rain (FX 1)",
        "98 Sound Track (FX 2)",
        "99 Crystal (FX 3)",
        "100 Atmosphere (FX 4)",
        "101 Brightness (FX 5)",
        "102 Goblins (FX 6)",
        "103 Echoes (FX 7)",
        "104 Sci-fi (FX 8)",
        "105 Sitar",
        "106 Banjo",
        "107 Shamisen",
        "108 Koto",
        "109 Kalimba",
        "110 Bag Pipe",
        "111 Fiddle",
        "112 Shanai",
        "113 Tinkle Bell",
        "114 Agogo",
        "115 Pitched Percussion",
        "116 Woodblock",
        "117 Taiko Drum",
        "118 Melodic Tom",
        "119 Synth Drum",
        "120 Reverse Cymbal",
        "121 Guitar Fret Noise",
        "122 Breath Noise",
        "123 Seashore",
        "124 Bird Tweet",
        "125 Telephone Ring",
        "126 Helicopter",
        "127 Applause",
        "128 Gunshot"
    };

    private SerialReceiver serial = new SerialReceiver();
    private MidiInControl midiIn = new MidiInControl();
    private JComboBox combox;
    private int instrument;
    private StringBuilder dumpBuffer = new StringBuilder();
    private Formatter formatter = new Formatter(dumpBuffer);

    public Mistran(int instrument) {
        this.instrument = instrument;
    }

    public void setup() {
        if (serial.setup(new SerialListener(this))) {
            midiIn.setup(serial);
        }
    }

    public void createAndShowGui() {
        JFrame frame = createFrame();
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Mistran");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(midiIn.getKeyListener());
        JPanel pane = createPane();
        frame.setContentPane(pane);
        frame.pack();
        return frame;
    }

    private JPanel createPane() {
        JPanel pane = new JPanel();
        pane.add(createCombox());
        return pane;
    }

    private JComboBox createCombox() {
        combox = new JComboBox(INST_NAMES);
        combox.addActionListener(this);
        combox.addKeyListener(midiIn.getKeyListener());
        combox.setSelectedIndex(instrument - 1);
        return combox;
    }

    @Override
    public void run() {
        createAndShowGui();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        instrument = combox.getSelectedIndex() + 1;
        serial.setInstrument(instrument);
    }

    @Override
    public void handleChunk(byte[] chunk) {
        if (isChunkHelo(chunk)) {
            serial.setInstrument(instrument);
        }
        dumpChunk(chunk);
    }

    private boolean isChunkHelo(byte[] chunk) {
        return chunk.length == 4
            && chunk[0] == (byte)'H' && chunk[1] == (byte)'E'
            && chunk[2] == (byte)'L' && chunk[3] == (byte)'O';
    }

    private void dumpChunk(byte[] chunk) {
        formatter.format("%X%X%X%X", chunk[0], chunk[1], chunk[2], chunk[3]);
        System.out.println(dumpBuffer.toString());
        dumpBuffer.setLength(0);
    }

    public static void main(String[] args) throws Exception {
        int instrument = getInstrumentNumber(args);
        Mistran app = new Mistran(instrument);
        app.setup();
        javax.swing.SwingUtilities.invokeLater(app);
    }

    private static int getInstrumentNumber(String[] args) {
        if (args.length == 0) {
            return 1;
        }
        try {
            return Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
