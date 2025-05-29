/*
 * 版权所有 (c) 2009, 2013, Oracle 和/或其附属公司。保留所有权利。
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

import sun.util.logging.LoggingProxy;

/**
 * 当存在 java.util.logging 类时的 LoggingProxy 实现。
 */
class LoggingProxyImpl implements LoggingProxy {
    static final LoggingProxy INSTANCE = new LoggingProxyImpl();

    private LoggingProxyImpl() { }

    @Override
    public Object getLogger(String name) {
        // 始终使用资源包名称创建平台日志记录器
        return Logger.getPlatformLogger(name);
    }

    @Override
    public Object getLevel(Object logger) {
        return ((Logger) logger).getLevel();
    }

    @Override
    public void setLevel(Object logger, Object newLevel) {
        ((Logger) logger).setLevel((Level) newLevel);
    }

    @Override
    public boolean isLoggable(Object logger, Object level) {
        return ((Logger) logger).isLoggable((Level) level);
    }

    @Override
    public void log(Object logger, Object level, String msg) {
        ((Logger) logger).log((Level) level, msg);
    }

    @Override
    public void log(Object logger, Object level, String msg, Throwable t) {
        ((Logger) logger).log((Level) level, msg, t);
    }

    @Override
    public void log(Object logger, Object level, String msg, Object... params) {
        ((Logger) logger).log((Level) level, msg, params);
    }

    @Override
    public java.util.List<String> getLoggerNames() {
        return LogManager.getLoggingMXBean().getLoggerNames();
    }

    @Override
    public String getLoggerLevel(String loggerName) {
        return LogManager.getLoggingMXBean().getLoggerLevel(loggerName);
    }

    @Override
    public void setLoggerLevel(String loggerName, String levelName) {
        LogManager.getLoggingMXBean().setLoggerLevel(loggerName, levelName);
    }

    @Override
    public String getParentLoggerName(String loggerName) {
        return LogManager.getLoggingMXBean().getParentLoggerName(loggerName);
    }

    @Override
    public Object parseLevel(String levelName) {
        Level level = Level.findLevel(levelName);
        if (level == null) {
            throw new IllegalArgumentException("未知级别 \"" + levelName + "\"");
        }
        return level;
    }

    @Override
    public String getLevelName(Object level) {
        return ((Level) level).getLevelName();
    }

    @Override
    public int getLevelValue(Object level) {
        return ((Level) level).intValue();
    }

    @Override
    public String getProperty(String key) {
        return LogManager.getLogManager().getProperty(key);
    }
}
