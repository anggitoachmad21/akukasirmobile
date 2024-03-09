package id.latenight.creativepos.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScanActivity extends Activity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        mScannerView.setAspectTolerance(0.5f);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("tag", rawResult.getContents()); // Prints scan results
        Log.v("tag", rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        Intent intent = new Intent();
        intent.putExtra("barcode", rawResult.getContents());
        setResult(RESULT_OK, intent);
        finish();

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}