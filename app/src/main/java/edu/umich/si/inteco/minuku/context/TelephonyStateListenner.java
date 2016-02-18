package edu.umich.si.inteco.minuku.context;

import android.content.Context;
import android.os.BatteryManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import edu.umich.si.inteco.minuku.context.ContextStateManagers.PhoneStatusManager;

/**
 * Created by Armuro on 2/18/16.
 */
public class TelephonyStateListenner extends PhoneStateListener {

    private static final String LOG_TAG = "TelStateListener";

    private int _LTESignalStrength;
    private int _GsmSignalStrength;
    private int _CdmaSignalStrenth;
    private int _CdmaSignalStrenthLevel; // 1, 2, 3, 4
    private int _GeneralSignalStrength;
    private boolean _isGSM = false;
    private Context mContext;

    public TelephonyStateListenner(Context context){

        mContext = context;
        _LTESignalStrength = -9999;
        _GsmSignalStrength = -9999;
        _CdmaSignalStrenth = -9999;
        _GeneralSignalStrength = -9999;
        _CdmaSignalStrenthLevel = -9999;
        _isGSM = false;
        Log.d(LOG_TAG, "TestTelephony constructor " );
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {

        super.onSignalStrengthsChanged(signalStrength);
        Log.d(LOG_TAG, "TestTelephony signal strength change " );

        try{
            String ssignal = signalStrength.toString();
            String[] parts = ssignal.split(" ");

            /***
            The parts[] array will then contain these elements:
             1: GsmSignalStrength, 2: GsmBitErrorRate, 3: CdmaDbm, 4: CdmaEcio, 5: EvdoDbm, 6: EvdoEcio,  7: EvdoSnr,  8:LteSignalStrength,
             9: LteRsrp,  10: LteRsrq, 11: LteRssnr, 12: LteCqi, 13: gsm|lte,

            e.g. SignalStrength: 12 99 -12 -200 -12 -200 -1 99 2147483647 2147483647 2147483647 -1 gsm|lte
             **/

            TelephonyManager tm = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);

            int dbm = 0;

            PhoneStatusManager.setGSM(signalStrength.isGsm());

            /**
             * Returns current signal strength in "asu", ranging from 0-31
             * or -1 if unknown
             *
             * For GSM, dBm = -113 + 2*asu
             * 0 means "-113 dBm or less"
             * 31 means "-51 dBm or greater"
             *
             */

            if ( tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                dbm = Integer.parseInt(parts[8])*2-113;

                PhoneStatusManager.setLTESignalStrength(dbm);
                PhoneStatusManager.setGeneralSignalStrength(dbm);

//                _GeneralSignalStrength = _LTESignalStrength = dbm;
                Log.d(LOG_TAG, "TestTelephony LTE strength: " + PhoneStatusManager.getLTESignalStrength());

            }
            else if (signalStrength.isGsm()){ //if not LTE //tell if is gsm

                //GSM
                if (signalStrength.getGsmSignalStrength() != 99) {
                    dbm = -113 + 2* signalStrength.getGsmSignalStrength();

                    PhoneStatusManager.setGsmSignalStrength(dbm);
                    PhoneStatusManager.setGeneralSignalStrength(dbm);

//                    _GeneralSignalStrength = _GsmSignalStrength = dbm;
                   Log.d(LOG_TAG, "TestTelephony GSM strength: " + PhoneStatusManager.getGsmSignalStrength() + "asu: " + signalStrength.getGsmSignalStrength());

                }
                else{
                    PhoneStatusManager.setGsmSignalStrength(signalStrength.getGsmSignalStrength());
                    PhoneStatusManager.setGeneralSignalStrength(signalStrength.getGsmSignalStrength());
                }


            }
            /** if not GSM...: CDMA **/
            else {

                /**
                 * DBM
                 level 4 >= -75
                 level 3 >= -85
                 level 2 >= -95
                 level 1 >= -100

                 Ecio
                 level 4 >= -90
                 level 3 >= -110
                 level 2 >= -130
                 level 1 >= -150

                 level is the lowest of the two
                 actualLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;
                 */

                int snr = signalStrength.getEvdoSnr();
                int cdmaDbm = signalStrength.getCdmaDbm();
                int cdmaEcio = signalStrength.getCdmaEcio();

                int levelDbm;
                int levelEcio;

                if (snr == -1) { //if not 3G, use cdmaDBM or cdmaEcio
                    if (cdmaDbm >= -75) levelDbm = 4;
                    else if (cdmaDbm >= -85) levelDbm = 3;
                    else if (cdmaDbm >= -95) levelDbm = 2;
                    else if (cdmaDbm >= -100) levelDbm = 1;
                    else levelDbm = 0;

                    // Ec/Io are in dB*10
                    if (cdmaEcio >= -90) levelEcio = 4;
                    else if (cdmaEcio >= -110) levelEcio = 3;
                    else if (cdmaEcio >= -130) levelEcio = 2;
                    else if (cdmaEcio >= -150) levelEcio = 1;
                    else levelEcio = 0;

                    _CdmaSignalStrenthLevel = (levelDbm < levelEcio) ? levelDbm : levelEcio;

                    PhoneStatusManager.setCdmaSignalStrenthLevel(_CdmaSignalStrenthLevel);
                    Log.d(LOG_TAG, "TestTelephony not 3G CDMA strength: " + PhoneStatusManager.getCdmaSignalStrenthLevel());

                } else {	//if 3G, use SNR
                    if (snr == 7 || snr == 8) _CdmaSignalStrenthLevel =4;
                    else if (snr == 5 || snr == 6 ) _CdmaSignalStrenthLevel =3;
                    else if (snr == 3 || snr == 4) _CdmaSignalStrenthLevel = 2;
                    else if (snr ==1 || snr ==2) _CdmaSignalStrenthLevel =1;

                    PhoneStatusManager.setCdmaSignalStrenthLevel(_CdmaSignalStrenthLevel);
                    Log.d(LOG_TAG, "TestTelephony 3G CDMA strength: " +PhoneStatusManager.getCdmaSignalStrenthLevel());

                }

            }
        }catch (Exception e){
            Log.e(LOG_TAG, "Azure data base errror:" + e.getMessage() + "");

        }

        // Log.d(LOG_TAG, "Get Signal Strength string.. " + signalStrength.toString() );
    }

}
