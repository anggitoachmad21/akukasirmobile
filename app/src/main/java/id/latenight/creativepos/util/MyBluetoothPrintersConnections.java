package id.latenight.creativepos.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

public class MyBluetoothPrintersConnections extends BluetoothConnections {

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @return a EscPosPrinterCommands instance
     */
    public static BluetoothConnection selectFirstPaired() {
        BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
        BluetoothConnection[] bluetoothPrinters = printers.getList();

        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            for (BluetoothConnection printer : bluetoothPrinters) {
                try {
                    return printer.connect();
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */


    @SuppressLint("MissingPermission")
    public BluetoothConnection[] getList() {
        BluetoothConnection[] bluetoothDevicesList = super.getList();

        if (bluetoothDevicesList == null) {
            return null;
        }

        int i = 0;
        BluetoothConnection[] printersTmp = new BluetoothConnection[bluetoothDevicesList.length];
        for (BluetoothConnection bluetoothConnection : bluetoothDevicesList) {
            BluetoothDevice device = bluetoothConnection.getDevice();
            printersTmp[i++] = new BluetoothConnection(device);
        }
        BluetoothConnection[] bluetoothPrinters = new BluetoothConnection[i];
        System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i);
        return bluetoothPrinters;
    }
}
