package io.github.astrapi69.music;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PianoGUI extends JFrame {
    private Synthesizer synthesizer;
    private MidiChannel channel;
    private ArrayList<MidiEvent> recordedEvents;
    private long startTime;
    private boolean isRecording = false;
    private Sequencer sequencer;

    public PianoGUI() {
        setTitle("Simple Piano GUI");
        setSize(800, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channel = synthesizer.getChannels()[0];
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        JPanel pianoPanel = new JPanel();
        pianoPanel.setLayout(new GridLayout(1, 12));

        for (int i = 0; i < 12; i++) {
            JButton key = new JButton(String.valueOf(i + 1));
            int note = 60 + i; // Middle C is 60
            key.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playNote(note);
                    if (isRecording) {
                        recordNoteOn(note);
                    }
                }
            });
            pianoPanel.add(key);
        }

        JPanel controlPanel = new JPanel();
        JButton recordButton = new JButton("Record");
        JButton stopButton = new JButton("Stop");
        JButton playButton = new JButton("Play");
        JButton saveButton = new JButton("Save");
        JButton loadButton = new JButton("Load");

        recordButton.addActionListener(e -> startRecording());
        stopButton.addActionListener(e -> stopRecording());
        playButton.addActionListener(e -> playRecording());
        saveButton.addActionListener(e -> saveRecording());
        loadButton.addActionListener(e -> loadRecording());

        controlPanel.add(recordButton);
        controlPanel.add(stopButton);
        controlPanel.add(playButton);
        controlPanel.add(saveButton);
        controlPanel.add(loadButton);

        add(pianoPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void playNote(int note) {
        channel.noteOn(note, 600);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channel.noteOff(note);
    }

    private void startRecording() {
        recordedEvents = new ArrayList<>();
        startTime = System.currentTimeMillis();
        isRecording = true;
    }

    private void stopRecording() {
        isRecording = false;
    }

    private void recordNoteOn(int note) {
        long timestamp = System.currentTimeMillis() - startTime;
        try {
            ShortMessage message = new ShortMessage(ShortMessage.NOTE_ON, 0, note, 600);
            recordedEvents.add(new MidiEvent(message, timestamp));
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void playRecording() {
        if (recordedEvents != null && !recordedEvents.isEmpty()) {
            try {
                Sequence sequence = new Sequence(Sequence.PPQ, 1);
                Track track = sequence.createTrack();

                for (MidiEvent event : recordedEvents) {
                    track.add(event);
                }

                sequencer.setSequence(sequence);
                sequencer.start();
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRecording() {
        if (recordedEvents != null && !recordedEvents.isEmpty()) {
            try {
                Sequence sequence = new Sequence(Sequence.PPQ, 1);
                Track track = sequence.createTrack();

                for (MidiEvent event : recordedEvents) {
                    track.add(event);
                }

                File file = new File("recording.mid");
                MidiSystem.write(sequence, 1, file);
                System.out.println("Recording saved as recording.mid");
            } catch (IOException | InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRecording() {
        try {
            File file = new File("recording.mid");
            Sequence sequence = MidiSystem.getSequence(file);
            sequencer.setSequence(sequence);
            System.out.println("Recording loaded from recording.mid");
        } catch (IOException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PianoGUI gui = new PianoGUI();
            gui.setVisible(true);
        });
    }
}
