package com.example.signalstrength;

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
