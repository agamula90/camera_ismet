package com.ismet.usbcamera;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ismet.usbcamera.table.DividerItemDecoration;
import com.ismet.usbcamera.table.GridAdapter;
import com.ismet.usbcamera.table.RowItem;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.usb.UVCCamera;

import java.util.Arrays;
import java.util.List;

public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 1;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private UVCCameraHandler mCameraHandler;
    /**
     * for camera preview display
     */
    private CameraViewInterface mUVCCameraView;
    /**
     * for open&start / stop&close camera preview
     */
    private ToggleButton mCameraButton;
    /**
     * button for start/stop recording
     */
    private ImageButton mCaptureButton;

    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraButton = (ToggleButton)findViewById(R.id.camera_button);
        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCaptureButton = (ImageButton)findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(mOnClickListener);
        mCaptureButton.setVisibility(View.INVISIBLE);
        final View view = findViewById(R.id.camera_view);
        view.setOnLongClickListener(mOnLongClickListener);

        mUVCCameraView = (CameraViewInterface)view;
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float)PREVIEW_HEIGHT);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView, 1, PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.table);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration());

        gridAdapter = new GridAdapter(readRowItems(), this);
        recyclerView.setAdapter(gridAdapter);

        findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridAdapter.selectPrevious();
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gridAdapter.selectNext();
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Send pressed [" + gridAdapter.getCurrentRow() + "]", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Save pressed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<RowItem> readRowItems() {
        return Arrays.asList(
                new RowItem(0, new String[]{"a", "b", "c", "d"}),
                new RowItem(1, new String[]{"e", "f", "g", "h"}),
                new RowItem(2, new String[]{"i", "j", "k", "l"}));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUSBMonitor.register();
        if (mUVCCameraView != null)
            mUVCCameraView.onResume();
    }

    @Override
    protected void onPause() {
        mCameraHandler.close();
        if (mUVCCameraView != null)
            mUVCCameraView.onPause();
        checkCameraButtonSilent(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCameraHandler != null) {
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        super.onDestroy();
    }



    /**
     * event handler when click camera / capture button
     */
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.capture_button:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                            if (!mCameraHandler.isRecording()) {
                                mCaptureButton.setColorFilter(0xffff0000);	// turn red
                                mCameraHandler.startRecording();
                            } else {
                                mCaptureButton.setColorFilter(0);	// return to default color
                                mCameraHandler.stopRecording();
                            }
                        }
                    }
                    break;
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            switch (compoundButton.getId()) {
                case R.id.camera_button:
                    if (isChecked && !mCameraHandler.isOpened()) {
                        CameraDialog.showDialog(MainActivity.this);
                    } else {
                        mCameraHandler.close();
                        checkCameraButtonSilent(false);
                    }
                    break;
            }
        }
    };

    /**
     * capture still image when you long click on preview image(not on buttons)
     */
    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mCameraHandler.captureStill();
                        }
                        return true;
                    }
            }
            return false;
        }
    };


    private void checkCameraButtonSilent(final boolean checked) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mCameraButton.setOnCheckedChangeListener(null);
                    mCameraButton.setChecked(checked);
                } finally {
                    mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
                }
                if (!checked && mCaptureButton != null) {
                    mCaptureButton.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void startPreview() {
        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        mCameraHandler.startPreview(new Surface(st));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaptureButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.v(TAG, "onConnect:");
            mCameraHandler.open(ctrlBlock);
            startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            Log.v(TAG, "onDisconnect:");
            if (mCameraHandler != null) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mCameraHandler.close();
                    }
                }, 0);
                checkCameraButtonSilent(false);
            }
        }
        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            checkCameraButtonSilent(false);
        }
    };

    /**
     * to access from CameraDialog
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        Log.v(TAG, "onDialogResult:canceled=" + canceled);
        if (canceled) {
            checkCameraButtonSilent(false);
        }
    }
}