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
import android.util.Log;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

import android.net.NetworkInfo;
import android.net.NetworkCapabilities;
import android.net.Network;
import android.telephony.PhoneStateListener;
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
import android.telephony.SignalStrength;


import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthNr;

import android.os.Build;

import androidx.core.app.ActivityCompat;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;

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
        } else if (action.equals("getSignalLevel")) {
            this.getSignalLevel(callbackContext);
            return true;
        }        
        return false;
    }

    private void getCellDbm(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            int tipo = tm.getNetworkType();
            Log.d("Signal", "Tipo de rede: " + tipo);


            // Verifica permissão obrigatória no Android 10+
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                callbackContext.error("Permissão ACCESS_FINE_LOCATION não concedida");
                return;
            }

            int networkType = tm.getNetworkType();
            List<CellInfo> infos = tm.getAllCellInfo();

            if (infos == null || infos.isEmpty()) {
                callbackContext.success(-1); // Nenhuma informação disponível
                return;
            }

            int melhorDbm = -999; // Inicializa com valor inválido

            for (CellInfo info : infos) {
                int dbm = -999;

                // 📶 4G - LTE
                if (info instanceof CellInfoLte) {
                    dbm = ((CellInfoLte) info).getCellSignalStrength().getDbm();

                //  5G - NR (apenas Android 10+)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info instanceof CellInfoNr) {
                    dbm = ((CellInfoNr) info).getCellSignalStrength().getDbm();

                //  3G - WCDMA (nem todos os devices suportam)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                        info instanceof CellInfoWcdma) {
                    dbm = ((CellInfoWcdma) info).getCellSignalStrength().getDbm();

                //  3G (fallback) - alguns dispositivos representam 3G com GSM
                } else if (info instanceof CellInfoGsm) {
                    if (isNetwork3G(networkType)) {
                        dbm = ((CellInfoGsm) info).getCellSignalStrength().getDbm();
                    }

                //  2G (ou raros 3G/CDMA antigos) - CDMA
                } else if (info instanceof CellInfoCdma) {
                    dbm = ((CellInfoCdma) info).getCellSignalStrength().getDbm();
                }

                if (dbm > melhorDbm) {
                    melhorDbm = dbm;
                }
            }

            //Log.d("SignalStrength", "Detectado: " + info.getClass().getSimpleName() + " => " + dbm + " dBm");

            callbackContext.success(melhorDbm);
    }

    private boolean isNetwork3G(int networkType) {
        return networkType == TelephonyManager.NETWORK_TYPE_UMTS ||
            networkType == TelephonyManager.NETWORK_TYPE_HSDPA ||
            networkType == TelephonyManager.NETWORK_TYPE_HSUPA ||
            networkType == TelephonyManager.NETWORK_TYPE_HSPA ||
            networkType == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
            networkType == TelephonyManager.NETWORK_TYPE_EVDO_A ||
            networkType == TelephonyManager.NETWORK_TYPE_EVDO_B;
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


    private void getSignalLevel(CallbackContext callbackContext) {
        Context context = this.cordova.getActivity().getApplicationContext();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {

            // Verifica se é Wi-Fi
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                int rssi = wifiInfo != null ? wifiInfo.getRssi() : -127;
                int level = WifiManager.calculateSignalLevel(rssi, 5); // level entre 0 e 4

                JSONObject result = new JSONObject();
                try {
                    result.put("tipo", "wifi");
                    result.put("level", level);
                } catch (Exception e) {
                    callbackContext.error("Erro JSON Wi-Fi");
                    return;
                }
                Log.d("Level", "Level de  Wi-Fi: " + result.toString());
                callbackContext.success(result.toString());
                return;
            }

            // Caso seja rede móvel (cellular)
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                callbackContext.error("Permissão ACCESS_FINE_LOCATION não concedida");
                return;
            }

            PhoneStateListener listener = new PhoneStateListener() {
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    super.onSignalStrengthsChanged(signalStrength);
                    int level = signalStrength.getLevel(); // de 0 a 4

                    String tipo = mapNetworkType(tm.getNetworkType());

                    JSONObject result = new JSONObject();
                    try {
                        result.put("tipo", tipo);
                        result.put("level", level);
                    } catch (Exception e) {
                        callbackContext.error("Erro JSON móvel");
                        return;
                    }

                    Log.d("Level", "Level de rede: " + result.toString());

                    callbackContext.success(result.toString());
                    tm.listen(this, PhoneStateListener.LISTEN_NONE); // Para escutar só uma vez
                }
            };

            tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } else {
            callbackContext.error("Sem rede ativa");
        }
    }

    private String mapNetworkType(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR: return "5g";
            case TelephonyManager.NETWORK_TYPE_LTE: return "4g";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3g";
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2g";
            default: return "cellular";
        }
    }    
}
