package ar.edu.utn.dds.k3003.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Decide que logs se mandan a Better Stack (se engancha al appender en logback-spring.xml).
 *
 * <p>Pasan: todos los logs de nuestro codigo (ar.edu.utn.dds.k3003), y los WARN/ERROR de
 * cualquier lado (Spring, Hibernate, Tomcat), porque esos si indican un problema.
 *
 * <p>No pasan: los INFO de frameworks (el arranque de Spring, Tomcat, JPA...), que son ruido
 * en Better Stack. Igual se siguen viendo en la consola / pestaña Logs de Render.
 */
public class BetterStackFilter extends Filter<ILoggingEvent> {

    private static final String PAQUETE_APP = "ar.edu.utn.dds.k3003";

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLoggerName().startsWith(PAQUETE_APP)) {
            return FilterReply.NEUTRAL;
        }
        if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            return FilterReply.NEUTRAL;
        }
        return FilterReply.DENY;
    }
}
