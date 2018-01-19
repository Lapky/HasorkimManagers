package il.ac.tau.cloudweb17a.hasorkimmanagers;

public class Scanner {

    private final String userId;
    private String duration;

    private boolean assignedScanner;
    private String name;

    Scanner(String userId, String name, String duration, boolean assignedScanner) {
        this.userId = userId;
        this.duration = duration;
        this.assignedScanner = assignedScanner;
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

    public boolean isAssignedScanner() {
        return assignedScanner;
    }

    public void setAssignedScanner(boolean assignedScanner) {
        this.assignedScanner = assignedScanner;
    }

    public String getName() {
        return name;
    }
}
