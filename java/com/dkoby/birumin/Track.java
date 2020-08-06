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
import android.util.Log;
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

    private long startTime; 

    private static final long  MINTIME     = 500; /* ms */
    private static final float MINDISTANCE = 1;   /* m */

    private MainActivity mainActivity;
    private ArrayList<Point> points;
    private Track.LocationListener locationListener;
    private LocationManager locationManager;

    /*
     *
     */
    public Track(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        points = new ArrayList<Point>(3600);

        state = Track.State.NEW;

        movingTime  = 0;
        elapsedTime = 0;
        elevation   = 0;
        locationManager =
            (LocationManager)mainActivity.getSystemService(Context.LOCATION_SERVICE);

    }
    /*
     *
     */
    public void start() {
        synchronized(this) {
            if (state != Track.State.NEW)
                return;
        }

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

        synchronized(this) {
            state = Track.State.GET_POSITION;
        }

        resume();
    }
    /*
     *
     */
    public void pause() {
        synchronized(this) {
            if (state != Track.State.RECORD)
                return;
            state = Track.State.PAUSE;
            locationManager.removeUpdates(locationListener);
        }

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
    }
    /*
     *
     */
    public void resume() {
        synchronized(this) {
            if (state != Track.State.GET_POSITION && state != Track.State.PAUSE)
                return;
        }

        locationManager.requestLocationUpdates(
                locationProvider,
                MINTIME,
                MINDISTANCE,
                locationListener);

        synchronized(this) {
            if (state == Track.State.PAUSE)
                state = Track.State.RECORD;
        }

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
    }
    /*
     *
     */
    public void stop() {
        synchronized(this) {
            if (state != Track.State.PAUSE &&
                state != Track.State.GET_POSITION &&
                state != Track.State.RECORD
                )
                return;
        }

        synchronized(this) {
            if (state != Track.State.PAUSE)
                locationManager.removeUpdates(locationListener);

            if (state == Track.State.PAUSE || state == Track.State.RECORD)
            {
                state = Track.State.SAVE;
                store();
            }
            else
                state = Track.State.CANCEL;
        }

        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
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
        Point lastPoint;

        lastPoint = getLastPoint();
        if (lastPoint != null)
        {
            if (lastPoint.altitude > altitude)
                elevation += lastPoint.altitude - altitude;
        }

        synchronized(this) {
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
        return points.size();
    }
    /*
     *
     */
    private void store() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File filesDir = null;

                    /* Get last known storage, should be ext_sd. */
                    for (File dir: mainActivity.getExternalFilesDirs(null))
                        filesDir = dir;
                    if (filesDir == null)
                        throw new StoreException("No storage media");

                    Log.i(MainActivity.TAG, "Storage dir: " + filesDir);

                    if (!filesDir.exists())
                        filesDir.mkdir();

                    storeData(filesDir);

                    synchronized (Track.this) {
                        state = Track.State.DONE;
                    }
                    mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
                } catch (Exception e) {
                    Log.e(MainActivity.TAG, "Store error: " + e);

                    synchronized (Track.this) {
                        state = Track.State.ERROR;
                    }
                    mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
                }
            }
        }).start();
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
            xw.writeln("<gpx creator=\"" + MainActivity.VERSION + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" version=\"1.1\" xmlns=\"http://www.topografix.com/GPX/1/1\">");

            xw.writeln("<metadata>");
            xw.write("    <time>");
            xw.write(new SimpleDateFormat(dateFormat).format(date));
            xw.writeln("</time>");
            xw.writeln("</metadata>");
            xw.writeln("<trk>");
            xw.writeln("<name>Ride</name>");
            xw.writeln("<type>1</type>");
            xw.writeln("<trkseg>");
            synchronized (Track.this) {
                for (Point point: points)
                {
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

                    synchronized(Track.this) {
                        if (state == Track.State.SAVE ||
                            state == Track.State.DONE)
                            return;
                        elapsedTime++;
                        if (state == Track.State.RECORD)
                        {
                            movingTime++;
                            update = true;
                        }
                    }
                    if (update)
                        mainActivity.sendMessage(new MainMessage(MainMessage.MsgType.TRACK_UPDATE));
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

            synchronized(Track.this) {
                if (state == Track.State.GET_POSITION)
                {
                    startTime = System.currentTimeMillis();
                    state = Track.State.RECORD;
                    runThread();
                }
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

