package lyfjshs;

public class ImportProgressUpdate {
    private final String message;
    private final int progress;

    public ImportProgressUpdate(String message, int progress) {
        this.message = message;
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public int getProgress() {
        return progress;
    }
}
