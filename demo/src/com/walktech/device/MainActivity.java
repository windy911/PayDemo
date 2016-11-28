package com.walktech.device;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.walktech.template.impl.TransactionListener;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends BaseActivity {
    private static final String TAG = "Walktech Demo";
    private static final boolean D = true;

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_HOST = 2;
    public static final int MESSAGE_SWIPER = 3;
    public static final int MESSAGE_IN = 4;
    public static final int MESSAGE_OUT = 5;
    public static final int SELECTION = 6;
    public static final int ENABLE_BUTTON = 7;

    public static final int ENABLE_TRANS_BUTTON = 1;
    public static final int ENABLE_CONNECT_BUTTON = 2;
    public static final int ENABLE_CARD_BUTTON = 3;
    public static final int ENABLE_NFC_BUTTON = 4;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    private ListView mViewHost;

    private Button mButtonCard;
    private Button mButtonNFC;
    private Button mButtonStatus;
    private Button mButtonScan;
    private Button mButtonCancel;
    private Button mButtonClear;
    private Button mButtonConnect;
    private Button mButtonDisonnect;
    private Button mButtonFW;
    private Button mButtonSDK;

    private String mCurentPath;
    private String mConfigFile = "conf.prop";

    private String mConnectedDeviceName = null;
    private String mConnectedDeviceMAC = null;

    public ArrayAdapter<String> mArrayAdapterHost;

    private BluetoothAdapter mBluetoothAdapter = null;

    private TransactionProcess mmposService = null;

    public int cardType;

    private TransactionListener swiper = null;

    private Context mcontext;

    public static boolean bMposConnected;
    public static MainActivity mInstance;
    public TextView tvStatusBT;


    private final static byte CONECT_RETRY_COUNT = 1;
    public static byte mConnectAgain = CONECT_RETRY_COUNT;
    private TextView tvTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        mInstance = this;
        setContentView(R.layout.main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mCurentPath = this.getCacheDir().getPath();
        mcontext = this;
        initView();
    }

    private void initView() {
        initTitle();
    }

    private void initTitle() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("设置");

        tvStatusBT = (TextView) findViewById(R.id.tvStatusBT);
        tvStatusBT.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mmposService == null) {
                setup();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
    }

    private Properties loadConfig(Context context, String file) {
        Properties properties = new Properties();
        try {
            FileInputStream s = new FileInputStream(file);
            properties.load(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    private void saveConfig(Context context, String file, Properties properties) {
        try {
            FileOutputStream s = new FileOutputStream(file, false);
            properties.store(s, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bytes2CharString(byte[] b, int length) {
        String ret = "";
        for (int i = 0; i < length; i++) {
            String hex = Character.toString((char) b[i]);
            ret += hex;
        }
        return ret;
    }

    private void setup() {
        Log.d(TAG, "setup()");

        mArrayAdapterHost = new ArrayAdapter<String>(this, R.layout.message);
        mViewHost = (ListView) findViewById(R.id.log);
        mViewHost.setAdapter(mArrayAdapterHost);

        TextView log = new TextView(this);
        log.setText("DEBUG INFO");
        mViewHost.addHeaderView(log);

        mButtonCard = (Button) findViewById(R.id.button_basic);
        mButtonCard.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cardType = mmposService.CARDTYPE_IC | mmposService.CARDTYPE_MAG;
                cardTest();
            }
        });

        mButtonStatus = (Button) findViewById(R.id.button_status);
        mButtonStatus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getDeviceStatus();
            }
        });

        mButtonScan = (Button) findViewById(R.id.button_scan_mpos);
        mButtonScan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScanMpos();
            }
        });

        mButtonCancel = (Button) findViewById(R.id.button1);
        mButtonCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cancelSwiper();
            }
        });

        mButtonConnect = (Button) findViewById(R.id.button_Connect);
        mButtonConnect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectAgain = CONECT_RETRY_COUNT;
                connectBT();
            }
        });

        mButtonDisonnect = (Button) findViewById(R.id.button_Disconnect);
        mButtonDisonnect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                disconnectBT();
            }
        });

        mButtonClear = (Button) findViewById(R.id.button2);
        mButtonClear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mArrayAdapterHost.clear();
            }
        });

        mButtonNFC = (Button) findViewById(R.id.button3);
        mButtonNFC.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cardType = mmposService.CARDTYPE_NFC;
                cardTest();
            }
        });

        mButtonFW = (Button) findViewById(R.id.button4);
        mButtonFW.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String fw = mmposService.getFirmwareVer();
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "固件版本为： " + fw).sendToTarget();
            }
        });

        mButtonSDK = (Button) findViewById(R.id.button5);
        mButtonSDK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String sdk = mmposService.getSDKVer();
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "SDK版本为： " + sdk).sendToTarget();
            }
        });

        swiper = new transactListener();

        mmposService = new TransactionProcess(mcontext, swiper);

        mConnectAgain = CONECT_RETRY_COUNT;

        connectBT();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mmposService != null) {
//            mmposService.disconnectBT();
//        }
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    public void readNFC(){

        cancelSwiper();

        if(mButtonNFC!=null){
            mButtonNFC.performClick();
        }
    }

    class transactListener implements TransactionListener {

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onBluetoothBounding()
         */
        @Override
        public void onBluetoothBounding() {
            // TODO Auto-generated method stub
            mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                    "Bluetooth Bounding ").sendToTarget();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"连接中",Toast.LENGTH_SHORT).show();
                    tvStatusBT.setText("连接中");
                }
            });

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onBluetoothConnected()
         */
        @Override
        public void onBluetoothConnected() {
            // TODO Auto-generated method stub
            mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                    "Bluetooth Connected ").sendToTarget();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                    tvStatusBT.setText("连接成功");
                }
            });
            mHandler.obtainMessage(ENABLE_BUTTON, ENABLE_CONNECT_BUTTON, 0, 0).sendToTarget();
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onBluetoothConnectFail()
         */
        @Override
        public void onBluetoothConnectFail() {
            // TODO Auto-generated method stub
            mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                    "Bluetooth ConnectFail ").sendToTarget();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"未连接",Toast.LENGTH_SHORT).show();
                    tvStatusBT.setText("未连接");
                }
            });
            mHandler.obtainMessage(ENABLE_BUTTON, ENABLE_CONNECT_BUTTON, 0, 0).sendToTarget();
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onWaitingForCardSwipe()
         */
        @Override
        public void onWaitingForCardSwipe() {
            // TODO Auto-generated method stub

            if (cardType == mmposService.CARDTYPE_NFC) {
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "请放非接卡").sendToTarget();
            } else {
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "请插卡或刷卡").sendToTarget();
            }

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onCheckCardCompleted(int, int, java.lang.String, java.lang.String, java.util.HashMap)
         */
        @Override
        public void onCheckCardCompleted(int cardType, int status, String PAN, String track2, HashMap additonal) {
            // TODO Auto-generated method stub
            if (status != 0) {
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "检卡失败!,请重新执行.返回: " + String.format("%02X", status)).sendToTarget();
                return;
            }

            byte[] resp = new byte[512];
            String uuid = "";

            if (cardType == mmposService.CARDTYPE_NFC) {
                if (additonal != null) {
                    uuid = (String) additonal.get("UUID");
                    mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                            "UUID: " + uuid).sendToTarget();

                    Log.d("RAMBO: uuid = " , uuid);
                    if(CashDeskActivity.mInstance!=null){
                        CashDeskActivity.mInstance.setUUID(uuid);
                    }
                }
                try {
                    Thread.sleep(100);
                    byte[] APDU = new byte[]{0x00,(byte)0xA4,0x04,0x00,0x0E,(byte)0x31,(byte)0x50,(byte)0x41,
                            (byte)0x59,(byte)0x2E,(byte)0x53,(byte)0x59,(byte)0x53,(byte)0x2E,(byte)0x44,(byte)0x44,
                            (byte)0x46,(byte)0x30,(byte)0x31};
                    int sl = mmposService.PCDSendRecv(APDU, APDU.length, resp);
                    if((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "IC选择1PAY失败." + resp[0] + " : " + String.valueOf(sl)).sendToTarget();
                        return;
                    }

                    Thread.sleep(100);
//             byte[] APDU1 = new byte[]{0x00,(byte)0xA4,0x04,0x00,0x07,(byte)0xA0,0x00,0x00,0x03,0x33,0x01,0x01};
                    byte[] APDU1 = new byte[]{0x00,(byte)0xA4,0x04,0x00,0x09,(byte)0xA0,0x00,0x00,0x00,0x03,(byte)0x86,(byte)0x98,(byte)0x07,0x01};
                    sl = mmposService.PCDSendRecv(APDU1, APDU1.length, resp);
                    if((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "选择应用失败." + resp[0]).sendToTarget();
                        return;
                    }

                    Thread.sleep(100);
//             byte[] APDU2 = new byte[]{0x00,(byte)0xB2,0x01,0x0C,0x00};
                    byte[] APDU2 = new byte[]{0x00,(byte)0xB0,(byte)0x95,0x00,0x1E};
                    sl = mmposService.PCDSendRecv(APDU2, APDU2.length, resp);
                    if((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "IC读取文件失败." + resp[0]).sendToTarget();
                        return;
                    }
                    String str = "";
                    for(int i = 1; i < sl; i++) {
                        str = str + String.format("%02X", resp[i]);
                    }
                    mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                            " 发卡信息文件: " + str).sendToTarget();

                    if(CashDeskActivity.mInstance!=null){
                        CashDeskActivity.mInstance.setCardID(str);
                    }

                    Log.d("RAMBO: 发卡信息文件 = " , str);
                }
                catch (Exception e) {

                }
            } else if (cardType == mmposService.CARDTYPE_IC) {
                String atr = "";
                if (additonal != null) {
                    atr = (String) additonal.get("ATR");
                    if (atr != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "ATR: " + atr).sendToTarget();
                    }
                }
                try {
                    Thread.sleep(100);
                    byte[] APDU = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, (byte) 0x31, (byte) 0x50, (byte) 0x41,
                            (byte) 0x59, (byte) 0x2E, (byte) 0x53, (byte) 0x59, (byte) 0x53, (byte) 0x2E, (byte) 0x44, (byte) 0x44,
                            (byte) 0x46, (byte) 0x30, (byte) 0x31};
                    int sl = mmposService.ICCSendRecv(APDU, APDU.length, resp);
                    if ((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "IC选择1PAY失败." + resp[0] + " : " + String.valueOf(sl)).sendToTarget();
                        return;
                    }

                    Thread.sleep(100);
                    byte[] APDU1 = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01};
                    sl = mmposService.ICCSendRecv(APDU1, APDU1.length, resp);
                    if ((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "IC选择应用失败." + String.valueOf(resp[0]) + " : " + String.valueOf(sl)).sendToTarget();
                        return;
                    }

                    Thread.sleep(100);
                    byte[] APDU2 = new byte[]{0x00, (byte) 0xB2, 0x01, 0x0C, 0x00};
                    sl = mmposService.ICCSendRecv(APDU2, APDU2.length, resp);
                    if ((resp[0] != 0x00) && (sl > 0)) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "IC读取文件失败." + resp[0]).sendToTarget();
                        return;
                    }
                    String str = "";
                    String str1 = "";
                    String str2 = "";
                    for (int i = 1; i < sl; i++) {
                        str = str + String.format("%02X", resp[i]);
                    }
                    if (str.length() > 3) {
                        Map<String, TLV> tags = TLVUtils.builderTlvMap(str.substring(4));
                        TLV tag57 = tags.get("57");
                        if (tag57 != null) {
                            String val = tag57.getValue();
                            if (val.indexOf("D") > 0) {
                                str = val.substring(0, val.indexOf("D"));
                                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                        " PAN: " + str).sendToTarget();
                                str1 = val.substring(val.indexOf("D") + 1, val.indexOf("D") + 5);
                                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                        " Expiry: " + str1).sendToTarget();
                                str2 = val.substring(val.indexOf("D") + 5, val.indexOf("D") + 8);
                                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                        " S.C: " + str2).sendToTarget();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (cardType == mmposService.CARDTYPE_MAG) {
            }

            String track1 = "";
            String track3 = "";
            String countryCode = "";
            String cardholder = "";
            String expireDate = "";
            boolean isICCard;

            if (status == 0) {
                if (additonal != null) {
                    track1 = (String) additonal.get("TRACK1");
                    if (track1 != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "TRACK1: " + track1).sendToTarget();
                    }
                    track3 = (String) additonal.get("TRACK3");
                    if (track3 != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "TRACK3: " + track3).sendToTarget();
                    }
                    countryCode = (String) additonal.get("COUNTRYCODE");
                    if (countryCode != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "COUNTRYCODE: " + countryCode).sendToTarget();
                    }
                    cardholder = (String) additonal.get("CARDHOLDER");
                    if (cardholder != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "CARDHOLDER: " + cardholder).sendToTarget();
                    }

                    expireDate = (String) additonal.get("EXPIRE");
                    if (expireDate != null) {
                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "EXPIRE DATE: " + expireDate).sendToTarget();
                    }

                    Object temp = additonal.get("isICCARD");
                    if (temp != null) {
                        isICCard = (Boolean) additonal.get("isICCARD");

                        mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                                "isICCARD: " + isICCard).sendToTarget();
                    }
                } else {
                    mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                            "No additional data").sendToTarget();
                }

                if (PAN != null) {
                    mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                            "PAN: " + PAN).sendToTarget();
                }
                if (track2 != null) {
                    mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                            "TRACK2: " + track2).sendToTarget();
                }
            } else {
                mHandler.obtainMessage(MESSAGE_SWIPER, 0, 0,
                        "check Card error: " + String.format("%02X", status)).sendToTarget();
            }
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onTimeout()
         */
        @Override
        public void onTimeout() {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onUpdateProcess(int)
         */
        @Override
        public void onUpdateProcess(int percent) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onInputPIN(byte[])
         */
        @Override
        public int onInputPIN(byte[] pin) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onGetAllAID(byte[])
         */
        @Override
        public int onGetAllAID(byte[] aid) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onGetRID(byte[], int, int, byte[], byte[])
         */
        @Override
        public int onGetRID(byte[] rid, int ridLen, int pki, byte[] Mod, byte[] Exp) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onGetTerminalTag(java.lang.String, byte[])
         */
        @Override
        public int onGetTerminalTag(String reserve, byte[] tag) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onGetOfflineTotalAmount()
         */
        @Override
        public int onGetOfflineTotalAmount() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onAppSelect(int, java.lang.String[])
         */
        @Override
        public int onAppSelect(int total, String[] appName) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#HostSendRecv(byte[], int, byte[], java.lang.String)
         */
        @Override
        public int HostSendRecv(byte[] req, int len, byte[] resp, String reserve) {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onTransactResult(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
         */
        @Override
        public void onTransactResult(int res, int status, String TVR, String TSI, String CVM, String[] Script) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onDebug(java.lang.String)
         */
        @Override
        public void onDebug(String info) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see com.walktech.template.impl.TransactionListener#onCheckCRL(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public int onCheckCRL(String RID, String Index, String SN) {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    public void cancelSwiper() {
        try {
            mmposService.abortCheckCard();
        } catch (Exception e) {


        }

    }

    private void cardTest() {
        try {
            byte[] appendData = new byte[16];
            mmposService.checkCard(cardType,
                    mmposService.GENERAL_READER_DEVICE,
                    appendData, appendData.length,
                    30);
        } catch (Exception e) {


        }
    }


    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private String byteToChar(byte c) {
        return "0123456789ABCDEF".substring(c, c + 1);
    }

    public String bcdToString(byte[] bcd, int bcdLen) {

        String res = "";

        for (int i = 0; i < bcdLen; i++) {
            byte num = (byte) (bcd[i] & 0xFF);
            String temp;

            num = (byte) (num >> 4);

            num = (byte) (num & 0x0F);

            temp = byteToChar(num);

            res += temp;

            num = (byte) ((byte) (bcd[i] & 0xFF) & 0x0F);

            temp = byteToChar(num);

            res += temp;
        }


        return res;
    }

    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    private void connectBT() {

        Properties prop = loadConfig(this, mCurentPath + "/" + mConfigFile);

        String address = (String) prop.get("MAC");
        String name = (String) prop.get("Name");

        if (address == null) {
            mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                    "Haven't connected to mpos, press ScanMpos to scan and connect mpos").sendToTarget();
            if (D) Log.e(TAG, "-- Config file is null --");
            return;
        }

        mButtonConnect.setEnabled(false);

        //disable scan
        mButtonScan.setEnabled(false);

        mmposService.connectBluetoothDevice(30, address);

        mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                "Connect BT Device: name=" + name + " MAC=" + address).sendToTarget();

//        Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
//        tvStatusBT.setText("连接成功");
    }

    private void disconnectBT() {

        mConnectAgain = 0;

        mmposService.disconnectBT();

        mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                "Disonnect BT").sendToTarget();

    }

    private void getDeviceStatus() {

        boolean status = mmposService.isDevicePresent();

        if (status == true) {
            mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                    "Device connected").sendToTarget();
//            Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
            tvStatusBT.setText("连接成功");
        } else {
            mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                    "Device did not connectd, press Scan MPOS to connect device").sendToTarget();
