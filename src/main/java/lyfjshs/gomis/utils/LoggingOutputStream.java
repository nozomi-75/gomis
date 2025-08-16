package lyfjshs.gomis.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LoggingOutputStream extends OutputStream {
    private final Logger logger;
    private final Level level;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public LoggingOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            flush();
        } else {
            buffer.write(b);
        }
    }

    @Override
    public void flush() {
        String message = buffer.toString().trim();
        if (!message.isEmpty()) {
            logger.log(level, message);
        }
        buffer.reset();
    }
} 