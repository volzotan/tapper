package de.volzo.tapper;

import android.app.NotificationManager;
import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Locale;

import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.provider.AlarmClock;
import java.util.Calendar;

import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class ActionTriggers implements TextToSpeech.OnInitListener {
    private boolean lightOn;
    private Camera camera;
    private boolean cameraOpen;
    private TextToSpeech tts;
    private NotificationManager mNotificationManager;
    //private AlarmManager mAlarmManager;
    private Context context;

    //!! Grant Camera access (via Apps) & internet access (via Apps) & Phone Access (via Apps)
    // & Do not disturb access (via Sounds and Notifications)

    public ActionTriggers(Context context, NotificationManager mNotificationManager){
        this.context = context;
        this.mNotificationManager = mNotificationManager;
        //this.mAlarmManager = mAlarmManager;

        //FLASHLIGHT
        lightOn = false;
        cameraOpen = false;

        //TTS
        tts = new TextToSpeech(context, this);
        tts.setLanguage(Locale.US);
    }

    public void triggerAction(String action){
        switch (action){
            case "flashlight":
                flashlightTrigger();
                break;
            case "time":
                ttsTime();
                break;
            case "nextAlarm":
                ttsNextAlarm();
                break;
            case "play":
                play();
                break;
            case "pause":
                pause();
                break;
            case "previous":
                previous();
                break;
            case "next":
                next();
                break;
            case "doNotDisturb":
                doNotDisturb();
                break;
            case "disconnectCall":
                disconnectCall();
                break;
            case "disableAlarm":
                disableAlarm();
                break;
        }
    }

    public void flashlightTrigger(){
        if (lightOn){
            Parameters p = camera.getParameters();
            p.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            lightOn = false;
            camera.stopPreview();
            camera.release();
            cameraOpen = false;
        }
        else {
            camera = Camera.open();
            cameraOpen = true;
            Parameters p = camera.getParameters();
            p.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
            lightOn = true;
        }
    }

    public void ttsTime(){
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR);
        if(tts != null) {
            tts.speak("It is " + hours + " hours, " + minutes + " minutes and " + seconds + " seconds.", TextToSpeech.QUEUE_ADD, null);
        }
    }

    public void ttsNextAlarm(){
        String nextAlarm = Settings.System.getString(context.getContentResolver(),
                Settings.System.NEXT_ALARM_FORMATTED);
        if(nextAlarm.equals("")) {
            if(tts != null) {
                tts.speak("You have no upcoming alarms.", TextToSpeech.QUEUE_ADD, null);
            }
        }
        else{
            Log.i("info", "-"+nextAlarm+"-");
            if(tts != null) {
                tts.speak("The next alarm is on: " + nextAlarm, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

    public void play(){
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "play");
        context.sendBroadcast(i);
    }

    public void pause(){
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        context.sendBroadcast(i);
    }

    public void next(){
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "next");
        context.sendBroadcast(i);
    }

    public void previous(){
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "previous");
        context.sendBroadcast(i);
    }

    public void doNotDisturb(){
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
    }

    //for RESTRequest: need internet permission (turn on via apps -> this app -> permissions)
    //url for info about lights: GET http://<bridge ip address>/api/key/lights/lightid
    //url for turning light on/off: PUT http://<bridge ip address>/api/apikey/lights/1/state {"on":false}

    //After checking WISE Lab: they don't have a Philips Hue Light, but a Philips Livingcolor;
    //Which doesn't have WiFi access; only a remote which doesn't even use infrared, so
    //we cannot really interact with that light;

    //however, we are allowed to use an emulator
    //to show proof of concept: run this on a computer ( http://steveyo.github.io/Hue-Emulator/ )

    //Use the ip from the computer on the same network, and the port configured in the app
    //Apikey seems to be newdeveloper by default
    //Light number is the number of the light you want to toggle

    //First: do restrequest to get status of that light
    //then check if status and do restrequest put with on/off to toggle the lights status

    public void toggleHueLight(final String bridgeIP, final String bridgePort, final String apiKey, final String lightID){
        RequestQueue queue = Volley.newRequestQueue(context);
        final String urlGet = "http://" + bridgeIP + ":" + bridgePort + "/api/" + apiKey + "/lights/" + lightID;
        final String urlPut = urlGet + "/state";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlGet,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject jsonState = jsonResponse.getJSONObject("state");
                            final boolean stateOn = (Boolean) jsonState.get("on");
                            JSONObject jsonBody = new JSONObject();
                            RequestQueue queue = Volley.newRequestQueue(context);
                            if(stateOn == true){
                                jsonBody.put("on", "false");
                            }
                            if(stateOn == false){
                                jsonBody.put("on", "true");
                            }
                            final String requestBody = jsonBody.toString();
                            StringRequest putRequest = new StringRequest(Request.Method.PUT, urlPut,
                                    new Response.Listener<String>()
                                    {
                                        @Override
                                        public void onResponse(String response) {
                                        }
                                    },
                                    new Response.ErrorListener()
                                    {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast toast = Toast.makeText(context, "Couldn't complete RESTRequest to Hue", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }) {
                                @Override
                                public String getBodyContentType() {
                                    return "application/json; charset=utf-8";
                                }
                                @Override
                                public byte[] getBody() throws AuthFailureError {
                                    try {
                                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                                    } catch (UnsupportedEncodingException uee) {
                                        return null;
                                    }
                                }
                            };
                            queue.add(putRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(context, "Couldn't complete RESTRequest to Hue", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        queue.add(stringRequest);

    }

    //disconnect call
    //based on: http://stackoverflow.com/questions/20965702/end-incoming-call-programmatically
    //Allow phone permissions (VIA APPS) !!!

    public void disconnectCall(){
        try {
            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Disable alarm:

    public void disableAlarm(){
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        context.startActivity(intent);
    }


    public void onDestroy(){
        //FLASHLIGHT
        if(cameraOpen) { camera.release(); }

        //TTS
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    };

    @Override
    public void onInit(int status) {
    }
}
