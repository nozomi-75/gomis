package lyfjshs;

public class ProgressUpdate {
    final String message;
    final int progress;
    
    ProgressUpdate(String message, int progress) {
        this.message = message;
        this.progress = progress;
    }
}