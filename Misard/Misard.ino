// -*- c++ -*-

#include <SoftwareSerial.h>

class MisPort {
private:
    SoftwareSerial* output;

public:
    MisPort() : output(NULL) {
        output = new SoftwareSerial(2, 3);
    }

    ~MisPort() {
        delete output;
    }

    void setup() {
        output->begin(31250);
        setRealtimeMidiMode();
        setChannelVolume(0, 120);
    }

    void setRealtimeMidiMode() {
        byte RESET_MIDI = 4;
        ::pinMode(RESET_MIDI, OUTPUT);
        ::digitalWrite(RESET_MIDI, LOW);
        ::delay(100);
        ::digitalWrite(RESET_MIDI, HIGH);
        ::delay(100);
    }

    void setBankDefault() {
        send(0xB0, 0, 0x00);
    }

    void setChannelVolume(byte channel, byte volume) {
        sendShort(0, 0xB0, 0x07, volume);
    }

    void noteOn(byte channel, byte note, byte attack_velocity) {
        sendShort(channel, 0x90, note, attack_velocity);
    }

    void noteOff(byte channel, byte note, byte release_velocity) {
        sendShort(channel, 0x80, note, release_velocity);
    }

    void sendShort(byte channel, byte command, byte data1, byte data2) {
        send(channel | command, data1, data2);
    }

    void send(byte cmd, byte data1, byte data2) {
        output->write(cmd);
        output->write(data1);
        if ((cmd & 0xF0) <= 0xB0) {
            output->write(data2);
        }
    }
};

class Misard {
private:
    MisPort* misPort;
    byte buffer[4];
    int index;
    bool demo;

public:
    Misard() : misPort(NULL), index(0), demo(true) {
        misPort = new MisPort();
    }

    ~Misard() {
        delete misPort;
    }

    void setup() {
        setupSerial();
        setupMisPort();
        echoHelo();
    }

    void setupSerial() {
        Serial.begin(57600);
        Serial.setTimeout(1000);
    }

    void setupMisPort() {
        misPort->setup();
        misPort->setBankDefault();
    }
    
    void echoHelo() {
        Serial.print("HELO");
        Serial.flush();
    }

    void echoBuffer() {
        Serial.print(buffer[0], HEX);
        Serial.print(buffer[1], HEX);
        Serial.print(buffer[2], HEX);
        Serial.print(buffer[3], HEX);
    }

    void loop() {
        if (!demo) {
            demoMainBank();
            demo = true;
        }
        while (Serial.available()) {
            if (handleByte(Serial.read())) {
                return;
            }
        }
    }

    bool handleByte(char c) {
        buffer[index] = (byte)c;
        index++;
        if (index < 4) {
            return false;
        }
        sendMidi();
        echoBuffer();
        index = 0;
        return true;
    }

    void sendMidi() {
        misPort->sendShort(buffer[0], buffer[1], buffer[2], buffer[3]);
    }

    void demoMainBank() {
        misPort->setBankDefault();
        for (int instrument = 8; instrument >= 1; --instrument) {
            misPort->send(0xC0, instrument, 0);
            for (byte note = 60; note < 70; note++) {
                misPort->noteOn(0, note, 60);
                ::delay(50);
                misPort->noteOff(0, note, 60);
                ::delay(50);
            }
            ::delay(100);
        }
    }
};

void* gApp;

void setup() {
    Misard* app = new Misard();
    gApp = app;
    app->setup();
}

void loop() {
    ((Misard*)gApp)->loop();
}
