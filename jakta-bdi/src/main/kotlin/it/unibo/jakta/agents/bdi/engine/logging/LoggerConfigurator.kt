package it.unibo.jakta.agents.bdi.engine.logging

import it.unibo.jakta.agents.bdi.engine.logging.loggers.JaktaLogger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration

object LoggerConfigurator {
    private val configBuilder: ConfigurationBuilder<BuiltConfiguration> =
        ConfigurationBuilderFactory.newConfigurationBuilder()

    private val configuredAppenders = mutableSetOf<String>()
    private val configuredLoggers = mutableSetOf<String>()

    val jsonLayout: LayoutComponentBuilder =
        configBuilder.newLayout("JsonTemplateLayout").apply {
            addAttribute("eventTemplateUri", "classpath:JaktaLayout.json")

            val eventField =
                builder
                    .newComponent("EventTemplateAdditionalField")
                    .addAttribute("key", "message")
                    .addAttribute("format", "JSON")
                    .addAttribute("value", "{\"\$resolver\": \"agentContext\"}")

            addComponent(eventField)
        }

    val consolePatternLayout: LayoutComponentBuilder =
        configBuilder.newLayout("PatternLayout").apply {
            val levelHighlight =
                "%highlight{%level}{FATAL=red blink, ERROR=red, WARN=yellow, INFO=green, DEBUG=blue, TRACE=cyan}"
            val loggerStyle = "%style{%logger{36}}{yellow}"
            val message = "- %description%n"
            val fullPattern = "$levelHighlight $loggerStyle $message"
            addAttribute("pattern", fullPattern)
        }

    val logFilePatternLayout: LayoutComponentBuilder =
        configBuilder.newLayout("PatternLayout").apply {
            addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss.nnnnnn}%d{XXX} [%t] %-5level %logger - %description%n")
        }

    const val CONSOLE_APPENDER_NAME = "Console"

    const val TCP_APPENDER_NAME = "JsonTcp"

    init {
        configBuilder.setStatusLevel(Level.ERROR)
        configBuilder.setConfigurationName("JaktaLoggingConfig")
        addConsoleAppender(consolePatternLayout)
    }

    fun applyConfiguration() {
        val configuration = configBuilder.build(false)
        Configurator.reconfigure(configuration)
    }

    fun addConsoleAppender(layout: LayoutComponentBuilder) {
        if (CONSOLE_APPENDER_NAME !in configuredAppenders) {
            configBuilder
                .newAppender(CONSOLE_APPENDER_NAME, "Console")
                .apply {
                    addAttribute("target", "SYSTEM_OUT")
                    add(layout)
                }.let {
                    configBuilder.add(it)
                }.also {
                    configuredAppenders.add(CONSOLE_APPENDER_NAME)
                    applyConfiguration()
                }
        }
    }

    fun addFileAppender(
        name: String,
        fileName: String,
        layout: LayoutComponentBuilder,
    ) {
        if (name !in configuredAppenders) {
            configBuilder
                .newAppender(name, "File")
                .apply {
                    addAttribute("fileName", fileName)
                    addAttribute("append", true)
                    add(layout)
                }.let {
                    configBuilder.add(it)
                }.also {
                    configuredAppenders.add(name)
                    applyConfiguration()
                }
        }
    }

    fun addTcpAppender(
        logServerURL: String,
        layout: LayoutComponentBuilder,
    ) {
        val (host, port) = JaktaLogger.extractHostnameAndPort(logServerURL)
        if (port != null && TCP_APPENDER_NAME !in configuredAppenders) {
            configBuilder
                .newAppender(TCP_APPENDER_NAME, "Socket")
                .apply {
                    addAttribute("host", host)
                    addAttribute("port", port)
                    addAttribute("protocol", "TCP")
                    add(layout)
                }.let {
                    configBuilder.add(it)
                }.also {
                    configuredAppenders.add(TCP_APPENDER_NAME)
                    applyConfiguration()
                }
        }
    }

    fun addLogger(
        loggerName: String,
        level: Level,
        appenderRefs: List<String>,
        additivity: Boolean = false,
    ) {
        if (loggerName !in configuredLoggers) {
            val logger =
                configBuilder.newLogger(loggerName, level).apply {
                    addAttribute("additivity", additivity.toString())
                    appenderRefs.forEach { appenderName ->
                        if (appenderName in configuredAppenders) {
                            add(configBuilder.newAppenderRef(appenderName))
                        }
                    }
                }
            configBuilder.add(logger)
            configuredLoggers.add(loggerName)
            applyConfiguration()
        }
    }
}
