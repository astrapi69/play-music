package io.github.astrapi69.music;
import javax.sound.midi.*;
import java.util.Random;

public class SimplePiano {
    public static void main(String[] args) {
        try {
            // Get and open a synthesizer
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            // Get the channels from the synthesizer
            MidiChannel[] channels = synthesizer.getChannels();

            // Get the available instruments
            Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();

            Random random = new Random();

            // Loop to assign random instruments to channels
            for (MidiChannel channel : channels) {
                int randomInstrumentIndex = random.nextInt(instruments.length);
                synthesizer.loadInstrument(instruments[randomInstrumentIndex]);
                channel.programChange(randomInstrumentIndex);
            }

            // Loop to play random notes on random channels
            while (true) {
                for (MidiChannel channel : channels) {
                    int randomNote = 60 + random.nextInt(24); // Random note between Middle C (60) and two octaves above
                    int randomVelocity = 100 + random.nextInt(28); // Random velocity between 100 and 127

                    channel.noteOn(randomNote, randomVelocity);
                    Thread.sleep(200); // Play note for 200 milliseconds
                    channel.noteOff(randomNote);

                    // Sleep for a short duration before playing the next note
                    Thread.sleep(100);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
