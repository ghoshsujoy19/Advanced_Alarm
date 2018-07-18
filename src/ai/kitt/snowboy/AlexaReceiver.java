package ai.kitt.snowboy;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ai.kitt.snowboy.audio.AudioDataSaver;
import ai.kitt.snowboy.audio.PlaybackThread;
import ai.kitt.snowboy.audio.RecordingThread;
import ai.kitt.snowboy.ui.MainActivity;

public class AlexaReceiver extends Service {
    public int counter=0;
    private int alexaCount=0;
    private int preVolume = -1;
    private RecordingThread recordingThread;
//    private PlaybackThread playbackThread;


    public AlexaReceiver(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public AlexaReceiver() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        setProperVolume();
        recordingThread = new RecordingThread(handle, new AudioDataSaver());
        startRecording();
//        playbackThread = new PlaybackThread();
        return START_STICKY;
    }

    private void startRecording() {
        recordingThread.startRecording();
    }

    private void stopRecording() {
        recordingThread.stopRecording();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("HERE", "here I am closing!");
        stopRecording();
//        t.stop();
        restoreVolume();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent();
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    Thread t = new Thread(){
        public void run(){
            Message mess = new Message();
            Bundle res = new Bundle();
            res.putString("status", "SUCCESS");
            mess.obj = res;
            handy.sendMessage(mess);
        }
    };

    String toShow="Active "+alexaCount;

    @SuppressLint("HandlerLeak")
    private Handler handy = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            Toast.makeText(getApplicationContext(), toShow, Toast.LENGTH_LONG).show();
        }
    };

    @SuppressLint("HandlerLeak")
    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            switch(message) {
                case MSG_ACTIVE:
                    alexaCount++;
//                    updateLog(" ----> Detected " + activeTimes + " times", "green");
                    // Toast.makeText(Demo.this, "Active "+activeTimes, Toast.LENGTH_SHORT).show();
                    toShow = "Active "+ alexaCount;
//                    t.start();
//                    t.stop();
//                    Toast.makeText(getApplicationContext(), toShow, Toast.LENGTH_SHORT).show();
//                    showToast("Active "+activeTimes);
                    Intent dt = new Intent(getApplicationContext(), MainActivity.class);
                    dt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dt);
                    break;
                case MSG_INFO:
//                    updateLog(" ----> "+message);
                    break;
                case MSG_VAD_SPEECH:
//                    updateLog(" ----> normal voice", "blue");
                    break;
                case MSG_VAD_NOSPEECH:
//                    updateLog(" ----> no speech", "blue");
                    break;
                case MSG_ERROR:
//                    updateLog(" ----> " + msg.toString(), "red");
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void setProperVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        preVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        updateLog(" ----> preVolume = "+preVolume, "green");
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        updateLog(" ----> maxVolume = "+maxVolume, "green");
        int properVolume = (int) ((float) maxVolume * 0.2);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, properVolume, 0);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        updateLog(" ----> currentVolume = "+currentVolume, "green");
    }

    private void restoreVolume() {
        if(preVolume>=0) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
//            updateLog(" ----> set preVolume = "+preVolume, "green");
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            updateLog(" ----> currentVolume = "+currentVolume, "green");
        }
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
