package id.latenight.creativepos.util;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.zj.btsdk.BluetoothService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import id.latenight.creativepos.PrinterSections;
import id.latenight.creativepos.mobile.MainActivity;
import id.latenight.creativepos.R;
import pub.devrel.easypermissions.EasyPermissions;

public class MyApplication extends Application implements EasyPermissions.PermissionCallbacks, BluetoothHandler.HandlerInterface
{
    private final String TAG = MainActivity.class.getSimpleName();
    public static final int RC_BLUETOOTH = 0;
    public static final int RC_CONNECT_DEVICE = 1;
    public static final int RC_ENABLE_BLUETOOTH = 2;
    public static final int PERMISSION_BLUETOOTH = 1;

    private BluetoothAdapter mAdapeter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothService mService = new BluetoothService(this, new BluetoothHandler(this));
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    private BluetoothConnection connection;
    private boolean isPrinterReady = false;

    private static MyApplication sInstance;
    private Context context;
    private SessionManager sessionManager;

    private String textPrint = "";


    public static MyApplication getApplication() {
        return sInstance;
    }


    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
//        String[] params = {Manifest.permission.BLUETOOTH, Manifexxst.permission.BLUETOOTH_ADMIN};
//        if (!EasyPermissions.hasPermissions(this, params)) {
//            EasyPermissions.requestPermissions((Activity) context, "You need bluetooth permission", RC_BLUETOOTH, params);
//            return;
//        }
        sInstance = this;
//        connection = MyBluetoothPrintersConnections.selectFirstPaired();
    }

    public void setupBluetoothConnection(String address)
    {
        // Either setup your connection here, or pass it in
        mDevice = mService.getDevByMac(address);
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mService.connect(mDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothSocket getCurrentBluetoothConnection()
    {
        return mSocket;
    }

    public int isConnected() {
        return mService.getState();
    }

    public void sendMessage(String header, String body) {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {
            mService.write(PrinterCommands.SELECT_FONT_A);
            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage(header, "");

            //test Print
            mService.write(PrinterCommands.FEED_LINE);
            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            //
            mService.sendMessage(body, "");
            mService.write(PrinterCommands.FEED_LINE);
            mService.write(PrinterCommands.FEED_LINE);
            openCashDrawer();
        } else {
            if (mService.isBTopen()) {
//                Intent intent = new Intent(this, AdminActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_connect_with_printer), Toast.LENGTH_LONG).show();
            } else {
                requestBluetooth();
            }
        }
    }

    private void printNew(String header, String body)
    {

    }

    public void dailyReport(String header, String body) {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        if (isPrinterReady) {
            if(!sessionManager.getMainLogo().isEmpty()) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                byte[] sendData = new byte[0];
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(sessionManager.getMainLogo()).getContent());
                    PrintPic pg = new PrintPic();
                    pg.initCanvas(400);
                    pg.initPaint();
                    pg.drawImage(0, 0, bitmap);
                    sendData = pg.printDraw();
                } catch (IOException e) {
                    // Log exception
                }

                mService.write(sendData);
            }

            mService.write(PrinterCommands.SELECT_FONT_A);
            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
            mService.sendMessage(header, "");
            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
            mService.sendMessage(body, "");
            mService.write(PrinterCommands.FEED_LINE);
            mService.write(PrinterCommands.FEED_LINE);
        } else {
            if (mService.isBTopen()) {
//                Intent intent = new Intent(this, AdminActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_connect_with_printer), Toast.LENGTH_LONG).show();
            } else {
                requestBluetooth();
            }
        }
    }

    public void testPrint(String body) {
        if (!mService.isAvailable()) {
            Log.i(TAG, "printText: perangkat tidak support bluetooth");
            return;
        }
        printText();
        printer(textPrint);
//        if (isPrinterReady) {
//            mService.write(PrinterCommands.SELECT_FONT_A);
//            mService.write(PrinterCommands.ESC_ALIGN_CENTER);
//            mService.write(PrinterCommands.ESC_ALIGN_LEFT);
//            mService.sendMessage(body, "");
//            mService.write(PrinterCommands.FEED_LINE);
//            mService.write(PrinterCommands.FEED_LINE);
//        } else {
//            if (mService.isBTopen()) {
////                Intent intent = new Intent(this, AdminActivity.class);
////                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                startActivity(intent);
//                Toast.makeText(getApplicationContext(), getResources().getString(R.string.unable_connect_with_printer), Toast.LENGTH_LONG).show();
//            } else {
//                requestBluetooth();
//            }
//        }
    }

    public void printer(String body)
    {
        try {
            if (connection != null) {
                EscPosPrinter printer = new EscPosPrinter(connection, 203, 68f, 32);
                final String text = body;
                printer.printFormattedText(text);
            } else {
                Toast.makeText(this, String.valueOf(connection), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void printNew(String body)
    {
        try {
            if (connection != null) {
                EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);
                final String text =
                        body;

                printer.printFormattedText(text);
            } else {
                Toast.makeText(this, "No printer was connected!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("APP", "Can't print", e);
        }
    }

    public boolean openCashDrawer() {
        try {
            byte[] bytes = intArrayToByteArray(new int[]{27, 112, 0, 50, 250});
            mService.write(bytes);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Open drawer error", e);
            return false;
        }
    }

    private byte[] intArrayToByteArray(int[] Iarr) {
        byte[] bytes = new byte[Iarr.length];
        for (int i = 0; i < Iarr.length; i++) {
            bytes[i] = (byte) (Iarr[i] & 0xFF);
        }
        return bytes;
    }

    public void requestBluetooth() {
        if (mService != null) {
            if (!mService.isBTopen()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDeviceConnected() {
        isPrinterReady = true;
        Log.e("DEBUG", "Terhubung dengan perangkat");
    }

    @Override
    public void onDeviceConnecting() {
        Log.e("DEBUG", "Sedang menghubungkan...");
    }

    @Override
    public void onDeviceConnectionLost() {
        isPrinterReady = false;
        Log.e("DEBUG", "Koneksi perangkat terputus");
        setupBluetoothConnection(sessionManager.getPrinter());
    }

    @Override
    public void onDeviceUnableToConnect() {
        Log.e("DEBUG", "Tidak dapat terhubung ke perangkat");
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strings, @NonNull int[] ints) {

    }

    public void printText()
    {
        textPrint =
                "[L]Testing Printer\n" +
                "[C]\n" +
                "[L]<b>Effective Java</b>\n" +
                "[L]1 pcs[R]" + "Rp. 200.000" + "\n" +
                "[C]\n" +
                "[L]<b>Headfirst Java</b>\n" +
                "[L]1 pcs[R]" + "Rp. 200.000" + "\n" +
                "[C]\n" +
                "[L]<b>The Martian</b>\n" +
                "[L]1 pcs[R]" + "Rp. 200.000" + "\n" +
                "[C]--------------------------------\n" +
                "[L]TOTAL[R]" + "Rp. 200.000" + "\n" +
                "[C]--------------------------------\n" +
                "[C]<barcode type='ean13' height='10'>081290376487</barcode>\n" +
                "[C]--------------------------------\n" +
                "[C]Thanks For Shopping\n" +
                "[C]Akukasir\n" +
                "[C]\n";
    }
}