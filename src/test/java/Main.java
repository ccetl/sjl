import ccetl.Logger;
import ccetl.Sjl;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Sjl.setFileSuffix("txt");
        Sjl.init(Path.of("path/to/your/log/folder"));
        Logger logger = new Logger("A logger");
        logger.warn("This is a warning");
        logger.error("This is an error");
        logger.info("This is an info");
        logger.error(new IOException());
        logger.error("An other Exception", new AccessDeniedException("a File"));

        Logger logger1 = logger.child("An other logger");
        logger1.info("Hello from the other logger");


    }
}