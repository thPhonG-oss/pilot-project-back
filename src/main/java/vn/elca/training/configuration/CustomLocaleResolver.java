package vn.elca.training.configuration;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class CustomLocaleResolver implements LocaleResolver {
    private static final List<String> SUPPORTED;

    static {
        SUPPORTED = new ArrayList<>();
        SUPPORTED.add("en");
        SUPPORTED.add("vi");
    }
    private static final String DEFAULT = "en";

    @Override
    public Locale resolveLocale(HttpServletRequest request) {

        String localeHeader = request.getHeader("locale");
        if (localeHeader != null && SUPPORTED.contains(localeHeader)) {
            return Locale.forLanguageTag(localeHeader);
        }

        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null) {
            String lang = acceptLanguage.split(",")[0];
            if (SUPPORTED.contains(lang)) {
                return Locale.forLanguageTag(lang);
            }
        }

        return Locale.forLanguageTag(DEFAULT);
    }

    @Override
    public void setLocale(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Locale locale) {

    }
}
