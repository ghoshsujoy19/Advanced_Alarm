package ai.kitt.snowboy.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.kitt.snowboy.AppResCopy;
import ai.kitt.snowboy.R;
//import ai.kitt.snowboy.adapter.AlarmsAdapter;
import ai.kitt.snowboy.data.DatabaseHelper;
import ai.kitt.snowboy.model.Alarm;
import ai.kitt.snowboy.service.AlarmReceiver;
//import ai.kitt.snowboy.service.LoadAlarmsReceiver;
import ai.kitt.snowboy.service.LoadAlarmsService;

public class MainActivity extends AppCompatActivity {
//    private LoadAlarmsReceiver mReceiver;
//    private AlarmsAdapter mAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppResCopy.copyResFromAssetsToSD(this);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    t1.setLanguage(Locale.getDefault());
                    Log.d("Error", "Highly suspicious1");
                }
            }
        });
        askSpeechInput();
    }

    public void onDestroy(){
        Log.d("Error", "Highly suspicious7");
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    private void askSpeechInput() {
        String msg = "Yes Boss! How can I help you?";
        t1.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi speak something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            Log.d("Error", "Highly suspicious2");
        } catch (ActivityNotFoundException a) {
            Log.d("Error", "Highly suspicious3");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Error", "Highly suspicious4");
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                Log.d("Error", "Highly suspicious5");
                if (resultCode == RESULT_OK && null != data) {
                    Log.d("Error", "Highly suspicious6");
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String speech = result.get(0);
                    Log.d("Error", speech);
//                    t1.speak(speech, TextToSpeech.QUEUE_FLUSH,null, null);
                    if(speech.contains("alarm"))
                    {
                        boolean night = true;
                        if(speech.contains("a.m.") || speech.contains("morning"))
                        {
                            night = false;
                        }
                        if(speech.contains(":"))
                        {
                            String regex = "([0-9]{1,2}):([0-9]{1,2})";
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(speech);
                            if(m.find())
                            {
                                String grp1 = m.group(1);
                                String grp2 = m.group(2);
                                Log.d("Error", grp1+":"+grp2);
                                int hr = Integer.parseInt(grp1);
                                if(night)
                                {
                                    hr = (hr+12)%24;
                                }
                                int minutes = Integer.parseInt(grp2);
                                save(hr, minutes, speech);
                            }
                        }
                        else
                        {
                            String regex = "([0-9]{1,2})";
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(speech);
                            if(m.find())
                            {
                                String grp = m.group(1);
                                int hr = Integer.parseInt(grp);
                                int minutes = 0;
                                if(night)
                                {
                                    hr = (hr+12)%24;
                                }
                                Log.d("Error", grp);
                                save(hr, minutes, speech);
                            }
                        }
//                        if(speech.contains("interval")){
//
//                        }
                    }
                }
                break;
            }

        }
    }

    private Alarm getAlarm(){
        final long id = DatabaseHelper.getInstance(getApplicationContext()).addAlarm();
        LoadAlarmsService.launchLoadAlarmsService(getApplicationContext());
        return new Alarm(id);
    }
    private void save(int hours, int minutes, String xyz) {
        final Alarm alarm = getAlarm();
        final Calendar time = Calendar.getInstance();
        String nm = time.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        Log.d("Current_day", nm);
        time.set(Calendar.HOUR_OF_DAY, hours);
        time.set(Calendar.MINUTE, minutes);
        alarm.setTime(time.getTimeInMillis());
        String speech = xyz.toLowerCase()+nm.toLowerCase();
        if(speech.contains("monday") && !speech.contains("except")){
            alarm.setDay(Alarm.MON, true);
        }
        else{
            alarm.setDay(Alarm.MON, false);
        }

        if(speech.contains("tuesday") && !speech.contains("except")){
            alarm.setDay(Alarm.TUES, true);
        }
        else{
            alarm.setDay(Alarm.TUES, false);
        }

        if(speech.contains("wednesday") && !speech.contains("except")){
            alarm.setDay(Alarm.WED, true);
        }
        else{
            alarm.setDay(Alarm.WED, false);
        }

        if(speech.contains("thursday") && !speech.contains("except")){
            alarm.setDay(Alarm.THURS, true);
        }
        else{
            alarm.setDay(Alarm.THURS, false);
        }

        if(speech.contains("friday") && !speech.contains("except")){
            alarm.setDay(Alarm.FRI, true);
        }
        else{
            alarm.setDay(Alarm.FRI, false);
        }

        if(speech.contains("saturday") && !speech.contains("except")){
            alarm.setDay(Alarm.SAT, true);
        }
        else{
            alarm.setDay(Alarm.SAT, false);
        }

        if(speech.contains("sunday") && !speech.contains("except")){
            alarm.setDay(Alarm.SUN, true);
        }
        else{
            alarm.setDay(Alarm.SUN, false);
        }
        if(speech.contains("everyday")){
            alarm.setDay(Alarm.MON, true);
            alarm.setDay(Alarm.TUES, true);
            alarm.setDay(Alarm.WED, true);
            alarm.setDay(Alarm.THURS, true);
            alarm.setDay(Alarm.FRI, true);
            alarm.setDay(Alarm.SAT, true);
            alarm.setDay(Alarm.SUN, true);
        }
        final int rowsUpdated = DatabaseHelper.getInstance(getApplicationContext()).updateAlarm(alarm);
        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;

        Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_LONG).show();
        AlarmReceiver.setReminderAlarm(getApplicationContext(), alarm);
    }

}
