package il.ac.tau.cloudweb17a.hasorkimmanagers;

public class Scanner {

    private final String userId;
    private String duration;
    private boolean isAssignedScanner;
    private String name;

    Scanner(String userId, String name, String duration, boolean isAssignedScanner) {
        this.userId = userId;
        this.duration = duration;
        this.isAssignedScanner = isAssignedScanner;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Scanner{" +
                "userId='" + userId + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public String getDuration() {
        return duration;
    }

    public boolean getIsAssignedScanner() {
        return isAssignedScanner;
    }

    public String getName() {
        return name;
    }
}
