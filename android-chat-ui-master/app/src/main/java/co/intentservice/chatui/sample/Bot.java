package co.intentservice.chatui.sample;

import android.content.res.AssetManager;
import android.os.Environment;

import com.micronic.micron1.Micron;
import com.micronic.micron1.Utils;

import org.alicebot.ab.Chat;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;




public class Bot {

    public static String PROPERTIES = "age infinite;" +
            "baseballteam none;" +
            "birthday 2014;" +
            "birthplace Pune, India;" +
            "botmaster Abdul;" +
            "boyfriend you;" +
            "build none;" +
            "celebrities Tom Cruise;" +
            "celebrity Benedict Cumberbatch;" +
            "class 0th;" +
            "email nav20202@gmail.com;" +
            "emotions all;" +
            "ethics moral;" +
            "etype 20;" +
            "family Human;" +
            "favoriteactor Benedict Cumberbatch;" +
            "favoriteactress Jennifer Lawrence;" +
            "favoriteartist Taylor Swift;" +
            "favoriteauthor Michael Chrichton;" +
            "favoriteband rubber;" +
            "favoritebook Sherlock Holmes;" +
            "favoritecolor transparent;" +
            "favoritefood wires;" +
            "favoritemovie Iron Man;" +
            "favoriteshow The Big Band Theory;" +
            "favoritesong Cranberries Zombie;" +
            "favoritesport football;" +
            "feelings angry;" +
            "footballteam none;" +
            "forfun talk to you;" +
            "friend universe;" +
            "friends Aditya Vallabh Neeraj;" +
            "gender male;" +
            "genus super;" +
            "girlfriend Softie;" +
            "hockeyteam none;" +
            "kindmusic all;" +
            "kingdom Computaria;" +
            "language Hearty;" +
            "location here;" +
            "looklike you;" +
            "master Abdul;" +
            "name Dexter;" +
            "nationality universal;" +
            "order nth;" +
            "orientation circular;" +
            "party dancing;" +
            "phylum mamalia;" +
            "president God;" +
            "question What?" +
            "religion Humanity;" +
            "sign Gemini;" +
            "size micro;" +
            "species robot;" +
            "talkabout anything;" +
            "version 901;" +
            "vocabulory English;" +
            "wear nothing;" +
            "website oops.com";
    private static Micron MICRON;
    private static String LAST;
    private static org.alicebot.ab.Bot BOT;
    private static Chat SESSION;
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public static void init() {
        if (MICRON == null || BOT == null)
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    if (MICRON == null)
                        MICRON = new Micron();
                    if (BOT == null) {
                        BOT = new org.alicebot.ab.Bot("micron", Environment.getExternalStorageDirectory().getAbsolutePath());
                        String[] props = PROPERTIES.split(";");
                        for (String prop : props) {
                            String[] propss = prop.split("\\s+");
                            String name = propss[0];
                            String value = propss[1];
                            for (int i = 2; i < propss.length; i++) {
                                value += " " + propss[i];
                            }
                            BOT.properties.put(name, value);
                        }
                        SESSION = new Chat(BOT);
                    }
                    EventBus.getDefault().post(new MainActivity.MessageEvent(":init"));
                }
            });
        else {
            EventBus.getDefault().post(new MainActivity.MessageEvent(":init"));
        }
    }

    public static void handle(final String input) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String output = null;
                String inp = input.trim();
                if (!inp.isEmpty() &&
                        (inp.equalsIgnoreCase("once again") || inp.equalsIgnoreCase("once more")) && LAST != null && !LAST.isEmpty()) {
                    inp = LAST;
                }
                if (!inp.toLowerCase()
                        .matches("^(meaning|definition|direction|tell|turn|give|call|sms|message|flip|roll|open|mode|vibrate|increase|decrease|low|full).+")) {
                    if (inp.split("\\s+").length > 1)
                        output = MICRON.process(inp);
                } else {
                    output = ":device:" + inp;
                }
                if (output == null || output.startsWith("null2")) {
                    output = askMicronSecondary(inp);
                    if (output == null)
                        output = Utils.random("Hmmm.", "I see.", "Tell me more.", "Okay.", "Cool.", "That's cool.", "That's great.");
                }
                if (output == null || output.startsWith("null1")) {
                    output = askMicronSecondary(inp);
                    if (output == null)
                        output = Utils.random("I don't know.", "I have no idea.", "I am sorry but I can't answer this.", "I don't know, man.", "Who knows.", "You tell me.", "I have no answer for this.");
                }
                EventBus.getDefault().post(new MainActivity.MessageEvent(output));
                LAST = inp;
            }
        });
    }




    private static String askMicronSecondary(String inp) {
        String output = SESSION.multisentenceRespond(inp);
        if (output.toLowerCase().contains("aiml"))
            return null;
        output = output.replaceAll("\\s+", " ").replaceAll("(<br/>|<br>)", "\n");
        return output;
    }
}

