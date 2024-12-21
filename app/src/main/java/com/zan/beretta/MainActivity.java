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
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView cellRsrp;
    private TextView cellRsrq;
    private TextView cellSnr;
    private TextView cellMcc;
    private TextView cellMnc;
    private TextView cellId;
    private TextView cellPci;
    private TextView cellTac;

    private TelephonyManager telephonyManager;

    private Handler handler;
    private Runnable updateSignalTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cellRsrp = findViewById(R.id.cellRsrp);
        cellRsrq = findViewById(R.id.cellRsrq);
        cellSnr = findViewById(R.id.cellSnr);
        cellMcc = findViewById(R.id.cellMcc);
        cellMnc = findViewById(R.id.cellMnc);
        cellId = findViewById(R.id.cellID);
        cellPci = findViewById(R.id.cellPci);
        cellTac = findViewById(R.id.cellTac);
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
                handler.postDelayed(this, 100); // Update setiap 10 detik
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
                        int cid = cellInfoLte.getCellIdentity().getCi();
                        int pci = cellInfoLte.getCellIdentity().getPci();
                        int tac = cellInfoLte.getCellIdentity().getTac();
                        long bandwidth = cellInfoLte.getCellIdentity().getBandwidth();
                        int rssnr = cellInfoLte.getCellSignalStrength().getRssnr();
                        int cqi = cellInfoLte.getCellSignalStrength().getCqi();
                        int rssi = cellInfoLte.getCellSignalStrength().getRssi();
                        int level = cellInfoLte.getCellSignalStrength().getLevel();
                        int dbm = cellInfoLte.getCellSignalStrength().getDbm();
                        long timeStamp = cellInfoLte.getTimeStamp();
                        int timingAdvance = cellInfoLte.getCellSignalStrength().getTimingAdvance();
                        int asuLevel = cellInfoLte.getCellSignalStrength().getAsuLevel();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HHmmSS", Locale.getDefault());
                        String formattedDate = sdf.format(new Date(timeStamp));
                        long snr = rsrp / bandwidth;

                        cellRsrp.setText("RSSNR: " + rssnr + " dBm");
                        cellRsrq.setText("RSSIa: " + rssi + " dB");
                        cellSnr.setText("CQI: " + cqi + " dB");
                        cellId.setText("Level: " + level);
                        cellMcc.setText("DBM: " + dbm);
                        cellMnc.setText("ASU Level: " + asuLevel);
                        cellPci.setText("Tima Stamp: " + formattedDate);
                        cellTac.setText("TAC: " + timingAdvance);

                        return;
                    }
                }
                cellRsrp.setText("Tidak ada informasi LTE tersedia");
            } else {
                cellRsrp.setText("Gagal mendapatkan informasi sinyal");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Izin akses lokasi diperlukan untuk mendapatkan sinyal", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Error", "UpdateSignalStrength Error: " + e.getMessage());
            cellRsrp.setText("Error: " + e.getMessage());
        }
    }
}
