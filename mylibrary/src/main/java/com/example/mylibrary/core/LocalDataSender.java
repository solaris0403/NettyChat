package com.example.mylibrary.core;

import android.util.Log;

import com.example.mylibrary.utils.MBObserver;

import net.x52im.mobileimsdk.server.protocal.ErrorCode;
import net.x52im.mobileimsdk.server.protocal.c.PLoginInfo;

public class LocalDataSender {
    private final static String TAG = LocalDataSender.class.getSimpleName();
    private static LocalDataSender instance = null;

    public static LocalDataSender getInstance() {
        if (instance == null)
            instance = new LocalDataSender();
        return instance;
    }

    private LocalDataSender() {
    }

    int sendLogin(PLoginInfo loginInfo) {

        int codeForCheck = this.checkBeforeSend();
        if (codeForCheck != ErrorCode.COMMON_CODE_OK)
            return codeForCheck;

        if (!LocalSocketProvider.getInstance().isLocalSocketReady()) {

            if (ClientCoreSDK.DEBUG)
                Log.d(TAG, "【IMCORE-TCP】发送登陆指令时，socket连接未就绪，首先开始尝试发起连接（登陆指令将在连接成功后的回调中自动发出）。。。。");

            MBObserver connectionDoneObserver = (sucess, extraObj) -> {
                if (sucess)
                    sendLoginImpl(loginInfo);
                else
                    Log.w(TAG, "【IMCORE-TCP】[来自Netty的连接结果回调观察者通知]socket连接失败，本次登陆信息未成功发出！");
            };
            LocalSocketProvider.getInstance().setConnectionDoneObserver(connectionDoneObserver);
            return LocalSocketProvider.getInstance().resetLocalSocket() != null
                    ? ErrorCode.COMMON_CODE_OK : ErrorCode.ForC.BAD_CONNECT_TO_SERVER;
        } else {
            return this.sendLoginImpl(loginInfo);
        }
    }

    int sendLoginImpl(PLoginInfo loginInfo) {
        byte[] b = ProtocalFactory.createPLoginInfo(loginInfo).toBytes();
        int code = send(b, b.length);
        if (code == 0) {
            ClientCoreSDK.getInstance().setCurrentLoginInfo(loginInfo);
        }

        return code;
    }

    public int sendLoginout() {
        int code = ErrorCode.COMMON_CODE_OK;
        if (ClientCoreSDK.getInstance().isLoginHasInit()) {
            byte[] b = ProtocalFactory.createPLoginoutInfo(ClientCoreSDK.getInstance().getCurrentLoginUserId()).toBytes();
            code = send(b, b.length);
            if (code == 0) {
                // do nothing
            }
        }
        ClientCoreSDK.getInstance().release();
        return code;
    }

    int sendKeepAlive() {
        byte[] b = ProtocalFactory.createPKeepAlive(ClientCoreSDK.getInstance().getCurrentLoginUserId()).toBytes();
        return send(b, b.length);
    }

    public int sendCommonData(String dataContentWidthStr, String to_user_id) {
        return sendCommonData(dataContentWidthStr, to_user_id, -1);
    }

    public int sendCommonData(String dataContentWidthStr, String to_user_id, int typeu) {
        return sendCommonData(dataContentWidthStr, to_user_id, null, typeu);
    }

    public int sendCommonData(String dataContentWidthStr, String to_user_id, String fingerPrint, int typeu) {
        return sendCommonData(dataContentWidthStr, to_user_id, true, fingerPrint, typeu);
    }

    public int sendCommonData(String dataContentWidthStr, String to_user_id, boolean QoS, String fingerPrint, int typeu) {
        return sendCommonData(ProtocalFactory.createCommonData(dataContentWidthStr
                , ClientCoreSDK.getInstance().getCurrentLoginUserId(), to_user_id, QoS, fingerPrint, typeu));
    }

    public int sendCommonData(Protocal p) {
        if (p != null) {
            byte[] b = p.toBytes();
            int code = send(b, b.length);
            if (code == 0) {
                if (p.isQoS() && !QoS4SendDaemon.getInstance().exist(p.getFp()))
                    QoS4SendDaemon.getInstance().put(p);
            }
            return code;
        } else
            return ErrorCode.COMMON_INVALID_PROTOCAL;
    }

    private int send(byte[] fullProtocalBytes, int dataLen) {

        int codeForCheck = this.checkBeforeSend();
        if (codeForCheck != ErrorCode.COMMON_CODE_OK)
            return codeForCheck;

        Channel ds = LocalSocketProvider.getInstance().getLocalSocket();
        if (ds != null && ds.isActive()) {// && [ClientCoreSDK sharedInstance].connectedToServer)
            return TCPUtils.send(ds, fullProtocalBytes, dataLen) ? ErrorCode.COMMON_CODE_OK : ErrorCode.COMMON_DATA_SEND_FAILD;
        } else {
            Log.d(TAG, "【IMCORE-TCP】scocket未连接，无法发送，本条将被忽略（dataLen=" + dataLen + "）!");
            return ErrorCode.COMMON_CODE_OK;
        }
    }

    private int checkBeforeSend() {
        if (!ClientCoreSDK.getInstance().isInitialed())
            return ErrorCode.ForC.CLIENT_SDK_NO_INITIALED;
        return ErrorCode.COMMON_CODE_OK;
    }

    //------------------------------------------------------------------------------------------ utilities class

    public static abstract class SendCommonDataAsync extends MBAsyncTask{
        protected Protocal p = null;

        public SendCommonDataAsync(String dataContentWidthStr, String to_user_id) {
            this(dataContentWidthStr, to_user_id, null, -1);
        }

        public SendCommonDataAsync(String dataContentWidthStr, String to_user_id, int typeu) {
            this(dataContentWidthStr, to_user_id, null, typeu);
        }

        public SendCommonDataAsync(String dataContentWidthStr, String to_user_id, String fingerPrint, int typeu) {
            this(ProtocalFactory.createCommonData(dataContentWidthStr
                    , ClientCoreSDK.getInstance().getCurrentLoginUserId(), to_user_id, true, fingerPrint, typeu));
        }

        public SendCommonDataAsync(Protocal p) {
            if (p == null) {
                Log.w(TAG, "【IMCORE-TCP】无效的参数p==null!");
                return;
            }
            this.p = p;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            if (p != null)
                return LocalDataSender.getInstance().sendCommonData(p);//dataContentWidthStr, to_user_id);
            return -1;
        }

        @Override
        protected abstract void onPostExecute(Integer code);
    }

    public static abstract class SendLoginDataAsync extends MBAsyncTask{
        protected PLoginInfo loginInfo = null;

        public SendLoginDataAsync(PLoginInfo loginInfo) {
            this.loginInfo = loginInfo;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            int code = LocalDataSender.getInstance().sendLogin(this.loginInfo);
            return code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if (code == 0) {
//				LocalUDPDataReciever.getInstance().startup();
            } else {
                Log.d(TAG, "【IMCORE-TCP】数据发送失败, 错误码是：" + code + "！");
            }

            fireAfterSendLogin(code);
        }

        protected void fireAfterSendLogin(int code) {
            // default do nothing
        }
    }
}
