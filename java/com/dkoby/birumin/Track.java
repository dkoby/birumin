/*
 * Jaro 2020.
 * Auxtoro estas Dmitrij Kobilin. 
 *
 * Nenia rajtigilo ekzistas.
 * Faru bone, ne faru malbone.
 */
package com.dkoby.birumin;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.io.FileOutputStream;

/*
 *
 */
public class Track {
    private static long POINT_TIMEOUT      = 15000;
    private static final long  MINTIME     = 1000; /* ms */
    private static final float MINDISTANCE = 0;   /* m */

    /*
     *
     */
    public enum State {
        NEW,
        GET_POSITION,
        RECORD,
        PAUSE,
        SAVE,
        DONE,
        ERROR,
        CANCEL,
    }

    public String locationProvider;
    public volatile long movingTime;
    public volatile long elapsedTime;
    public volatile Track.State state;
    public volatile long elevation;
    public volatile long distance;

    private long startTime; 
    private volatile Long lastPointTime;
    private boolean updates;

    private MainActivity mainActivity;
    private ArrayList<Point> points;
    private ArrayList<Point> wayPoints;
    private Track.LocationListener locationListener;
    private LocationManager locationManager;

    private TrackLoop trackLoop;

    /*
     *
     */
    public Track(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        points = new ArrayList<Point>(3600);
        wayPoints = new ArrayList<Point>(100);

        state = Track.State.NEW;

        locationManager =
            (LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);

        trackLoop = this.new TrackLoop();
    }
    /*
     *
     */
    public Track launch() {
        trackLoop.start();
        return this;
    } 

    /*
     *
     */
    private class TrackLoop extends Thread {
        private volatile boolean ready;
        private Looper looper;
        public Handler handler;
        /*
         *
         */
        public Looper getLooper() {
            return looper;
        }
        /*
         *
         */
        public void sendMessage(TrackMessage message) {
            if (!ready)
                return;

            Message msg;

            msg = handler.obtainMessage();
            msg.obj = message;
            handler.sendMessage(msg);
        }
        /*
         *
         */
        @Override
        public void run() {
            Looper.prepare();
            looper = Looper.myLooper();

            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    TrackMessage message;

//                    Log.i(MainActivity.TAG, "Track handle: " + msg);

                    message = (TrackMessage)msg.obj;
                    switch (message.msgType)
                    {
                        case TRACK_CONTROL_START:
                            onCStart();
                            break;
                        case TRACK_CONTROL_RESUME:
                            onCResume();
                            break;
                        case TRACK_CONTROL_PAUSE:
                            onCPause();
                            break;
                        case TRACK_CONTROL_STOP:
                            onCStop();
                            ready = false;
                            looper.quit();
                            break;
                        case TRACK_CONTROL_ADD_WPT:
                            Point lastPoint = getLastPoint();
                            if (lastPoint != null)
                            {
                                wayPoints.add(lastPoint);
                                mainActivity.sendMessage(
                                        new MainMessage(
                                            MainMessage.MsgType.DIALOG_INFO,
                                            new String("Waypoint was added")
                                            ));
                            }
                            break;
                        case TRACK_POINT_TIMEOUT:
                            stopUpdates();
                            /* XXX deffer? */
                            startUpdates();
                            break;
                        case TRACK_UPDATE:
                            mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
                            break;
                    }
                }
            };
            ready = true;

