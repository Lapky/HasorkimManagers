package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class Report implements  java.io.Serializable{


    private String id;
    private String reporterName;
    private int incrementalReportId;
    private String status;
    private long startTime;
    private String address;
    private String freeText;
    private String phoneNumber;
    private String extraPhoneNumber;

    private String assignedScanner;

    private String scannerOnTheWay;

    private int availableScanners;

    private Set<String> potentialScanners ;


    private String closingText;
    private String cancellationText;
    private String cancellationUserType;
    private String userId;

    private boolean hasSimilarReports;
    private boolean isDogWithReporter;
    private String imageUrl;


    private boolean isScannerEnlistedStatus;
    //private boolean isManagerEnlistedStatus;


    private double Lat;
    private double Long;

    private String distance;
    private String duration;

    private int nextIncrementalId;
    private static final String TAG = "Report";
    private int distancevalue;

    private void changePotentialScanners(String userId, int change){
        if (change == 0) potentialScanners.remove(userId);
        else if (change == 1) potentialScanners.add(userId);

        availableScanners = potentialScanners.size();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        Map<String,Object> potentialScannersMap = new HashMap<String,Object>();

        for (String scanner: potentialScanners){
            potentialScannersMap.put(scanner, this.getDuration());
        }

        reportMap.put("availableScanners", this.availableScanners);
        reportMap.put("potentialScanners", potentialScannersMap);

        reportsRef.updateChildren(reportMap);
    }

    public void addToPotentialScanners(String userId){
        this.changePotentialScanners(userId, 1);
    }


    public void subtrectFromPotentialScanners(String userId){
        this.changePotentialScanners(userId, 0);
    }


    public boolean isScannerEnlisted(String userId){
        return potentialScanners.contains(userId);
    }


    public void setPotentialScanners(){
        if(this.id!=null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference reportsRef = ref.child("reports").child(this.id).child("potentialScanners");
            potentialScanners = new HashSet<>();
            reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                    for (DataSnapshot userId : contactChildren) {
                        potentialScanners.add(userId.getKey().toString());
                    }

                    availableScanners = potentialScanners.size();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        }
        else{
            Log.e(TAG,"id is null");
        }

    }

    public int getPotentialScannersSize() {
        return this.potentialScanners.size();
    }


    public Report(){
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)

        //this.setPotentialScanners();
    }


    public void setIsDogWithReporter(boolean isDogWithReporter) {
        this.isDogWithReporter = isDogWithReporter;
    }

    public boolean getIsDogWithReporter() {
        return isDogWithReporter;
    }


    public String getId() {return id;   }

    public String getReporterName() {
        return reporterName;
    }

    public String getFreeText() {
        return freeText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getExtraPhoneNumber() {
        return extraPhoneNumber;
    }

    public String getAssignedScanner() {
        if (assignedScanner == null) return "";
        return assignedScanner;
    }


    public String getStatus() {

        if (Objects.equals(this.status, "NEW")){
            if (getIsScannerEnlistedStatus()) return "SCANNER_ENLISTED";
            //if (potentialScanners.size() > 0) return "SCANNER_ENLISTED";
            //if (getIsManagerEnlistedStatus()) return "MANAGER_ENLISTED";
            return "NEW";
        }

        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public long getStartTime(){
        return this.startTime;
    }

    public String getStartTimeAsString() {
        Format format = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        return format.format(new Date(-this.startTime));
    }

    public String getAddress() { return this.address; }
    public String getUserId() {return userId;   }
    public int getAvailableScanners() { return this.availableScanners; }

    public String getCancellationText() {
        return cancellationText;
    }

    public String getClosingText() {
        return closingText;
    }

    public int getIncrementalReportId() { return incrementalReportId; }

    /*
    public void persistReport(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports");
        reportsRef.push().setValue(this);
    }*/

    public void setExtraPhoneNumber(String extraPhoneNumber) {
        this.extraPhoneNumber = extraPhoneNumber;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber=phoneNumber;
    }

    public void setMoreInformation(String moreInformation) {
        this.freeText = moreInformation;
    }

    public void setId(String id) {
        this.id = id;
        this.setPotentialScanners();
    }

    public void setStartTime() {
        this.startTime =  -Calendar.getInstance().getTime().getTime();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCancellationText(String cancellationText) {
        this.cancellationText = cancellationText;
    }

    public void setClosingText(String closingText) {
        this.closingText = closingText;
    }

    public double getLat() {
        return Lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }

    @Exclude
    public void setIncrementalReportId() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("reports").orderByChild("incrementalReportId").limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();

                for (DataSnapshot report : contactChildren) {
                    Report lastReport = report.getValue(Report.class);
                    nextIncrementalId = lastReport.getIncrementalReportId() + 1;
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        this.incrementalReportId = nextIncrementalId;
    }

    public void reportUpdateStatus(String status, ReportListActivity.MyCallBackClass myCallBackClass){
        String dbStatus = status;
        if (Objects.equals(dbStatus, "SCANNER_ENLISTED")) {
            dbStatus = "NEW";
            this.setIsScannerEnlistedStatus(true);
        }

        /*
        if (Objects.equals(dbStatus, "MANAGER_ENLISTED")) {
            dbStatus = "NEW";
            this.setIsManagerEnlistedStatus(true);
        }
        */

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<>();
        reportMap.put("status", dbStatus);
        reportMap.put("IsScannerEnlistedStatus", isScannerEnlistedStatus);
        //reportMap.put("IsManagerEnlistedStatus", isManagerEnlistedStatus);
        reportsRef.updateChildren(reportMap);
        if(myCallBackClass!=null)
            myCallBackClass.execute();
    }


    public void reportUpdateExtraPhoneNumber(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("extraPhoneNumber", this.extraPhoneNumber);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateIncrementalReportId(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id).child("incrementalReportId");
        reportsRef.setValue(this.incrementalReportId);
    }

    public void reportUpdateClosingText(String closingText){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        setClosingText(closingText);
        reportMap.put("closingText", closingText);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateCancellationText(String cancellationText){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        setCancellationText(cancellationText);
        reportMap.put("cancellationText", cancellationText);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateCancellationManagerType() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String, Object> reportMap = new HashMap<String, Object>();
        reportMap.put("cancellationUserType", "מנהל");
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateAssignedScanner(String userId){
        this.setAssignedScanner(userId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("assignedScanner", userId);
        reportsRef.updateChildren(reportMap);

    }

    public void updateOnTheWayTimestamp(){
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
                .format(new java.util.Date());

        this.setScannerOnTheWay(timeStamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("scannerOnTheWay", timeStamp);
        reportsRef.updateChildren(reportMap);

    }

    public boolean isOpenReport(){
        if ((Objects.equals(this.getStatus(), "CANCELED")) || (Objects.equals(this.getStatus(), "CLOSED")))
            return false;
        else return true;
    }

    public String statusInHebrew(){
        return translateStatus(this.getStatus());
    }

    public String translateStatus(String status){
        User user = getUser();
        if (user.getIsManager()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "קיים סורק זמין");
            map.put("MANAGER_ENLISTED", "בטיפול מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "נבחר סורק לקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            return map.get(status);
        }
        else{
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "סורק אחר נבחר לקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            if (user.getId()!=null && user.getId().equals(assignedScanner)){
                map.put("MANAGER_ASSIGNED_SCANNER", "צא ליעד, דווח יציאה לדרך");
            }
            return map.get(status);
        }

    }

    public String validate(){
        String error ="";
        if(reporterName.equals("")){
            error = error+ "חסר שם ";
        }
        if(phoneNumber.equals("")){
            error = error+ "חסר מספר טלפון ";
        }else {
            if (!phoneNumber.matches("^([0-9]{10})|([0-9]{3}-[0-9]{7})|([0-9]{2}-[0-9]{7})$")) {
                error = error + "מספר טלפון לא תקין";
            }
        }
        if(address.equals("")){
            error = error+ "חסרה כתובת ";
        }
        return error;
    }

    public boolean isHasSimilarReports(){
        return this.hasSimilarReports;
    }



    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", reportyName='" + reporterName + '\'' +
                ", status='" + getStatus() + '\'' +
                ", startTime='" + startTime + '\'' +
                ", address='" + address + '\'' +
                ", freeText='" + freeText + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", extraPhoneNumber='" + extraPhoneNumber + '\'' +
                ", assignedScanner='" + assignedScanner + '\'' +
                ", availableScanners=" + availableScanners +
                '}';
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getDistanceStr() {
        return "5";
    }

    public String getAvailableScannersStr() {
        return String.valueOf(availableScanners);
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        //return Integer.toString(ThreadLocalRandom.current().nextInt(3, 31));
        return duration;
    }


    public void setDistancevalue(int distancevalue) {
        this.distancevalue = distancevalue;
    }

    public int getDistancevalue() {
        return distancevalue;
    }

    public boolean getIsScannerEnlistedStatus() {
        return isScannerEnlistedStatus;
    }

    public void setIsScannerEnlistedStatus(boolean scannerEnlistedStatus) {
        isScannerEnlistedStatus = scannerEnlistedStatus;
    }

    /*
    public boolean getIsManagerEnlistedStatus() {
        return isManagerEnlistedStatus;
    }

    public void setIsManagerEnlistedStatus(boolean ManagerEnlistedStatus) {
        isManagerEnlistedStatus = ManagerEnlistedStatus;
    }
    */

    public void setAssignedScanner(String assignedScanner) {
        this.assignedScanner = assignedScanner;
    }

    public String getScannerOnTheWay() {
        return scannerOnTheWay;
    }

    public void setScannerOnTheWay(String scannerOnTheWay) {
        this.scannerOnTheWay = scannerOnTheWay;
    }


    private static long lastReportStartTime;

    public static void setLastReportStartTime(long lastReportStartTime) {
        Report.lastReportStartTime = lastReportStartTime;
    }

    public static long getLastReportStartTime() {

        return lastReportStartTime;
    }

    public boolean isAssignedScanner(String userId) {
        return Objects.equals(getAssignedScanner(), userId);
    }

    public String getCancellationUserType() {
        return cancellationUserType;
    }

    public void setCancellationUserType(String cancellationUserType) {
        this.cancellationUserType = cancellationUserType;
    }
}
