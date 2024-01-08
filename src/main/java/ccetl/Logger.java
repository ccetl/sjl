package ccetl;

import java.util.Arrays;
import java.util.Date;

@SuppressWarnings("unused")
public class Logger implements ILogger {
    private final String[] name;

    public Logger(String... name) {
        this.name = name;
    }

    public Logger(String name) {
        this.name = new String[]{name};
    }

    private void log(Throwable throwable, StackTraceElement[] message, Date date) {
        for (StackTraceElement string : message) {
            Sjl.log(build("[" + throwable.getClass().getName() + "]: " + string.toString(), Level.ERROR, date));
        }
    }

    private void queueForLogging(Runnable runnable) {
        Sjl.lock.lock();
        try {
            runnable.run();
        } finally {
            Sjl.lock.unlock();
        }
    }

    @Override
    public void info(String message) {
        Date date = new Date();
        queueForLogging(() -> Sjl.log(build(message, Level.INFO, date)));
    }

    @Override
    public void warn(String message) {
        Date date = new Date();
        queueForLogging(() -> Sjl.log(build(message, Level.WARN, date)));
    }

    @Override
    public void error(String message) {
        Date date = new Date();
        queueForLogging(() -> Sjl.log(build(message, Level.ERROR, date)));
    }

    @Override
    public void error(String message, Throwable throwable) {
        Date date = new Date();
        queueForLogging(() -> {
            Sjl.log(build(message, Level.ERROR, date));
            log(throwable, throwable.getStackTrace(), date);
        });
    }

    @Override
    public void error(Throwable throwable) {
        Date date = new Date();
        queueForLogging(() -> log(throwable, throwable.getStackTrace(), new Date()));
    }

    private String build(String message, Level level) {
        return build(message, level, new Date());
    }

    private String build(String message, Level level, Date date) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(Sjl.dateFormat.format(date));
        builder.append("] [");
        builder.append(level);
        builder.append("] [");
        builder.append(Thread.currentThread().getName());
        builder.append("] ");
        for (String string : name) {
            builder.append("[");
            builder.append(string);
            builder.append("]: ");
        }
        builder.append(message);
        builder.append("\n");
        return builder.toString();
    }

    public Logger child(String name) {
        int oldLength = this.name.length;
        String[] names = Arrays.copyOf(this.name, oldLength + 1);
        names[oldLength] = name;
        return new Logger(names);
    }
}
