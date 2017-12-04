package il.ac.tau.cloudweb17a.hasorkimmanagers;

/**
 * Created by workhourse on 12/2/17.
 */

public class Report {

    private int id;
    private String reportyName;
    private String status;
    private final String startTime;
    private final String address;
    private String[] imageUrls;
    private String freeText;
    private long phoneNumber;
    private String assignedScanner;
    private int availableScanners;
    private double distanceKm;

    Report(int id, String startTime, String address) {
        this.id = id;
        this.startTime = startTime;
        this.address = address;
    }

    Report(int id, String reportyName, String startTime, String address, String status,
           String freeText, long phoneNumber, String assignedScanner,
           int availableScanners, double distanceKm) {
        this.id = id;
        this.reportyName = reportyName;
        this.startTime = startTime;
        this.address = address;
        this.status = status;
        this.freeText = freeText;
        this.phoneNumber = phoneNumber;
        this.assignedScanner = assignedScanner;
        this.availableScanners = availableScanners;
        this.distanceKm = distanceKm;
    }

    public String getStatus() { return this.status; }
    public String getStartTime() { return this.startTime; }
    public String getAddress() { return this.address; }
    public String getAvailableScannersStr() { return Integer.toString(this.availableScanners); }
    public String getDistanceStr() { return Double.toString(this.distanceKm);}

}