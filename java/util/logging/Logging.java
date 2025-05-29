/*
 * 版权所有 (c) 2003, 2013, Oracle 和/或其关联公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.util.logging;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

/**
 * Logging 是 LoggingMXBean 的实现类。
 *
 * <tt>LoggingMXBean</tt> 接口提供了一种标准的方法，用于管理访问运行时可用的各个
 * {@code Logger} 对象。
 *
 * @author Ron Mann
 * @author Mandy Chung
 * @since 1.5
 *
 * @see javax.management
 * @see Logger
 * @see LogManager
 */
class Logging implements LoggingMXBean {

    private static LogManager logManager = LogManager.getLogManager();

    /** Logging 的构造函数，它是 LoggingMXBean 的实现类。 */
    Logging() {
    }

    public List<String> getLoggerNames() {
        Enumeration<String> loggers = logManager.getLoggerNames();
        ArrayList<String> array = new ArrayList<>();

        for (; loggers.hasMoreElements();) {
            array.add(loggers.nextElement());
        }
        return array;
    }

    private static String EMPTY_STRING = "";
    public String getLoggerLevel(String loggerName) {
        Logger l = logManager.getLogger(loggerName);
        if (l == null) {
            return null;
        }

        Level level = l.getLevel();
        if (level == null) {
            return EMPTY_STRING;
        } else {
            return level.getLevelName();
        }
    }

    public void setLoggerLevel(String loggerName, String levelName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }

        Logger logger = logManager.getLogger(loggerName);
        if (logger == null) {
            throw new IllegalArgumentException("Logger " + loggerName +
                "does not exist");
        }

        Level level = null;
        if (levelName != null) {
            // 如果 logLevel 无效，parse 将抛出 IAE
            level = Level.findLevel(levelName);
            if (level == null) {
                throw new IllegalArgumentException("Unknown level \"" + levelName + "\"");
            }
        }

        logger.setLevel(level);
    }

    public String getParentLoggerName( String loggerName ) {
        Logger l = logManager.getLogger( loggerName );
        if (l == null) {
            return null;
        }

        Logger p = l.getParent();
        if (p == null) {
            // 根记录器
            return EMPTY_STRING;
        } else {
            return p.getName();
        }
    }
}
