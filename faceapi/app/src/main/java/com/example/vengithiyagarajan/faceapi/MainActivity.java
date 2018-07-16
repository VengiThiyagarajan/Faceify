package com.example.vengithiyagarajan.faceapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;

import android.app.Activity;

import android.app.AlertDialog;

import android.app.Dialog;

import android.content.Context;

import android.content.DialogInterface;

import android.content.pm.PackageManager;

import android.os.Bundle;

import android.support.design.widget.Snackbar;

import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import android.view.View;

import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.vision.CameraSource;

import com.google.android.gms.vision.MultiProcessor;

import com.google.android.gms.vision.Tracker;

import com.google.android.gms.vision.face.Face;

import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

public final class MainActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;

    private GraphicOverlay mGraphicOverlay;

    private TextView mUpdates;

    private static final int RC_HANDLE_GMS = 9001;

    private static final int RC_HANDLE_CAMERA_PERM = 2;

    @Override

    public void onCreate(Bundle icicle) {

        super.onCreate(icicle);

        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);

        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        mUpdates = (TextView) findViewById(R.id.faceUpdates);

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (rc == PackageManager.PERMISSION_GRANTED) {

            createCameraSource();

        } else {

            requestCameraPermission();

        }

    }

    private void requestCameraPermission() {

        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,

                Manifest.permission.CAMERA)) {

            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);

            return;

        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                ActivityCompat.requestPermissions(thisActivity, permissions,

                        RC_HANDLE_CAMERA_PERM);

            }

        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,

                Snackbar.LENGTH_INDEFINITE)

                .setAction(R.string.ok, listener)

                .show();

    }

    private void createCameraSource() {

        Context context = getApplicationContext();

        FaceDetector detector = new FaceDetector.Builder(context)

                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)

                .build();

        detector.setProcessor(

                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())

                        .build());

        if (!detector.isOperational()) {

            new AlertDialog.Builder(this)

                    .setMessage("Face detector dependencies are not yet available.")

                    .show();

            Log.w(TAG, "Face detector dependencies are not yet available.");

            return;

        }

        mCameraSource = new CameraSource.Builder(context, detector)

                .setRequestedPreviewSize(1024, 720)

                .setFacing(CameraSource.CAMERA_FACING_BACK)

                .setRequestedFps(30.0f)

                .setAutoFocusEnabled(true)

                .build();

    }

    /**

     * Restarts the camera.

     */

    @Override

    protected void onResume() {

        super.onResume();

        startCameraSource();

    }

    /**

     * Stops the camera.

     */

    @Override

    protected void onPause() {

        super.onPause();

        mPreview.stop();

    }

    /**

     * Releases the resources associated with the camera source, the associated detector, and the

     * rest of the processing pipeline.

     */

    @Override

    protected void onDestroy() {

        super.onDestroy();

        if (mCameraSource != null) {

            mCameraSource.release();

        }

    }

    @Override

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode != RC_HANDLE_CAMERA_PERM) {

            Log.d(TAG, "Got unexpected permission result: " + requestCode);

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            return;

        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Camera permission granted - initialize the camera source");

// we have permission, so create the camerasource

            createCameraSource();

            return;

        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +

                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                finish();

            }

        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Face Tracker sample")

                .setMessage(R.string.no_camera_permission)

                .setPositiveButton(R.string.ok, listener)

                .show();

    }

//create camera source preview

    private void startCameraSource() {

// check that the device has play services available.

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(

                getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {

            Dialog dlg =

                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);

            dlg.show();

        }

        if (mCameraSource != null) {

            try {

                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {

                Log.e(TAG, "Unable to start camera source.", e);

                mCameraSource.release();

                mCameraSource = null;

            }

        }

    }

//Graphic Face Tracker

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override

        public Tracker<Face> create(Face face) {

            return new GraphicFaceTracker(mGraphicOverlay,MainActivity.this);

        }

    }

    private class GraphicFaceTracker extends Tracker<Face> {

        private GraphicOverlay mOverlay;

        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay,Context context) {

            mOverlay = overlay;

            mFaceGraphic = new FaceGraphic(overlay,context);

        }

        @Override

        public void onNewItem(int faceId, Face item) {

            mFaceGraphic.setId(faceId);

        }

        @Override

        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {

            mOverlay.add(mFaceGraphic);

            mFaceGraphic.updateFace(face);

        }

        @Override

        public void onMissing(FaceDetector.Detections<Face> detectionResults) {

            mOverlay.remove(mFaceGraphic);

        }

        @Override

        public void onDone() {

            mOverlay.remove(mFaceGraphic);

        }

    }

}