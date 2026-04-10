package sickbay.pokenamon.util;

import java.util.Arrays;
import java.util.List;

public class Localizer {
    public static String toTitleCase(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder sb = new StringBuilder();
        char[] charArr = text.toCharArray();

        for (int i = 0; i < charArr.length; i++) {
            char currentChar = charArr[i];

            if (i == 0 || charArr[i - 1] == ' ' || charArr[i - 1] == '-') {
                sb.append(Character.toUpperCase(currentChar));
            } else {
                sb.append(Character.toLowerCase(currentChar));
            }
        }

        return sb.toString();
    }

    public static String formatPokemonName(String pokemonName) {
        if (pokemonName == null || pokemonName.isEmpty()) return "";

        String titledName = toTitleCase(pokemonName.replace("-", " "));
        String[] parts = titledName.split(" ");

        if (parts.length == 1) {
            return parts[0];
        }

        StringBuilder sb = new StringBuilder();

        if (parts.length >= 2) {
            List<String> forms = Arrays.asList("Mega", "Gigantamax", "Primal", "Alolan", "Galarian");

            if (forms.contains(parts[1])) {
                sb.append(parts[1]).append(" ").append(parts[0]);
            } else {
                String second = parts[1].toLowerCase();
                if (second.equals("f") || second.equals("m")) {
                    parts[1] = "";
                }
                sb.append(parts[0]).append(" ").append(parts[1]);
            }

            if (parts.length >= 3) {
                String third = parts[2].toLowerCase();

                if (third.equals("f") || third.equals("m")) {
                    sb.append("");
                }
                else if (third.equals("x") || third.equals("y") || third.equals("z")) {
                    sb.append(" ").append(parts[2].toUpperCase());
                }

                else {
                    sb.append(" ").append(parts[2]);
                }
            }
        }

        return sb.toString().trim();
    }

    public static String formatPokemonMove(String pokemonMove) {
        if (pokemonMove == null || pokemonMove.isEmpty()) return "";

        return toTitleCase(pokemonMove.replace("-", " "));
    }

    public static String formatEnumString(String enumString) {
        return enumString.toUpperCase().replaceAll("-", "_");
    }
}
