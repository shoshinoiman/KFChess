package server.listener;

import java.awt.Toolkit;
import javax.sound.sampled.*;

import server.events.GameEvent;
import server.events.IEventListener;

public class SoundListener implements IEventListener {
    
    @Override
    public void onEvent(GameEvent event) {
        SoundType soundType = getSoundTypeForEvent(event.getType());
        playSound(soundType);
    }
    
    /**
     * מחזיר את סוג הצליל המתאים לאירוע
     */
    private SoundType getSoundTypeForEvent(String eventType) {
        switch (eventType) {
            case GameEvent.PIECE_MOVED:
                return SoundType.MOVE;
            case GameEvent.PIECE_CAPTURED:
                return SoundType.CAPTURE;
            case GameEvent.GAME_STARTED:
                return SoundType.GAME_START;
            case GameEvent.GAME_ENDED:
                return SoundType.GAME_END;
            case GameEvent.PIECE_JUMPED:
                return SoundType.JUMP;
          
            default:
                return SoundType.DEFAULT;
        }
    }
    
    /**
     * מנגן צליל בהתאם לסוג
     */
    private void playSound(SoundType soundType) {
        try {
            switch (soundType) {
                case MOVE:
                    playTone(440, 200, 0.3); // צליל A4 קצר
                    break;
                case CAPTURE:
                    playTone(330, 150, 0.5); // צליל E4 חזק
                    Thread.sleep(50);
                    playTone(220, 150, 0.5); // ואחריו A3
                    break;
                case JUMP:
                    playTone(659, 100, 0.4); // צליל E5 קצר וחד
                    Thread.sleep(50);
                    playTone(659, 100, 0.4); // כפול לאפקט קפיצה
                    break;
                case PIECE_SELECT:
                    playTone(523, 100, 0.2); // צליל C5 רך וקצר
                    break;
                case ERROR:
                    playTone(147, 400, 0.6); // צליל נמוך וארוך לשגיאה
                    break;
                case GAME_START:
                    // מנגינת התחלה
                    playTone(523, 200, 0.4); // C5
                    Thread.sleep(100);
                    playTone(659, 200, 0.4); // E5
                    Thread.sleep(100);
                    playTone(784, 300, 0.5); // G5
                    break;
                case GAME_END:
                    // מנגינת סיום
                    playTone(523, 300, 0.4); // C5
                    Thread.sleep(100);
                    playTone(392, 300, 0.4); // G4
                    Thread.sleep(100);
                    playTone(330, 500, 0.5); // E4 ארוך
                    break;
                case DEFAULT:
                    playSystemBeep();
                    break;
            }
        } catch (Exception e) {
            System.err.println("שגיאה בניגון צליל: " + e.getMessage());
        }
    }
    
    /**
     * מנגן צליל מערכת בסיסי
     */
    private void playSystemBeep() {
        Toolkit.getDefaultToolkit().beep();
    }
    
    /**
     * מנגן טון בתדירות מסוימת
     * @param frequency תדירות בהרץ
     * @param duration משך זמן במילישניות
     * @param volume עוצמה (0.0 - 1.0)
     */
    private void playTone(int frequency, int duration, double volume) {
        try {
            // יצירת format עבור הצליל
            AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100, // sample rate
                16,    // bits per sample
                1,     // channels (mono)
                2,     // frame size
                44100, // frame rate
                false  // big endian
            );
            
            // יצירת buffer לצליל
            int bufferSize = (int) (audioFormat.getSampleRate() * duration / 1000);
            byte[] buffer = new byte[bufferSize * 2]; // 16-bit = 2 bytes per sample
            
            // יצירת גל סינוס
            for (int i = 0; i < bufferSize; i++) {
                double angle = 2.0 * Math.PI * i * frequency / audioFormat.getSampleRate();
                short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * volume);
                
                // המרה ל-bytes (little endian)
                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            // ניגון הצליל
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
            
        } catch (LineUnavailableException e) {
            // אם יש בעיה עם הצליל הסינתטי, נחזור לצליל מערכת
            playSystemBeep();
        }
    }
    
    /**
     * enum לסוגי צלילים שונים
     */
    private enum SoundType {
        MOVE,
        CAPTURE,
        GAME_START,
        GAME_END,
        PIECE_SELECT,
        ERROR,
        JUMP,
        DEFAULT
    }
}
