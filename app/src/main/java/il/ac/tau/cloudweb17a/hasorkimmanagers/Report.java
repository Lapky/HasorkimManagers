package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


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

    private int availableScanners;

    private Set<String> potentialScanners ;


    private String cancellationText;
    private String userId;

    private boolean hasSimilarReports;
    private boolean isDogWithReporter;
    private String imageUrl;


    private boolean isScannerEnlistedStatus;
    private boolean isManagerEnlistedStatus;


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
            if (getIsManagerEnlistedStatus()) return "MANAGER_ENLISTED";
            return "NEW";
        }
        return this.status;
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
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void reportUpdateStatus(String status){
        String dbStatus = status;
        if (Objects.equals(dbStatus, "SCANNER_ENLISTED")) {
            dbStatus = "NEW";
            this.setIsScannerEnlistedStatus(true);
        }
        if (Objects.equals(dbStatus, "MANAGER_ENLISTED")) {
            dbStatus = "NEW";
            this.setIsManagerEnlistedStatus(true);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("status", dbStatus);
        reportMap.put("IsScannerEnlistedStatus", isScannerEnlistedStatus);
        reportMap.put("IsManagerEnlistedStatus", isManagerEnlistedStatus);
        reportsRef.updateChildren(reportMap);
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

    public void reportUpdateCancellationText(String cancellationText){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("cancellationText", cancellationText);
        reportsRef.updateChildren(reportMap);
    }

    public boolean isOpenReport(){
        if ((Objects.equals(this.getStatus(), "CANCELED")) || (Objects.equals(this.getStatus(), "CLOSED")))
            return false;
        else return true;
    }

    public String statusInHebrew(boolean isUserManager, String scannerId){
        if (isUserManager) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "סורק זמין");
            map.put("MANAGER_ENLISTED", "בטיפול מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "סורק קיבל את הקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            return map.get(this.getStatus());
        }
        else{
            Map<String, String> map = new HashMap<String, String>();
            map.put("NEW", "דיווח חדש");
            map.put("SCANNER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ENLISTED", "ממתין לאישור מנהל");
            map.put("MANAGER_ASSIGNED_SCANNER", "סורק אחר קיבל את הקריאה");
            map.put("SCANNER_ON_THE_WAY", "סורק יצא לדרך");
            map.put("CLOSED", "סגור");
            map.put("CANCELED", "בוטל");

            if (scannerId == assignedScanner){
                map.put("MANAGER_ASSIGNED_SCANNER", "צא ליעד, דווח יציאה לדרך");
            }
            return map.get(this.getStatus());
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
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", address='" + address + '\'' +
                ", freeText='" + freeText + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", extraPhoneNumber='" + extraPhoneNumber + '\'' +
                ", assignedScanner='" + assignedScanner + '\'' +
                ", availableScanners=" + availableScanners +
                '}';
    }


    public Bitmap getBitmapFromURL (String src){
        try{
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            Log.w(TAG, sStackTrace);
            return null;
        }
    }

/*    public void saveReport(Bitmap bitmap){
        if(bitmap !=null) {
            StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("images");
            String fileName = getUserId()+"_"+String.valueOf(new Date().getTime());
            StorageReference imageRef = imagesRef.child(fileName);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    imageUrl = downloadUrl.toString();
                    persistReport();
                }
            });
        }
        else
            persistReport();

    }*/

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
        return Integer.toString(ThreadLocalRandom.current().nextInt(3, 31));
        //return duration;
    }

    public String getDurationStr() {
        return "5 דקות";
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

    public boolean getIsManagerEnlistedStatus() {
        return isManagerEnlistedStatus;
    }

    public void setIsManagerEnlistedStatus(boolean ManagerEnlistedStatus) {
        isManagerEnlistedStatus = ManagerEnlistedStatus;
    }

    public void setAssignedScanner(String assignedScanner) {
        this.assignedScanner = assignedScanner;
    }

}
