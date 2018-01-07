package il.ac.tau.cloudweb17a.hasorkimmanagers;

public class Scanner {

    private final String userId;
    private String duration;
    private boolean isAssignedScanner;

    Scanner(String userId, String duration, boolean isAssignedScanner) {
        this.userId = userId;
        this.duration = duration;
        this.isAssignedScanner = isAssignedScanner;
    }

    @Override
    public String toString() {
        return "Scanner{" +
                "userId='" + userId + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public String getUserId() { return userId; }
    public String getDuration() { return duration;}
    public boolean getIsAssignedScanner() { return isAssignedScanner;}
}
