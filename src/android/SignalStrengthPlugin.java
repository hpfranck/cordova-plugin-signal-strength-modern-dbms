package com.example.signalstrength;
/*
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;

import org.apache.cordova.*;
import org.json.JSONArray;

import java.util.List;
*/

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.telephony.TelephonyManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthWcdma;

import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;

import android.os.Build;

import androidx.core.app.ActivityCompat;

import org.apache.cordova.*;
import org.json.JSONArray;

import java.util.List;

public class SignalStrengthPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("getCellDbm")) {
            this.getCellDbm(callbackContext);
            return true;
        } else if (action.equals("getWifiDbm")) {
            this.getWifiDbm(callbackContext);
            return true;
        }
        return false;
    }
/*
    private void getCellDbm(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        List<CellInfo> infos = tm.getAllCellInfo();
        if (infos != null) {
            for (CellInfo info : infos) {
                if (info instanceof CellInfoLte) {
                    CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                    callbackContext.success(lte.getDbm());
                    return;
                } else if (info instanceof CellInfoGsm) {
                    CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                    callbackContext.success(gsm.getDbm());
                    return;
                }
            }
        }
        callbackContext.success(-1); // fallback se nenhuma informação estiver disponível
    }
*/

private void getCellDbm(CallbackContext callbackContext) {
    Context context = this.cordova.getActivity().getApplicationContext();
    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        callbackContext.error("Permissão ACCESS_FINE_LOCATION não concedida");
        return;
    }

    List<CellInfo> infos = tm.getAllCellInfo();
    if (infos == null || infos.isEmpty()) {
        callbackContext.success(-1); // Nenhuma informação disponível
        return;
    }

    int melhorDbm = -999; // menor que qualquer sinal real

    for (CellInfo info : infos) {
        int dbm = -999;

        if (info instanceof CellInfoLte) {
            dbm = ((CellInfoLte) info).getCellSignalStrength().getDbm();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
                   info instanceof CellInfoNr) {
            dbm = ((CellInfoNr) info).getCellSignalStrength().getDbm();
        } else if (info instanceof CellInfoWcdma) {
            dbm = ((CellInfoWcdma) info).getCellSignalStrength().getDbm();
        } else if (info instanceof CellInfoGsm) {
            dbm = ((CellInfoGsm) info).getCellSignalStrength().getDbm();
        } else if (info instanceof CellInfoCdma) {
            dbm = ((CellInfoCdma) info).getCellSignalStrength().getDbm();
        }

        if (dbm > melhorDbm) {
            melhorDbm = dbm;
        }
    }

    callbackContext.success(melhorDbm);
}
    
    private void getWifiDbm(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int rssi = wifiInfo != null ? wifiInfo.getRssi() : -127;
        if (rssi != -127) {
            callbackContext.success(rssi);
        } else {
            callbackContext.error("Wi-Fi RSSI not available.");
        }
    }
}