            Looper.loop();
        }
    }
    /*
     *
     */
    public void caddWpt() {
        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_CONTROL_ADD_WPT));
    }
    /*
     *
     */
    public void cstart() {
        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_CONTROL_START));
    }
    /*
     *
     */
    private void onCStart() {
        if (state != Track.State.NEW)
            return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false); /* XXX */
        criteria.setCostAllowed(false);
        criteria.setSpeedRequired(true);

        List<String> providers = locationManager.getProviders(criteria, true);
        Log.i(MainActivity.TAG, "Providers " + providers.size());
        for (String provider: providers)
            Log.i(MainActivity.TAG, "Provider " + provider);
        locationProvider = locationManager.getBestProvider(criteria, true);
        Log.i(MainActivity.TAG, "Best provider " + locationProvider);

        locationListener = this.new LocationListener();

        state = Track.State.GET_POSITION;

        cresume();
    }
    /*
     *
     */
    public void cpause() {
        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_CONTROL_PAUSE));
    }
    /*
     *
     */
    public void onCPause() {
        if (state != Track.State.RECORD)
            return;
        state = Track.State.PAUSE;

        stopUpdates();

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
    }
    /*
     *
     */
    public void cresume() {
        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_CONTROL_RESUME));
    }
    /*
     *
     */
    public void onCResume() {
        if (state != Track.State.GET_POSITION && state != Track.State.PAUSE)
            return;

        Point lastPoint = getLastPoint();
        if (lastPoint != null)
        {
            lastPoint.resume = true;
        }

        startUpdates();


        if (state == Track.State.PAUSE)
            state = Track.State.RECORD;

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
    }
    /*
     *
     */
    public void cstop() {
        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_CONTROL_STOP));
    }
    /*
     *
     */
    private void onCStop() {
        if (state != Track.State.PAUSE &&
            state != Track.State.GET_POSITION &&
            state != Track.State.RECORD
            )
            return;

        stopUpdates();

        if (state == Track.State.PAUSE || state == Track.State.RECORD)
        {
            state = Track.State.SAVE;
            mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
            try {
                store();
                state = Track.State.DONE;
            } catch (Exception e) {
                Log.e(MainActivity.TAG, "Store error: " + e);
                state = Track.State.ERROR;
            }
        } else {
            state = Track.State.CANCEL;
        }

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
    }
    /*
     *
     */
    private void startUpdates() {
        if (updates)
            return;

        locationManager.requestLocationUpdates(
                locationProvider,
                MINTIME,
                MINDISTANCE,
                locationListener,
                /* XXX is looper ready? */
                trackLoop.getLooper());
        updates = true;
    }
    /*
     *
     */
    private void stopUpdates() {
        if (!updates)
            return;

        lastPointTime = null;
        locationManager.removeUpdates(locationListener);
        updates = false;
    }
    /*
     *
     */
    private void addPoint(
            double latitude,
            double longitude,
            double altitude,
            float speed,
            long time)
    {
        boolean add;

        lastPointTime = new Long(System.currentTimeMillis());

        Point lastPoint;

        add = true;
        lastPoint = getLastPoint();
        if (lastPoint != null)
        {
            /*
             * set pi 3.1415926535897931

             * set R 6371e3 ; # meters
             * set phy1 [expr {$lat1 * $pi / 180}]
             * set phy2 [expr {$lat2 * $pi / 180}]
             * set lm1 [expr {$lon1 * $pi / 180}]
             * set lm2 [expr {$lon2 * $pi / 180}]
        
             * set x [expr {($lm2 - $lm1) * cos(($phy1 + $phy2) / 2)}]
             * set y [expr {$phy2 - $phy1}]
             * set d [expr {sqrt($x*$x + $y*$y) * $R}]
             */

            double R = 6371e3;
            double ph1 = lastPoint.latitude * Math.PI / 180;
            double ph2 =           latitude * Math.PI / 180; 
            double lm1 = lastPoint.longitude * Math.PI / 180;
            double lm2 =           longitude * Math.PI / 180; 
            double x = (lm2 - lm1) * Math.cos((ph1 + ph2) / 2);
            double y = ph2 - ph1;
            double d = Math.hypot(x, y) * R;

            /* NOTE filter if distance is less then 1 meters */
            if (d < 1.0)
                add = false;

            if (add)
            {
                distance += d;
                if (lastPoint.altitude > altitude)
                    elevation += lastPoint.altitude - altitude;
            }
        }

        synchronized(this) {
            if (add)
                points.add(new Point(latitude, longitude, altitude, speed, time));
        }
    }
    /*
     *
     */
    public Point getLastPoint() {
        synchronized(this) {
            if (points.size() == 0)
                return null;
            else
                return points.get(points.size() - 1);
        }
    }
    /*
     *
     */
    public int getPointsNum() {
        synchronized(this) {
            return points.size();
        }
    }
    /*
     *
     */
    private void store() throws Exception {
        File filesDir = null;

        /* Get last known storage, should be ext_sd. */
        for (File dir: mainActivity.getExternalFilesDirs(null))
        {
            Log.i(MainActivity.TAG, "Dirs: " + dir);
            filesDir = dir;
//            break;
        }
        if (filesDir == null)
            throw new StoreException("No storage media");

        Log.i(MainActivity.TAG, "Storage dir: " + filesDir);

        if (!filesDir.exists())
            filesDir.mkdir();

        storeData(filesDir);

        mainActivity.sendMessage(
                new MainMessage(
                    MainMessage.MsgType.DIALOG_INFO,
                    new String("Store success")
                    ));
    }
    /*
     *
     */
    private void storeData(File dir) throws Exception {
        StringBuffer sb;

        Date date = new Date(startTime);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ssZ");

        File fileName = new File(dir, simpleDateFormat.format(date) + ".gpx");
        Log.i(MainActivity.TAG, "File: " + fileName);

        final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZZZZZ";
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            XMLWriter xw = Track.this.new XMLWriter(fos);
            xw.writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xw.writeln("<gpx version=\"1.1\" creator=\"" + MainActivity.VERSION + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\">");

            xw.writeln("<metadata>");
            xw.write("    <time>");
            xw.write(new SimpleDateFormat(dateFormat).format(date));
            xw.writeln("</time>");
            xw.writeln("</metadata>");
            synchronized (Track.this) {
                for (Point point: wayPoints)
                {
                    Date pointDate = new Date(point.time);

                    xw.write("    <wpt");
                    xw.write(" lat=\"");
                    xw.write(String.valueOf(point.latitude));
                    xw.write("\"");
                    xw.write(" lon=\"");
                    xw.write(String.valueOf(point.longitude));
                    xw.writeln("\">");
                    xw.write("        <time>");
                    xw.write(new SimpleDateFormat(dateFormat).format(pointDate));
                    xw.writeln("</time>");
                    xw.writeln("    </wpt>");
                }
            }
            xw.writeln("<trk>");
            xw.writeln("<name>Ride</name>");
            xw.writeln("<type>1</type>");
            xw.writeln("<trkseg>");
            synchronized (Track.this) {
                for (Point point: points)
                {
                    if (point.resume)
                    {
                        xw.writeln("</trkseg>");
                        xw.writeln("<trkseg>");
                    }

                    Date pointDate = new Date(point.time);

                    xw.write("    <trkpt");
                    xw.write(" lat=\"");
                    xw.write(String.valueOf(point.latitude));
                    xw.write("\"");
                    xw.write(" lon=\"");
                    xw.write(String.valueOf(point.longitude));
                    xw.writeln("\">");
                    xw.write("        <ele>");
                    xw.write(String.valueOf(point.altitude));
                    xw.writeln("</ele>");
                    xw.write("        <time>");
                    xw.write(new SimpleDateFormat(dateFormat).format(pointDate));
                    xw.writeln("</time>");
                    xw.writeln("    </trkpt>");
                }
            };

            xw.writeln("</trkseg>");
            xw.writeln("</trk>");
            xw.writeln("</gpx>");
        } catch (Exception e) {
            throw e;
        }
    }
    /*
     *
     */
    private class XMLWriter {
        FileOutputStream f;

        /* XXX TODO ? force to UTF-8. */
        public XMLWriter(FileOutputStream f) {
            this.f = f;
        }
        public void writeln(String s) throws IOException {
            write(s);
            write("\n");
        }
        public void write(String s) throws IOException {
            f.write(s.getBytes());
        }
    }
    /*
     *
     */
    private void runThread() {
        new Thread(this.new TimeThread()).start();
    }
    /*
     *
     */
    public class StoreException extends Exception {
        /*
         *
         */
        public StoreException(String msg) {
            super(msg);
        }
    }
    /*
     *
     */
    public class Point {
        public double latitude; /* degrees. */
        public double longitude; /* degrees. */
        public double altitude; /* meters. */
        public float speed; /* m/sec. */
        public long time; /* ms */
        public boolean resume;

        public Point(
                double latitude,
                double longitude,
                double altitude,
                float speed,
                long time
                ) {
            this.latitude  = latitude;
            this.longitude = longitude; 
            this.altitude  = altitude;
            this.speed     = speed;
            this.time      = time;
        }
    }
    /*
     *
     */
    private class TimeThread implements Runnable {
        @Override
        public void run() {
            try {
                while (true)
                {
                    boolean update = false;

                    Thread.sleep(1000);

                    if (state == Track.State.SAVE ||
                        state == Track.State.DONE ||
                        state == Track.State.ERROR ||
                        state == Track.State.CANCEL
                    ) {
                        return;
                    }

                    elapsedTime++;
                    if (state == Track.State.RECORD)
                    {
                        movingTime++;
                        update = true;

                        if (lastPointTime != null)
                        {
                            long duration = System.currentTimeMillis() - lastPointTime.longValue();

                            if (duration > 0 && duration >= POINT_TIMEOUT)
                            {
                                lastPointTime = null;
                                trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_POINT_TIMEOUT));
                            }
                        }
                    }

                    if (update)
                        trackLoop.sendMessage(new TrackMessage(TrackMessage.MessageType.TRACK_UPDATE));
                }
            } catch (InterruptedException e) {

            }
        }
    }
    /*
     *
     */
    private class LocationListener implements android.location.LocationListener {
        /*
         * Called when the location has changed.
         */
        @Override
        public void onLocationChanged(Location location)
        {
//            Log.i(MainActivity.TAG, "--New loc--");
//            Log.i(MainActivity.TAG, "Lat:   " + location.getLatitude());
//            Log.i(MainActivity.TAG, "Lon:   " + location.getLongitude());
//            Log.i(MainActivity.TAG, "Alt:   " + location.getAltitude());
//            Log.i(MainActivity.TAG, "Speed: " + location.getSpeed());
//            Log.i(MainActivity.TAG, "Time:  " + location.getTime());

            addPoint(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getSpeed(),
                location.getTime());

            if (state == Track.State.GET_POSITION)
            {
                startTime = System.currentTimeMillis();
                state = Track.State.RECORD;
                runThread();
            }

            mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
        }
        /*
         * Called when the provider is disabled by the user.
         */
        @Override
        public void onProviderDisabled(String provider)
        {

        }
        /*
         * Called when the provider is enabled by the user.
         */
        @Override
        public void onProviderEnabled(String provider)
        {

        }
        /*
         * This method was deprecated in API level 29. This callback will never be invoked on Android Q and above.
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }
    }
}

