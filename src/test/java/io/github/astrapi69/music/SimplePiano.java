package io.github.astrapi69.music;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.midi.*;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

class SimplePiano
{
	private static ArrayList<MidiEvent> recordedEvents;
	private static long startTime;
	private static Sequencer sequencer;
	private static Synthesizer synthesizer;
	private static MidiChannel[] channels;

	public static void main(String[] args)
	{
		try
		{
			// Get and open a synthesizer
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();

			// Get the channels from the synthesizer
			channels = synthesizer.getChannels();

			// Get the available instruments
			Instrument[] instruments = synthesizer.getDefaultSoundbank().getInstruments();

			Random random = new Random();

			// Loop to assign random instruments to channels
			for (int i = 0; i < channels.length; i++)
			{
				int randomInstrumentIndex = random.nextInt(instruments.length);
				synthesizer.loadInstrument(instruments[randomInstrumentIndex]);
				channels[i].programChange(randomInstrumentIndex);
			}

			// Initialize recording
			recordedEvents = new ArrayList<>();
			startTime = System.currentTimeMillis();
			sequencer = MidiSystem.getSequencer();
			sequencer.open();

			// Loop to play random notes on random channels
			while (true)
			{
				for (int i = 0; i < channels.length; i++)
				{
					int randomNote = 60 + random.nextInt(24); // Random note between Middle C (60)
																// and two octaves above
					int randomVelocity = 100 + random.nextInt(28); // Random velocity between 100
																	// and 127

					channels[i].noteOn(randomNote, randomVelocity);
					recordNoteOn(randomNote, i, randomVelocity);
					Thread.sleep(200); // Play note for 200 milliseconds
					channels[i].noteOff(randomNote);

					// Sleep for a short duration before playing the next note
					Thread.sleep(100);
				}

				// Check if 3 minutes have passed
				if (System.currentTimeMillis() - startTime >= 3 * 60 * 1000)
				{
					saveRecording();
					recordedEvents.clear();
					startTime = System.currentTimeMillis();
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void recordNoteOn(int note, int channelIndex, int velocity)
	{
		long timestamp = System.currentTimeMillis() - startTime;
		try
		{
			ShortMessage message = new ShortMessage(ShortMessage.NOTE_ON, channelIndex, note,
				velocity);
			recordedEvents.add(new MidiEvent(message, timestamp));
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
		}
	}

	private static void saveRecording()
	{
		try
		{
			javax.sound.midi.Sequence sequence = new Sequence(Sequence.PPQ, 1);
			Track track = sequence.createTrack();

			for (MidiEvent event : recordedEvents)
			{
				track.add(event);
			}

			String midiFileName = "recording_" + System.currentTimeMillis() + ".mid";
			File midiFile = new File(midiFileName);
			MidiSystem.write(sequence, 1, midiFile);
			System.out.println("Recording saved as " + midiFileName);

			// Convert to MP3
			convertMidiToMp3(midiFileName);

		}
		catch (IOException | InvalidMidiDataException e)
		{
			e.printStackTrace();
		}
	}

	private static void convertMidiToMp3(String midiFileName)
	{
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", midiFileName,
				"-acodec", "libmp3lame", midiFileName.replace(".mid", ".mp3"));
			Process process = processBuilder.start();
			process.waitFor();
			System.out.println("Converted " + midiFileName + " to MP3");
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}