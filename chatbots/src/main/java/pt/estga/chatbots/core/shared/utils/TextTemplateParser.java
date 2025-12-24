package pt.estga.chatbots.core.shared.utils;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.models.text.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextTemplateParser {

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
                if (end > i) {
                    nodes.add(new Bold(
                        parseNodes(input.substring(i + 3, end))
                    ));
                    i = end + 4;
                    continue;
                }
            }

            // Italic
            if (input.startsWith("{i}", i)) {
                int end = input.indexOf("{/i}", i);
                if (end > i) {
                    nodes.add(new Italic(
                        parseNodes(input.substring(i + 3, end))
                    ));
                    i = end + 4;
                    continue;
                }
            }

            // Code (no nesting)
            if (input.startsWith("{code}", i)) {
                int end = input.indexOf("{/code}", i);
                if (end > i) {
                    nodes.add(new Code(
                        input.substring(i + 6, end)
                    ));
                    i = end + 7;
                    continue;
                }
            }

            // Plain text
            int nextSpecial = findNextSpecial(input, i);
            nodes.add(new Plain(input.substring(i, nextSpecial)));
            i = nextSpecial;
        }

        return nodes;
    }

    private int findNextSpecial(String input, int start) {
        int next = input.length();
        for (String s : new String[]{"\n", "{b}", "{i}", "{code}"}) {
            int idx = input.indexOf(s, start);
            if (idx != -1 && idx < next) {
                next = idx;
            }
        }
        return next;
    }
}
