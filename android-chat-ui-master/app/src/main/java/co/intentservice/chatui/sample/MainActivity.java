package co.intentservice.chatui.sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.micronic.micron1.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
import static android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

public class MainActivity extends AppCompatActivity {

    public static class MessageEvent {
        public final String msg;

        public MessageEvent(String msg) {
            this.msg = msg;
        }
    }

    private static final String[] PERMS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG};

    private ChatView chatView;

    private TextToSpeech tts;

    private boolean isjoke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        askPermissions();
        setContentView(R.layout.activity_main);

        chatView = (ChatView) findViewById(R.id.chat_view);
        addMessage(new ChatMessage("Dexter is initializing. Please wait...",
                System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
        Bot.init();
        chatView.getInputEditText().setEnabled(false);
        chatView.getActionsMenu().setEnabled(false);
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                if (chatMessage.getMessage().isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, VoiceActivity.class);
                    startActivityForResult(intent, 1239);
                    return false;
                } else {
                    Bot.handle(chatMessage.getMessage());
                    return true;
                }
            }
        });
        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {

            }


            @Override
            public void userStoppedTyping() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1239 && data != null && data.hasExtra("msg")) {
            onMessageEvent(new MessageEvent(data.getStringExtra("msg")));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event == null || event.msg == null || event.msg.isEmpty())
            return;
        if (event.msg.equals(":init")) {
            chatView.getInputEditText().setEnabled(true);
            chatView.getActionsMenu().setEnabled(true);
            addMessage(new ChatMessage("Dexter is all set and ready to rock.",
                    System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
        } else if (event.msg.equals(":shut")) {
            if (tts != null && tts.isSpeaking())
                tts.stop();
        } else if (event.msg.startsWith(":voice:")) {
            String voiceInput = event.msg.substring(7);
            addMessage(new ChatMessage(voiceInput,
                    System.currentTimeMillis(), ChatMessage.Type.SENT));
            Bot.handle(voiceInput);
        } else if (event.msg.startsWith(":device:")) {
            String deviceInput = event.msg.substring(8);
            handleDeviceInput(deviceInput);
        } else {
            addMessage(new ChatMessage(event.msg,
                    System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
        }
    }

    private void handleDeviceInput(final String input) {

        String adi[] = input.split("\\s+");
        //handle device input here...
        if (input.equalsIgnoreCase("turn on flashlight")) {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, true);
                write("Flashlight turned on");
            } catch (CameraAccessException e) {
            }
        } else if (input.toLowerCase().startsWith("meaning of") || input.toLowerCase().startsWith("definition of")) {
            String word = input.replaceFirst("(?i)(meaning|definition) of\\s+", "");
            write(Utils.getMeaning(word));
        } else if (input.equalsIgnoreCase("turn off flashlight")) {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
                String cameraId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(cameraId, false);

                write("Flashlight turned off");
            } catch (CameraAccessException e) {
            }
        } else if (adi[0].equalsIgnoreCase("open")) {           //For opening any app
            if (adi.length > 1) {
                List<ApplicationInfo> packages;
                PackageManager pm1;
                pm1 = getPackageManager();

                // get a list of installed apps.
                packages = pm1.getInstalledApplications(0);                                     //List of all installed apps

                int flag = 0;
                for (ApplicationInfo packageInfo : packages) {

                    final String applicationName = (String) (packageInfo != null ? pm1
                            .getApplicationLabel(packageInfo) : "(unknown)");
                    Log.d("applicationName=" + applicationName, "package name="
                            + packageInfo.packageName);
                    if (adi[1].equalsIgnoreCase(applicationName)) {
                        flag = 1;
                        Intent launchIntent = pm1.getLaunchIntentForPackage(packageInfo.packageName);
                        startActivity(launchIntent);
                        String aaa = "Opening" + applicationName;
                        write(aaa);

                    }
                }

                if (flag == 0 && adi.length < 3) {                                                                //If app doesn't found
                    write("No such app on your device");
                }
            } else {
                write("open what?");

            }
        } else if (input.trim().split(" ", 2)[0].equalsIgnoreCase("direction")) {
            String dest = input.trim().split(" ", 2)[1];
//            String geoUri = "http://maps.google.com/maps?q="+ dest ;

//            String uri = String.format(Locale.ENGLISH, "geo:0,0?q="+dest);
            write("opening maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + dest));
            startActivity(intent);
        } else if (adi[0].equalsIgnoreCase("call")) {           //For opening any app
            try {
                adi = input.split(" ", 2);
                if (adi[1].matches("[0-9]+") || (adi[1].charAt(0) < 58 && adi[1].charAt(0) > 46)) {

                    String out = "";
                    out = adi[1].replace(" ", "");
                    //Toast.makeText(this,out,Toast.LENGTH_LONG).show();
//                for (int i=0;i<adi[1].length();i++){
//                    if((adi[1].charAt(i)<58 && adi[1].charAt(i)>47)){
//                        out+=adi[1].charAt(i);
//
//                    }else{
//                        speak("sorry.  incorrect number");
//                        return;
//
//                    }

//                }
                    if (out.matches("\\d+")) {
                        Intent call = new Intent(Intent.ACTION_CALL);
                        String num = adi[1].replaceAll("\\s+", "");
                        call.setData(Uri.parse("tel:" + out));

                        startActivity(call);

                        String queryString = "NUMBER='" + Integer.parseInt(out) + "'";
                        this.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
                    } else {
                        write("incorrect number");
                    }
                } else {
                    int flag = 0;

                    //Toast.makeText(this, "not found in favourite", Toast.LENGTH_LONG).show();
                    ContentResolver cr = getContentResolver();
                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);

                    if ((cur != null ? cur.getCount() : 0) > 0) {
                        while (cur != null && cur.moveToNext()) {
                            String id = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(cur.getColumnIndex(
                                    ContactsContract.Contacts.DISPLAY_NAME));

                            if (cur.getInt(cur.getColumnIndex(
                                    ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                                Cursor pCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        new String[]{id}, null);
                                pCur.moveToNext();

                                String phoneNo = pCur.getString(pCur.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                                if (adi[1].equalsIgnoreCase(name)) {

                                    flag = 1;
                                    Intent call = new Intent(Intent.ACTION_CALL);
                                    call.setData(Uri.parse("tel:" + phoneNo));
                                    startActivity(call);
                                    break;
                                }
                                Log.i(TAG, "Name: " + name);
                                Log.i(TAG, "Phone Number: " + phoneNo);

                                pCur.close();

                            }
                        }
                    }
                    if (cur != null) {
                        cur.close();
                    }
                    if (flag == 0) {
                        Toast.makeText(this, "No such contact in your device", Toast.LENGTH_LONG).show();
                    }

                }

            } catch (SecurityException e) {
            }

        } else if (input.equalsIgnoreCase("mode silent")) {
            final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            write("Silent mode on");
        } else if (input.equalsIgnoreCase("mode vibrate")) {
            final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            write("Vibrate mode on");
        } else if (input.equalsIgnoreCase("mode general")) {
            final AudioManager mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            write("General mode on");
        }
//        else if (input.equalsIgnoreCase("mode do not disturb")) {
//            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
//            write("DND mode on");
//        }
        else if (input.equalsIgnoreCase("vibrate device")) {
            Vibrator mVibrator;
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Check whether device hardware has a Vibrator
            if (mVibrator.hasVibrator()) {
                Toast.makeText(this, "Device has a Vibrator.", Toast.LENGTH_LONG).show();
                mVibrator.vibrate(15000);
            }
            write("Vibrating");
        } else if (input.equalsIgnoreCase("turn on wi-fi")) {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
            write("Wi Fi turned on");

        } else if (input.equalsIgnoreCase("turn off wi-fi")) {
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(false);
            write("Wi Fi turned off");
        } else if (input.equalsIgnoreCase("turn on bluetooth")) {
            BluetoothAdapter bt = null;
            bt = BluetoothAdapter.getDefaultAdapter();
            bt.enable();
            write("Bluetooth turned on");
        } else if (input.equalsIgnoreCase("turn off bluetooth")) {
            BluetoothAdapter bt = null;
            bt = BluetoothAdapter.getDefaultAdapter();
            bt.disable();
            write("Bluetooth turned off");
        } else if (input.equalsIgnoreCase("increase brightness")) {

            try {

                ContentResolver cResolver = this.getApplicationContext().getContentResolver();
                int curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                if (curBrightnessValue < 235) {
                    curBrightnessValue += 20;
                } else curBrightnessValue = 255;
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, curBrightnessValue);

                write("Brightness increased");
            } catch (Exception e) {
            }
        } else if (input.equalsIgnoreCase("decrease brightness")) {

            try {

                ContentResolver cResolver = this.getApplicationContext().getContentResolver();
                int curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                if (curBrightnessValue > 20) {
                    curBrightnessValue -= 20;
                } else curBrightnessValue = 0;
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, curBrightnessValue);

                write("Brightness decreased");
            } catch (Exception e) {
            }
        } else if (input.equalsIgnoreCase("full brightness")) {
            ContentResolver cResolver = this.getApplicationContext().getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
            write("Brightness set to maximum");
        } else if (input.equalsIgnoreCase("low brightness")) {
            ContentResolver cResolver = this.getApplicationContext().getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 0);
            write("Brightness set to lowest");
        } else if (input.equalsIgnoreCase("turn on adaptive brightness")) {

            Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            write("Adaptive brightness turned on");
        } else if (input.equalsIgnoreCase("turn off adaptive brightness")) {

            Settings.System.putInt(getContentResolver(), SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_MANUAL);
            write("Adaptive brightness turned off");
        } else if (input.equalsIgnoreCase("Flip a coin") || input.equalsIgnoreCase("Flip coin")) {

            Random random = new Random();
            int n = random.nextInt(2);
            if (n == 0) {

                try {

                    MediaPlayer mPlayer2;
                    mPlayer2 = MediaPlayer.create(this, R.raw.coinflip);
                    mPlayer2.start();
                } catch (Exception e) {
                }


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        write("You got HEADS");
                    }
                }, 1000);

            } else {

                try {
                    MediaPlayer mPlayer2;
                    mPlayer2 = MediaPlayer.create(this, R.raw.coinflip);
                    mPlayer2.start();
                } catch (Exception e) {
                }


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        write("You got TAILS");
                    }
                }, 1000);
            }
        } else if (input.equalsIgnoreCase("Roll a dice") || input.equalsIgnoreCase("Roll dice")) {

            Random random = new Random();
            final int n = random.nextInt(6);
            try {

                MediaPlayer mPlayer2;
                mPlayer2 = MediaPlayer.create(this, R.raw.rolldice);
                mPlayer2.start();
            } catch (Exception e) {
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    write("You got \n" + (n + 1));
                }
            }, 1000);

        } else if (input.equalsIgnoreCase("give me an applause") || input.equalsIgnoreCase("give me applause")) {
            write("ok");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    MediaPlayer mPlayer2;
                    mPlayer2 = MediaPlayer.create(MainActivity.this, R.raw.applause);
                    mPlayer2.start();
                }
            }, 1000);


        } else if (input.equalsIgnoreCase("tell me joke") || input.equalsIgnoreCase("tell me a joke") || input.equalsIgnoreCase("tell a joke")) {
            final String[] JOKES = {
                    "A computer once beat me at chess, but it was no match for me at kick boxing.",
                    "What did one ocean say to the other ocean? Nothing, they just waved.",
                    "If you owe the bank $100,000, the bank owns you. If you owe the bank $1,000,000,000, you own the bank.",
                    "When everything's coming your way, you're in the wrong lane.",
                    "Whenever I find the key to success, someone changes the lock.",
                    "Why did the bee get married? Because he found his honey.",
                    "I just let my mind wander, and it didn't come back.",
                    "IRS: We've got what it takes to take what you've got.",
                    "I asked God for a bike, but I know God works in mysterious ways. So I stole a bike and asked for forgiveness.",
                    "If I agreed with you we'd both be wrong.",
                    "If God is watching us, the least we can do is be entertaining.",
                    "I like work: it fascinates me. I can sit and look at it for hours.",
                    "Yo momma is so fat, when she sat on an iPod, she made the iPad!",
                    "Q: What did the spider do on the computer? A: Made a website!",
                    "Q: What did the computer do at lunchtime? A: Had a byte!",
                    "Q: What does a baby computer call his father? A: Data!",
                    "Q: Why did the computer keep sneezing? A: It had a virus!",
                    "Q: What is a computer virus? A: A terminal illness!",
                    "Q: Why was the computer cold? A: It left it's Windows open!",
                    "Q: Why was there a bug in the computer? A: Because it was looking for a byte to eat?",
                    "Q: Why did the computer squeak? A: Because someone stepped on it's mouse!",
                    "Q: What do you get when you cross a computer and a life guard? A: A screensaver!",
                    "Q: Where do all the cool mice live? A: In their mousepads",
                    "Q: What do you get when you cross a computer with an elephant? A: Lots of memory!",
                    "Yo momma so fat when she registered for MySpace there was no space left.",
                    "I decided to make my password \"incorrect\" because if I type it in wrong, my computer will remind me, \"Your password is incorrect.\"",
                    "I put my phone on airplane mode, but it sure ain't flyin'.",
                    "I’m employed at a computer security company and have a colleague whose name is M. Alware. His e-mail address is malware@company.com.",
                    "Client to designer: “It doesn’t really look purple. It looks more like a mixture of red and blue.”",
                    "Instagram is just Twitter for people who go outside.",
                    "I put so much more effort into naming my first Wi-Fi than my first child.",
                    "I was in a couple’s home trying to fix their Internet connection.",
                    "I can still remember a time when I knew more than my phone.",
                    "Daughter: I got an A in Chemistry. Mom: WTF! Daughter: Mom, what do you think WTF means? Mom: Well That’s Fantastic.",
                    "Mom: What do IDK, LY & TTYL mean? Son: I don’t know, love you, talk to you later. Mom: OK, I will ask your sister.",
                    "Before LinkedIn, I didn’t know any strangers.",
                    "Give a man a fish, and he’ll Instagram it; teach a man to fish, and he’ll still Instagram it.",
                    "I Renamed my iPod The Titanic, so when I plug it in, it says, “The Titanic is syncing.”",
                    "Once upon a time, a computer programmer drowned at sea. Many were on the beach and heard him cry out, “F1! F1!”, but no one understood.",
                    "Yo momma so ugly an uber driver would rather get 1-star review than pick her up.",
            };
            final Random rndmGenerator = new Random();
            isjoke = true;
            write(JOKES[rndmGenerator.nextInt(JOKES.length)]);

        } else if (input.equalsIgnoreCase("give me password") || input.equalsIgnoreCase("new password")) {
            String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ?/.,";
            Random rnd = new Random();
            char car;
            String s = "";
            for (int i = 0; i < 8; i++)
                s += car = alphabet.charAt(rnd.nextInt(alphabet.length()));
            write("yaa sure. Here it is...\n" + s);
        } else {
            write(Utils.random("Hey, I have no clue about this command.", "This command has not been implemented yet.", "Hey, who did you think I was? I can't do everything."));
        }

    }

    private void write(final String output) {
        addMessage(new ChatMessage(output, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            askPermissions();
        }
    }

    private void askPermissions() {
        boolean granted = true;
        for (String perm : PERMS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (!granted)
            ActivityCompat.requestPermissions(this, PERMS, 29);
    }

    private void addMessage(ChatMessage message) {
        chatView.addMessage(message);
        if (message.getType().equals(ChatMessage.Type.RECEIVED))
            speak(message.getMessage());
    }

    private void onSpeechDone() {
        if (!isjoke)
            return;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                MediaPlayer mPlayer2;
                mPlayer2 = MediaPlayer.create(MainActivity.this, R.raw.lough);
                mPlayer2.start();
            }
        }, 1);
        isjoke = false;
    }

    public void isTTSSpeaking() {
        final Handler h = new Handler();
        Runnable r = new Runnable() {

            public void run() {
                if (!tts.isSpeaking()) {
                    onSpeechDone();
                } else
                    h.postDelayed(this, 1000);
            }
        };
        h.postDelayed(r, 1000);
    }

    public void speak(final String inp) {
        if (tts == null) {
            tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {
                    // TODO Auto-generated method stub
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(new Locale("en", "IN"));
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e("error", "This Language is not supported");
                        } else {
                            isTTSSpeaking();
                            tts.speak(inp, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else
                        Log.e("error", "Initilization Failed!");
                }
            });
        } else {
            isTTSSpeaking();
            tts.speak(inp, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
