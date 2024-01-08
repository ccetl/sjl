package ccetl;

@SuppressWarnings("unused")
interface ILogger {
    void info(String message);
    void warn(String message);
    void error(String message);
    void error(String message, Throwable throwable);
    void error(Throwable throwable);
}
