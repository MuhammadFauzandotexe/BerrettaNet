package com.zan.beretta;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView signalStrengthTextView;
    private TelephonyManager telephonyManager;
    private Handler handler;
    private Runnable updateSignalTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signalStrengthTextView = findViewById(R.id.signalStrengt1);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        handler = new Handler();

        // Periksa izin ACCESS_FINE_LOCATION jika diperlukan
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Jalankan pembaruan sinyal secara periodik setiap 1 detik
        updateSignalTask = new Runnable() {
            @Override
            public void run() {
                updateSignalStrength();
                handler.postDelayed(this, 10000); // Jalankan lagi setelah 1 detik
            }
        };
        handler.post(updateSignalTask);
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
            List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();

            if (cellInfoList != null) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrength signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        int rsrp = signalStrength.getDbm();

                        // Perbarui TextView dengan nilai RSRP
                        signalStrengthTextView.setText("RSRP: " + rsrp + " dBm");
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
