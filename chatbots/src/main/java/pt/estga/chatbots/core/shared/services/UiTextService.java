package pt.estga.chatbots.core.shared.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.shared.models.text.TextNode;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UiTextService {

    private final MessageSource messageSource;
    private final TextTemplateParser parser;

    // Pattern to match {emoji.something}
    private static final Pattern EMOJI_PATTERN = Pattern.compile("\\{emoji\\.([a-zA-Z0-9_]+)\\}");

    public TextNode get(String key) {
        return get(key, (Object[]) null);
    }

    public TextNode get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        String raw = messageSource.getMessage(key, args, locale);
        raw = resolveEmojis(raw, locale);
        return parser.parse(raw);
    }

    public String raw(String key) {
        return raw(key, (Object[]) null);
    }

    public String raw(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        String raw = messageSource.getMessage(key, args, locale);
        return resolveEmojis(raw, locale);
    }

    private String resolveEmojis(String text, Locale locale) {
        if (text == null) return null;
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String emojiKey = "emoji." + matcher.group(1);
            String emoji = messageSource.getMessage(emojiKey, null, "", locale);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(emoji != null ? emoji : ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
