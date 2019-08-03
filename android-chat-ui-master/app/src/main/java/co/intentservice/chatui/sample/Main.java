package co.intentservice.chatui.sample;

import com.micronic.micron1.Micron;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bot bot = new Bot("micron", "/home/aditya/Desktop");
        String[] props = co.intentservice.chatui.sample.Bot.PROPERTIES.split(";");
        for (String prop : props) {
            String[] propss = prop.split("\\s+");
            String name = propss[0];
            String value = propss[1];
            for (int i = 2; i < propss.length; i++) {
                value += " " + propss[i];
            }
            bot.properties.put(name, value);
        }
        bot.writeLearnfIFCategories();
        bot.writeAIMLIFFiles();
        Chat chat = new Chat(bot);
        //Micron micron = new Micron();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine();
//            System.out.println(micron.process(input));
            System.out.println(chat.multisentenceRespond(input));
        }
    }
}
