package com.serp1983.nokiacomposer.lib;

import android.annotation.SuppressLint;

import com.serp1983.nokiacomposer.domain.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PCMConverter {

    public final static int SAMPLING_FREQUENCY = 4000; // min support value for AudioTrack
	
	private static PCMConverter instance;	
	public static PCMConverter getInstance(){	
		if (instance == null) return instance = new PCMConverter();
		return instance;
	}
	
	public static byte[] shorts2Bytes(ShortArrayList pcm){	
		byte[] array = new byte[pcm.size() * 2];
		int i = 0;
		for(short data : pcm.toArray()){
			array[i++] = (byte) (data & 0xff);
			array[i++] = (byte) ((data >>> 8) & 0xff);
		}
		return array;
	}

	private final Map<String, Float> notes = new HashMap<>();
	private final Map<String, String> tones = new HashMap<>();

	private PCMConverter(){
		notes.put("-", 0f);
		notes.put("C", 261.626f); 
		notes.put("#C", 277.183f); 
		notes.put("D", 293.665f); 
		notes.put("#D", 311.127f); 
		notes.put("E", 329.628f); 
		notes.put("#E", 349.228f); 
		notes.put("F", 349.228f); 
		notes.put("#F", 369.994f); 
		notes.put("G", 391.995f); 
		notes.put("#G", 415.305f); 
		notes.put("A", 440.000f); 
		notes.put("#A", 466.164f); 
		notes.put("B", 493.883f); 
		notes.put("#B", 523.251f);

		tones.put("-", "0");
		tones.put("C", "1");
		tones.put("#C", "1#");
		tones.put("D", "2");
		tones.put("#D", "2#");
		tones.put("E", "3");
		tones.put("#E", "3#");
		tones.put("F", "4");
		tones.put("#F", "4#");
		tones.put("G", "5");
		tones.put("#G", "5#");
		tones.put("A", "6");
		tones.put("#A", "6#");
		tones.put("B", "7");
		tones.put("#B", "7#");
	}
	
	private void appendNote(ShortArrayList pcm, float volume, int time, String note, int octave, int samplingType){
		float freq = (float) (notes.get(note) * Math.pow(2, octave-1));
		double val = 0;
		short value;
		int i;

		int max = SAMPLING_FREQUENCY * time / 1000;

		pcm.add((short)0);
		for(i = 1; i <= max-1; i++){
			val = SamplingType.getValByTime(samplingType, freq, i);
			value = (short) (32765f * volume * val);
			pcm.add(value);
		}

		// making clear sound
        while (Math.abs(val)>0.1f){
            val = SamplingType.getValByTime(samplingType, freq, i);
            value = (short) (32765f * volume * val);
            pcm.add(value);
            i++;
        }
        pcm.add((short)0);
	}

	public ShortArrayList convert(String nokiaCodes, float tempo /*120*/){
		return convert(nokiaCodes, tempo, 1f, SamplingType.Default);
	}

	public ShortArrayList convert(String nokiaCodes, float tempo /*120*/, int samplingType){
		return convert(nokiaCodes, tempo, 1f, samplingType);
	}

	@SuppressLint("DefaultLocale")
	private ShortArrayList convert(String nokiaCodes, float tempo /*120*/, float volume /*1*/, int samplingType){
		ShortArrayList pcm = new ShortArrayList();
		appendNote(pcm, 0, 100, "-", 1, samplingType);
		for(String token : Note.getTokens(nokiaCodes)){
			Note noteObj = new Note(token);
			int duration = noteObj.getDuration();
			Integer octave = noteObj.getOctave();
            if (octave == null)
                octave = 1;
			String note = noteObj.getNote();

			if (token.contains("."))
				duration = duration * 2 / 3;
			
			float time = 32f / duration; 			
			appendNote(pcm, volume, (int) (time * 1000f * 7.5f / tempo), note, octave, samplingType);
		}
		appendNote(pcm, 0, 100, "-", 1, samplingType);

		return pcm;
	}

	public String convert2Keys(String nokiaCodes){
		List<String> result = new ArrayList<>();
		int prevDuration = 4;
		int prevOctave = 1;
		for(String token : Note.getTokens(nokiaCodes)){
			Note noteObj = new Note(token);
			int duration = noteObj.getDuration();
			Integer octave = noteObj.getOctave();
			if (octave == null)
                octave = 1;
			String note = noteObj.getNote();

			String keyForNote= tones.get(note);
			if (token.contains("."))
				keyForNote = "(" + keyForNote + ")";

			result.add(keyForNote
					+ getKeysForDuration(prevDuration, duration)
					+ getKeysForOctave(note, prevOctave, octave)
			);

			if (!"-".equals(note)) {
				prevDuration = duration;
				prevOctave = octave;
			}
		}

		StringBuilder strBuilder = new StringBuilder();
		for (String token : result) {
			strBuilder.append(token);
			strBuilder.append(", ");
		}

		return strBuilder.toString();
	}

	private static String getKeysForDuration(int prevDuration, int duration){
		if (prevDuration == duration)
			return "";

		int divDuration = duration > prevDuration ? duration / prevDuration : prevDuration / duration;
		int log2Duration = 1;
		if (divDuration == 2) log2Duration = 1;
		if (divDuration == 4) log2Duration = 2;
		if (divDuration == 8) log2Duration = 3;
		if (divDuration == 16) log2Duration = 4;
		if (divDuration == 32) log2Duration = 5;

		String tone = duration > prevDuration ? "8" : "9";
		String res = "";
		for (int i = 0; i < log2Duration; i++)
			res += tone;

		return res;
	}

	private static String getKeysForOctave(String note, int prevOctave, int octave) {
		if ("-".equals(note) || octave == prevOctave)
			return "";

		int count = 0;
		if (prevOctave == 1 && octave == 2) count = 1;
		if (prevOctave == 1 && octave == 3) count = 2;
		if (prevOctave == 2 && octave == 1) count = 2;
		if (prevOctave == 2 && octave == 3) count = 1;
		if (prevOctave == 3 && octave == 1) count = 1;
		if (prevOctave == 3 && octave == 2) count = 2;

		String res = "";
		for (int i = 0; i < count; i++)
			res += "*";

		return res;
	}
}

