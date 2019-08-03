package com.micronic.micron1;

import java.util.List;

/**
 * Created by micronic on 14/7/15.
 */
public class Micron {
    private static final String intro = "You live in India. Your name is Dexter. Abdul made you because he wanted to change the world. " +
            "Abdul loves you. You love Abdul. Abdul loves himself. You love yourself. You believe in God. " +
            "You know everything. You can do everything. Everything is possible in this world. " +
            "God made this world. We can change this world. You love me.";

    private final Data data;

    public Micron() {
        data = new Data();
        process(intro, true);
    }

    private String process(MRoot root, boolean intro) {
        System.out.println("Type: " + root.getType());
        if (root.getType() == 1) {
            MWord mWord = data.has(root);
            if (mWord != null) {
                root.setType(0);
                return Utils.random("Yes", "Yep", "Yee", "Yoo") + ". " + root.state();
            } else {
                if (root.getMod("neg") == null)
                    root.mods.add(new MMod("neg", "not", "RB"));
                root.setType(0);
//                return Utils.random("Not that I know of.", "Not that I am aware of.");
                return "null1";
            }
        } else if (root.getType() == 2) {
            MWord word = data.ask(root);
            if (word != null)
                return word.state();
            else
                return "null1";
        } else if (root.getType() == 3) {
            return "null1";
        } else {
            MWord mWord = data.has(root);
            if (mWord != null) {
                return Utils.random("I think I know that already!", "I am already aware of it.", "I know!");
            } else {
                data.add(root, intro);
                return "null2";
            }
        }
    }

    private String process(String text, boolean intro) {
        String out = "Oops!";
        String[] lines = text.split("(?<=[?.!;])\\s+");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            //workaround #4
            if (!line.endsWith(".") && !line.endsWith("?") && !line.endsWith("!")) {
                String[] words = {"do", "did", "have", "has", "had", "will", "shall", "would", "should", "might", "may", "am", "are", "were", "is", "was", "what", "who", "where", "whom", "when", "which", "whose", "why", "how"};
                for (String word : words) {
                    if (line.toLowerCase().startsWith(word)) {
                        line = line + "?";
                        break;
                    }
                }
                if (!line.endsWith("?"))
                    line = line + ".";
            }
            //workaround #4 end

            List<Parser.Word> words = Parser.getWords(line);
            int type = Parser.typeOf(words, line);
            if (i == lines.length - 1 || type == 0) {
                out = process(Parser.parse(words, type), intro);
            }
        }
        return out;
    }

    public String process(String text) {
        String out = process(text, false);
        if (out.length() > 1 && !out.startsWith("null")) {
            out = Character.toUpperCase(out.charAt(0)) + out.substring(1);
            if (!out.endsWith(".") && !out.endsWith("?") && !out.endsWith("!"))
                out = out + ".";
        }
        return out;
    }
}
