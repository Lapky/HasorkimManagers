package il.ac.tau.cloudweb17a.hasorkimmanagers;

public class Scanner {

    private final String name;
    private double distanceKm;

    Scanner(String name, double distanceKm) {
        this.name = name;
        this.distanceKm = distanceKm;
    }

    public String getName() { return this.name; }
    public String getDistanceStr() { return Double.toString(this.distanceKm);}
}