//            Toast.makeText(MainActivity.this,"未连接",Toast.LENGTH_SHORT).show();
            tvStatusBT.setText("未连接");
        }
    }

    public String bytes2HexString(byte[] b, int length) {
        String ret = "";
        for (int i = 0; i < length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MESSAGE_HOST:
                    mArrayAdapterHost.add((String) msg.obj);
                    break;

                case MESSAGE_SWIPER:
                    mArrayAdapterHost.add((String) msg.obj);
                    break;

                case ENABLE_BUTTON:
                    int button = msg.arg1;
                    if (button == ENABLE_TRANS_BUTTON) {
                    } else if (button == ENABLE_CONNECT_BUTTON) {
                        mButtonConnect.setEnabled(true);
                        mButtonScan.setEnabled(true);
                    } else if (button == ENABLE_CARD_BUTTON) {
                        mButtonCard.setEnabled(true);
                    } else if (button == ENABLE_NFC_BUTTON) {
                        mButtonNFC.setEnabled(true);
                    }
                    break;
            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.e(TAG, "onActivityResult requestCode: " + requestCode + " resultCode : " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {

                    mButtonConnect.setEnabled(false);
                    mButtonScan.setEnabled(false);

                    connectDevice(data);
                }
                break;

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "BT is enabled");
                    setup();
                } else {
                    Log.d(TAG, "BT is not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    private void connectDevice(Intent data) {
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        if (mmposService == null) {
            Log.e(TAG, " + mBluetoothService NULL + ");
            return;
        }

        mmposService.connectBluetoothDevice(60, address);

        mConnectedDeviceName = device.getName();
        mConnectedDeviceMAC = address;

        Properties prop = new Properties();
        prop.put("MAC", mConnectedDeviceMAC);
        saveConfig(this, mCurentPath + "/" + mConfigFile, prop);

        prop.put("Name", mConnectedDeviceName);
        saveConfig(this, mCurentPath + "/" + mConfigFile, prop);

        mHandler.obtainMessage(MESSAGE_HOST, 0, 0,
                "Select BT Device: Name=" + mConnectedDeviceName + " MAC=" + mConnectedDeviceMAC).sendToTarget();
    }

    private void ScanMpos() {
        Intent serverIntent = null;
        serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
}
