package pt.estga.chatbot.utils;

import org.springframework.stereotype.Component;
import pt.estga.chatbot.constants.Emojis;
import pt.estga.chatbot.models.text.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses text with supported formatting tags ({b}, {i}, {code}, {emoji.NAME}) into a tree of TextNodes.
 * Handles UTF-16 surrogate pairs (emojis) correctly by preserving plain text sequences.
 */
@Component
public class TextTemplateParser {

    private static final String[] SPECIALS = {
            "\n",
            "{b}", "{/b}",
            "{i}", "{/i}",
            "{code}", "{/code}",
            "{emoji.",
            "{0}", "{1}" // optional: parser can detect placeholders dynamically too
    };

    public TextNode parse(String input) {
        return new Container(parseNodes(input));
    }

    private List<TextNode> parseNodes(String input) {
        List<TextNode> nodes = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            // New line
            if (input.startsWith("\n", i)) {
                nodes.add(NewLine.INSTANCE);
                i++;
                continue;
            }

            // Bold
            if (input.startsWith("{b}", i)) {
                int end = input.indexOf("{/b}", i);
                if (end == -1) throw new IllegalStateException("Unclosed {b} tag in message: " + input);
                nodes.add(new Bold(parseNodes(input.substring(i + 3, end))));
                i = end + 4;
                continue;
            }

            // Italic
            if (input.startsWith("{i}", i)) {
                int end = input.indexOf("{/i}", i);
                if (end == -1) throw new IllegalStateException("Unclosed {i} tag in message: " + input);
                nodes.add(new Italic(parseNodes(input.substring(i + 3, end))));
                i = end + 4;
                continue;
            }

            // Code (no nesting)
            if (input.startsWith("{code}", i)) {
                int end = input.indexOf("{/code}", i);
                if (end == -1) throw new IllegalStateException("Unclosed {code} tag in message: " + input);
                nodes.add(new Code(input.substring(i + 6, end)));
                i = end + 7;
                continue;
            }

            // Emoji
            if (input.startsWith("{emoji.", i)) {
                int end = input.indexOf("}", i);
                if (end == -1) throw new IllegalStateException("Unclosed {emoji.} tag in message: " + input);

                String name = input.substring(i + 7, end); // after "{emoji." up to "}"
                Emojis emoji = Emojis.valueOf(name.toUpperCase());       // enum lookup
                nodes.add(new Emoji(emoji));
                i = end + 1;
                continue;
            }

            // Placeholder
            if (input.startsWith("{0}", i) || input.startsWith("{1}", i) /* ... up to N */) {
                int end = input.indexOf("}", i);
                int idx = Integer.parseInt(input.substring(i + 1, end));
                nodes.add(new Placeholder(idx));
                i = end + 1;
                continue;
            }

            // Plain text until next special
            int nextSpecial = findNextSpecial(input, i);
            if (nextSpecial == i) {
                // Handle unrecognized single char
                int codePoint = input.codePointAt(i);
                int charCount = Character.charCount(codePoint);
                nodes.add(new Plain(input.substring(i, i + charCount)));
                i += charCount;
            } else {
                nodes.add(new Plain(input.substring(i, nextSpecial)));
                i = nextSpecial;
            }
        }

        return nodes;
    }

    private int findNextSpecial(String input, int start) {
        int next = input.length();
        for (String s : SPECIALS) {
            int idx = input.indexOf(s, start);
            if (idx != -1 && idx < next) {
                next = idx;
            }
        }
        return next;
    }
}
