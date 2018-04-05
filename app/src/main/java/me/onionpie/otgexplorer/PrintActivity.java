package me.onionpie.otgexplorer;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Iterator;

public class PrintActivity extends AppCompatActivity {

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbInterface mInterface;
    private UsbEndpoint mEndPoint;
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "me.onionpie.otgexplorer.USB_PERMISSION";
    private static Boolean forceCLaim = true;

    HashMap<String, UsbDevice> mDeviceList;
    Iterator<UsbDevice> mDeviceIterator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);






    }

}
