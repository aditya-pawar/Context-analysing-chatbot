package com.micronic.micron1;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Micron micron = new Micron();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String input = sc.nextLine();
            System.out.println(micron.process(input));
        }
    }
}
