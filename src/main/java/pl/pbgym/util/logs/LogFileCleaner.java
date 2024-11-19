package pl.pbgym.util.logs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class LogFileCleaner implements CommandLineRunner {
    private static final String LOG_FILE = "logs/application.log";
    private static final String ROLLING_LOG_FILE = "logs/application.log.1";

    @Override
    public void run(String... args) throws Exception {
        try {
            Files.deleteIfExists(Paths.get(ROLLING_LOG_FILE));
            Files.write(Paths.get(LOG_FILE), new byte[0]);
        } catch (IOException ignored) {}
    }
}

