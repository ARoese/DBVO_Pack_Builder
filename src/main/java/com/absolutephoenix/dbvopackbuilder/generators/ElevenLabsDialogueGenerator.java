package com.absolutephoenix.dbvopackbuilder.generators;

import com.absolutephoenix.dbvopackbuilder.config.ConfigManager;
import com.absolutephoenix.dbvopackbuilder.utils.AudioManipulation;
import com.absolutephoenix.dbvopackbuilder.utils.ElevenLabsFileHandling;
import net.andrewcpu.elevenlabs.model.voice.Voice;

public class ElevenLabsDialogueGenerator implements DialogueGenerator {
    double stability;
    double clarity;
    double style;
    Voice voice;
    public ElevenLabsDialogueGenerator(double stability, double clarity, double style, Voice voice){
        this.stability = stability;
        this.clarity = clarity;
        this.style = style;
        this.voice = voice;
    }

    @Override
    public boolean generateDialogue(String dialogue, String outFileName) {
        boolean result = ElevenLabsFileHandling.saveStreamAsMp3(voice, stability, clarity, style, dialogue, outFileName);
        if(result){
            AudioManipulation.convertMP3ToWAV(dialogue);
        }

        return result;
    }
}
