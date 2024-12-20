package com.zan.beretta;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView signalStrengthTextView;
    private TextView cellMcc;
    private TextView cellMnc;
    private TextView cellId;
    private TelephonyManager telephonyManager;

    private Handler handler;
    private Runnable updateSignalTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signalStrengthTextView = findViewById(R.id.signalStrengt1);
        cellMcc = findViewById(R.id.cellMcc);
        cellMnc = findViewById(R.id.cellMnc);
        cellId = findViewById(R.id.cellID);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        handler = new Handler();

        // Periksa izin ACCESS_FINE_LOCATION
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startSignalStrengthUpdates();
        }
    }

    private void startSignalStrengthUpdates() {
        updateSignalTask = new Runnable() {
            @Override
            public void run() {
                updateSignalStrength();
                handler.postDelayed(this, 5000); // Update setiap 10 detik
            }
        };
        handler.post(updateSignalTask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSignalStrengthUpdates();
        } else {
            Toast.makeText(this, "Izin diperlukan untuk mendapatkan sinyal", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateSignalTask != null) {
            handler.removeCallbacks(updateSignalTask);
        }
    }

    private void updateSignalStrength() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            if (cellInfoList != null) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        CellSignalStrengthLte signalStrengthLte = cellInfoLte.getCellSignalStrength();

                        int rsrp = signalStrengthLte.getRsrp();
                        int rsrq = signalStrengthLte.getRsrq();
                        int snr = signalStrengthLte.getRssnr();
                        int cid = cellInfoLte.getCellIdentity().getCi();

                        // Perbarui TextView dengan nilai sinyal dan identitas seluler
                        signalStrengthTextView.setText("RSRP: " + rsrp + " dBm\nRSRQ: " + rsrq + " dB\nSNR: " + snr + " dB");
                        cellId.setText("Cell ID: " + cid);
                        cellMcc.setText("MCC: " + cellInfoLte.getCellIdentity().getMcc());
                        cellMnc.setText("MNC: " + cellInfoLte.getCellIdentity().getMnc());
                        return;
                    }
                }
                signalStrengthTextView.setText("Tidak ada informasi LTE tersedia");
            } else {
                signalStrengthTextView.setText("Gagal mendapatkan informasi sinyal");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Izin akses lokasi diperlukan untuk mendapatkan sinyal", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            signalStrengthTextView.setText("Error: " + e.getMessage());
        }
    }
}
