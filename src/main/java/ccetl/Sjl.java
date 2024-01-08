package ccetl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Sjl {
    protected static final ReentrantLock lock = new ReentrantLock();
    protected static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static SimpleDateFormat dateFormatOldFile = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
    private static String fileSuffix = "txt";
    private static Path loggingDir;
    private static File loggingFile;
    private static Writer writer;
    private static Action action = Action.FILE;
    private static Consumer<String> logListener;
    private static boolean initialized = false;

    /**
     * Initializes Sjl.
     *
     * @param dir the path where the log file should be created in
     * @throws IllegalStateException when sjl is already initialized
     */
    public static void init(Path dir) {
        assertInitializationStatus(false);

        Sjl.loggingDir = dir;
        dir.toFile().mkdirs();
        loggingFile = new File(loggingDir + "/latest." + fileSuffix);
        if (loggingFile.exists()) {
            renameOld();
        }
        try {
            loggingFile.createNewFile();
            writer = new BufferedWriter(new FileWriter(loggingFile));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                initialized = false;
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }

    /**
     * Helper function for {@link Sjl#init}. <br>
     * Renames the old latest log file to its creation time.
     */
    private static void renameOld() {
        try {
            BasicFileAttributes attr = Files.readAttributes(Path.of(loggingDir.toString(), "latest." + fileSuffix), BasicFileAttributes.class);
            FileTime creationTime = attr.creationTime();
            if (creationTime == null) {
                loggingFile.delete();
                return;
            }
            String newName = dateFormatOldFile.format(new Date(creationTime.to(TimeUnit.MILLISECONDS)));
            File newFile = new File(loggingDir + "/" + newName + "." + fileSuffix);
            loggingFile.renameTo(newFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void log(String message) {
        if (logListener != null) {
            logListener.accept(message);
        }
        action.run(message);
    }

    /**
     * Writes to the log file.
     *
     * @param text the message to be printed
     * @throws IllegalStateException when Sjl isn't initialized
     */
    protected static void write(String text) {
        assertInitializationStatus(true);
        try {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Optional setting. Sets the file extension of the log file. <br>
     * The default is "txt".
     *
     * @param fileSuffix the new file extension
     */
    public static void setFileSuffix(String fileSuffix) {
        assertInitializationStatus(false);
        Sjl.fileSuffix = fileSuffix;
    }

    /**
     * Optional setting. Sets the formatting of the log-message timestamps. <br>
     * The default is "dd-MM-yyyy_HH-mm-ss".
     *
     * @param dateFormat the new format
     */
    public static void setDateFormat(SimpleDateFormat dateFormat) {
        assertInitializationStatus(false);
        Sjl.dateFormat = dateFormat;
    }

    /**
     * Optional setting. Sets the formatting for the file name from the old log file. <br>
     * The default is "dd-MM-yyyy_HH-mm-ss".
     *
     * @param dateFormat the new format
     */
    public static void setDateFormatOldFile(SimpleDateFormat dateFormat) {
        assertInitializationStatus(false);
        Sjl.dateFormatOldFile = dateFormat;
    }

    /**
     * Optional. The lambda function gets notified every time a message gets logged.
     *
     * @param logListener the listener
     */
    public static void setLogListener(Consumer<String> logListener) {
        Sjl.logListener = logListener;
    }

    /**
     * Optional. The action to log.
     */
    public static void setAction(Action action) {
        Sjl.action = action;
    }

    /**
     * Checks if sjl is initialized - or not.
     *
     * @param expected what the code assumes the initialization status is
     * @throws IllegalStateException when then expected, value mismatches the actual status
     */
    private static void assertInitializationStatus(boolean expected) {
        if (initialized == expected) {
            return;
        }

        throw new RuntimeException(new IllegalStateException());
    }

    /**
     * Checks whether {@link Sjl#init} was already called.
     *
     * @return the Initialization Status
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
