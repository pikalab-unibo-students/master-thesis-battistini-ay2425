package it.unibo.jakta.agents.bdi.engine.logging.loggers.resolvers;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.*;
import org.apache.logging.log4j.util.PerformanceSensitive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(name = "JaktaLoggerNamePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"c", "logger"})
@PerformanceSensitive("allocation")
public class JaktaLoggerNamePatternConverter extends NamePatternConverter {

    private static final JaktaLoggerNamePatternConverter INSTANCE = new JaktaLoggerNamePatternConverter(null);

    private final Pattern pattern = Pattern.compile("^([^-]+)-[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$");

    private JaktaLoggerNamePatternConverter(final String[] options) {
        super("Logger", "logger", options);
    }

    public static JaktaLoggerNamePatternConverter newInstance(final String[] options) {
        if (options == null || options.length == 0) {
            return INSTANCE;
        }

        return new JaktaLoggerNamePatternConverter(options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String eventName = event.getLoggerName();
        Matcher matcher = pattern.matcher(eventName);
        String name = matcher.matches() ? matcher.group(1) : eventName;
        abbreviate(name, toAppendTo);
    }
}
