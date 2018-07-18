package ai.kitt.snowboy.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ai.kitt.snowboy.AlexaReceiver;
import ai.kitt.snowboy.R;
import ai.kitt.snowboy.adapter.AlarmsAdapter;
import ai.kitt.snowboy.data.DatabaseHelper;
import ai.kitt.snowboy.model.Alarm;
import ai.kitt.snowboy.service.AlarmReceiver;
import ai.kitt.snowboy.service.LoadAlarmsReceiver;
import ai.kitt.snowboy.service.LoadAlarmsService;
import ai.kitt.snowboy.util.AlarmUtils;
import ai.kitt.snowboy.view.DividerItemDecoration;
import ai.kitt.snowboy.view.EmptyRecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public final class MainFragment extends Fragment
        implements LoadAlarmsReceiver.OnAlarmsLoadedListener {

    private LoadAlarmsReceiver mReceiver;
    private AlarmsAdapter mAdapter;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    TextToSpeech t1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadAlarmsReceiver(this);
        getActivity().startService(new Intent(getContext(), AlexaReceiver.class));
    }

//    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main, container, false);

        final EmptyRecyclerView rv = (EmptyRecyclerView) v.findViewById(R.id.recycler);
        mAdapter = new AlarmsAdapter();
        rv.setEmptyView(v.findViewById(R.id.empty_view));
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext()));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());
        t1=new TextToSpeech(this.getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    t1.setLanguage(Locale.getDefault());
                    Log.d("Error", "Highly suspicious1");
                }
            }
        });
        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmUtils.checkAlarmPermissions(getActivity());
                final Intent i =
                        AddEditAlarmActivity.buildAddEditAlarmActivityIntent(
                                getContext(), AddEditAlarmActivity.ADD_ALARM
                        );
                startActivity(i);
            }
        });
        final FloatingActionButton audio = (FloatingActionButton) v.findViewById(R.id.audio);
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                askSpeechInput();
                getActivity().stopService(new Intent(getContext(), AlexaReceiver.class));
            }
        });
        return v;

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
                    Log.d("input speech", speech);
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
//                        getArguments().getParcelable(AddEditAlarmActivity.ALARM_EXTRA);
                    }
                }
                break;
            }

        }
    }

    private Alarm getAlarm(){
        final long id = DatabaseHelper.getInstance(getContext()).addAlarm();
        LoadAlarmsService.launchLoadAlarmsService(getContext());
        return new Alarm(id);
    }
    private void save(int hours, int minutes, String speech) {
        final Alarm alarm = getAlarm();
        final Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hours);
        time.set(Calendar.MINUTE, minutes);
        alarm.setTime(time.getTimeInMillis());
        speech = speech.toLowerCase();
        alarm.setLabel("new alarm");

        if(speech.contains("everyday")){
            alarm.setDay(Alarm.MON, true);
            alarm.setDay(Alarm.TUES, true);
            alarm.setDay(Alarm.WED, true);
            alarm.setDay(Alarm.THURS, true);
            alarm.setDay(Alarm.FRI, true);
            alarm.setDay(Alarm.SAT, true);
            alarm.setDay(Alarm.SUN, true);
        }
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

        final int rowsUpdated = DatabaseHelper.getInstance(getContext()).updateAlarm(alarm);
        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;

        Toast.makeText(getContext(), messageId, Toast.LENGTH_LONG).show();
        AlarmReceiver.setReminderAlarm(getContext(), alarm);
    }
    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(LoadAlarmsService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
        LoadAlarmsService.launchLoadAlarmsService(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onAlarmsLoaded(ArrayList<Alarm> alarms) {
        mAdapter.setAlarms(alarms);
    }

}
