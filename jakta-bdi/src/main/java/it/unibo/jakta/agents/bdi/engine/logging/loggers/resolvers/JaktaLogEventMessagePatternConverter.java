package it.unibo.jakta.agents.bdi.engine.logging.loggers.resolvers;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.MessagePatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.util.PerformanceSensitive;

import static it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger.resolveObjectMessage;

@Plugin(name = "JaktaLogEventMessagePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"d", "descr", "description"})
@PerformanceSensitive("allocation")
public class JaktaLogEventMessagePatternConverter extends LogEventPatternConverter {
    private final MessagePatternConverter internalConverter;

    private static final JaktaLogEventMessagePatternConverter INSTANCE = new JaktaLogEventMessagePatternConverter();

    private JaktaLogEventMessagePatternConverter() {
        super("Message", "message");
        this.internalConverter = MessagePatternConverter.newInstance(null, null);
    }

    public static JaktaLogEventMessagePatternConverter newInstance(final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        Message message = event.getMessage();

        if (message instanceof ObjectMessage) {
            String msg = resolveObjectMessage((ObjectMessage) message);
            toAppendTo.append(msg);
        } else {
            internalConverter.format(event, toAppendTo);
        }
    }
}
