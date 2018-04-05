package me.onionpie.otgexplorer;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {



    private static final String ACTION_USB_PERMISSION =
            "me.onionpie.otgexplorer.USB_PERMISSION";
    private static final String LOGTAG = "otg_explorer";
    private UsbManager usbManager;
    private String mInfo = "设备列表:";
    //读取usbdevice时使用
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    TextView mInfoTextView;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            mInfo = "";
                            collectDeviceInfo(usbDevice);
                            mInfoTextView.setText(String.format("权限获取成功，设备信息：%s", mInfo));
                            //读取usbDevice里的内容
                        }
                    } else {
                        mInfoTextView.setText("权限被拒绝了");
                        Log.v(LOGTAG, "permission is denied");
                    }
                }
            } else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null) {
                    //close connection
                    mInfoTextView.setText("与设备断开连接");
                }
            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                //当设备插入时执行具体操作
                Log.v(LOGTAG,"设备接入");
                mInfoTextView.setText("设备接入");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInfoTextView = (TextView) findViewById(R.id.info);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        IntentFilter intentFilter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, intentFilter);

        for (UsbDevice usbDevice : usbManager.getDeviceList().values()) {

            Log.i(LOGTAG, "USB device name " + usbDevice.getDeviceName());
            Log.i(LOGTAG, "USB product name " + usbDevice.getProductName());

            collectDeviceInfo(usbDevice);
            if (hasPermission(usbDevice)) {


                Log.i(LOGTAG,   usbDevice.getDeviceName() +" got permission");


                printData(usbDevice);


                //读取usbDevice里的内容
            } else {
                Log.i(LOGTAG,   usbDevice.getDeviceName() +" no permission");
                requestPermission(usbDevice);
            }
        }
        mInfoTextView.setText(mInfo);
    }

    private void collectDeviceInfo(UsbDevice usbDevice) {
        mInfo += "\n" +
                "DeviceID: " + usbDevice.getDeviceId() + "\n" +
                "DeviceName: " + usbDevice.getDeviceName() + "\n" +
                "DeviceClass: " + usbDevice.getDeviceClass() + " - "
                + translateDeviceClass(usbDevice.getDeviceClass()) + "\n" +
                "DeviceSubClass: " + usbDevice.getDeviceSubclass() + "\n" +
                "VendorID: " + usbDevice.getVendorId() + "\n" +
                "ProductID: " + usbDevice.getProductId() + "\n";
    }

    private void printData(UsbDevice mDevice){

        String test = "THIS IS A PRINT TEST";
        final byte [] testBytes = test.getBytes(Charset.forName("UTF-8"));
        
        final UsbEndpoint mEndpointBulkOut;

        UsbInterface intf = mDevice.getInterface(0);

        Log.i(LOGTAG, "USB interface name " + intf.getName());

        Log.i(LOGTAG, "USB interface count " + intf.getEndpointCount());

        UsbEndpoint ep = intf.getEndpoint(0);

        Log.i(LOGTAG, "USB endpoint " + ep.toString() );

        if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
            if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                mEndpointBulkOut = ep;
                usbDeviceConnection = usbManager.openDevice(mDevice);
                if(usbDeviceConnection!=null)
                {
                    Log.i(LOGTAG, "Connection connected");
                    Toast.makeText(this, "Device connected", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(LOGTAG, "Connection not connected");
                }
                boolean forceClaim = true;
                usbDeviceConnection.claimInterface(intf, forceClaim );
                //    Integer res = usbDeviceConnection.bulkTransfer(mEndpointBulkOut, testBytes, testBytes.length, 10000);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int b = usbDeviceConnection.bulkTransfer(mEndpointBulkOut, testBytes, testBytes.length, 100000);
                        Log.i(LOGTAG, " Return Status b-->" + b);
                    }
                }).start();

                usbDeviceConnection.releaseInterface(intf);
            }
        }







    }

    private String translateDeviceClass(int deviceClass) {
        switch (deviceClass) {
            case UsbConstants.USB_CLASS_APP_SPEC:
                return "Application specific USB class";
            case UsbConstants.USB_CLASS_AUDIO:
                return "USB class for audio devices";
            case UsbConstants.USB_CLASS_CDC_DATA:
                return "USB class for CDC devices (communications device class)";
            case UsbConstants.USB_CLASS_COMM:
                return "USB class for communication devices";
            case UsbConstants.USB_CLASS_CONTENT_SEC:
                return "USB class for content security devices";
            case UsbConstants.USB_CLASS_CSCID:
                return "USB class for content smart card devices";
            case UsbConstants.USB_CLASS_HID:
                return "USB class for human interface devices (for example, mice and keyboards)";
            case UsbConstants.USB_CLASS_HUB:
                return "USB class for USB hubs";
            case UsbConstants.USB_CLASS_MASS_STORAGE:
                return "USB class for mass storage devices";
            case UsbConstants.USB_CLASS_MISC:
                return "USB class for wireless miscellaneous devices";
            case UsbConstants.USB_CLASS_PER_INTERFACE:
                return "USB class indicating that the class is determined on a per-interface basis";
            case UsbConstants.USB_CLASS_PHYSICA:
                return "USB class for physical devices";
            case UsbConstants.USB_CLASS_PRINTER:
                return "USB class for printers";
            case UsbConstants.USB_CLASS_STILL_IMAGE:
                return "USB class for still image devices (digital cameras)";
            case UsbConstants.USB_CLASS_VENDOR_SPEC:
                return "Vendor specific USB class";
            case UsbConstants.USB_CLASS_VIDEO:
                return "USB class for video devices";
            case UsbConstants.USB_CLASS_WIRELESS_CONTROLLER:
                return "USB class for wireless controller devices";
            default:
                return "Unknown USB class!";

        }
    }

    public boolean hasPermission(UsbDevice device) {
        return usbManager.hasPermission(device);
    }

    public void requestPermission(UsbDevice usbDevice) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(usbDevice, pendingIntent);
    }
}
