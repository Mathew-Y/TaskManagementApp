package com.example.taskmanagementapp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordToNumber {

    private static final Map<String, Integer> NUMBER_MAP = new HashMap<>();

    static {
        // Units
        NUMBER_MAP.put("zero", 0);
        NUMBER_MAP.put("one", 1);
        NUMBER_MAP.put("two", 2);
        NUMBER_MAP.put("three", 3);
        NUMBER_MAP.put("four", 4);
        NUMBER_MAP.put("five", 5);
        NUMBER_MAP.put("six", 6);
        NUMBER_MAP.put("seven", 7);
        NUMBER_MAP.put("eight", 8);
        NUMBER_MAP.put("nine", 9);

        // Teens
        NUMBER_MAP.put("ten", 10);
        NUMBER_MAP.put("eleven", 11);
        NUMBER_MAP.put("twelve", 12);
        NUMBER_MAP.put("thirteen", 13);
        NUMBER_MAP.put("fourteen", 14);
        NUMBER_MAP.put("fifteen", 15);
        NUMBER_MAP.put("sixteen", 16);
        NUMBER_MAP.put("seventeen", 17);
        NUMBER_MAP.put("eighteen", 18);
        NUMBER_MAP.put("nineteen", 19);

        // Tens
        NUMBER_MAP.put("twenty", 20);
        NUMBER_MAP.put("thirty", 30);
        NUMBER_MAP.put("forty", 40);
        NUMBER_MAP.put("fifty", 50);
        NUMBER_MAP.put("sixty", 60);
        NUMBER_MAP.put("seventy", 70);
        NUMBER_MAP.put("eighty", 80);
        NUMBER_MAP.put("ninety", 90);

        // Multipliers
        NUMBER_MAP.put("hundred", 100);
        NUMBER_MAP.put("thousand", 1000);
    }

    public static int parse(String input) {
        input = input.toLowerCase().replaceAll("[^a-z\\s-]", "").replaceAll("-", " ");
        String[] words = input.trim().split("\\s+");

        int total = 0;
        int current = 0;

        for (String word : words) {
            if (!NUMBER_MAP.containsKey(word)) continue;

            int value = NUMBER_MAP.get(word);

            if (value == 100) {
                current *= 100;
            } else if (value == 1000) {
                current *= 1000;
                total += current;
                current = 0;
            } else {
                current += value;
            }
        }
        return total + current;
    }

    public static String convertWordsInText(String text) {
        Pattern pattern = Pattern.compile("\\b(?:zero|one|two|three|four|five|six|seven|eight|nine|ten|"
                + "eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|"
                + "thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand)+\\b", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String wordNumber = matcher.group();
            int number = parse(wordNumber.toLowerCase());  // your existing parser
            matcher.appendReplacement(buffer, String.valueOf(number));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

}
