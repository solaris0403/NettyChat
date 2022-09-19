package com.example.nettychat.im;


import com.example.im_lib.IMSClientFactory;
import com.example.im_lib.IMSClientInterface;
import com.example.im_lib.MessageProtobuf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMSClientBootstrap.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     应用层的imsClient启动器</p>
 * </b>
 * <p>@author:          FreddyChen</p>
 * <p>@date:            2019/04/08 00:25</p>
 * <p>@email:           chenshichao@outlook.com</p>
 */
public class IMSClientBootstrap {

    private static final IMSClientBootstrap INSTANCE = new IMSClientBootstrap();
    private IMSClientInterface imsClient;
    private boolean isActive;

    private IMSClientBootstrap() {

    }

    public static IMSClientBootstrap getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        String userId = "100001";
        String token = "token_" + userId;
        IMSClientBootstrap bootstrap = IMSClientBootstrap.getInstance();
        String hosts = "[{\"host\":\"127.0.0.1\", \"port\":9877}]";
        bootstrap.init(userId, token, hosts, 0);
    }

    public synchronized void init(String userId, String token, String hosts, int appStatus) {
        if (!isActive()) {
            List<String> serverUrlList = convertHosts(hosts);
            if (serverUrlList == null || serverUrlList.size() == 0) {
                System.out.println("init IMLibClientBootstrap error,ims hosts is null");
                return;
            }

            isActive = true;
            System.out.println("init IMLibClientBootstrap, servers=" + hosts);
            if (null != imsClient) {
                imsClient.close();
            }
            imsClient = IMSClientFactory.getIMSClient();
            updateAppStatus(appStatus);
            imsClient.init(serverUrlList, new IMSEventListener(userId, token), new IMSConnectStatusListener());
        }
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    public void sendMessage(MessageProtobuf.Msg msg) {
        if (isActive) {
            imsClient.sendMsg(msg);
        }
    }

    private List<String> convertHosts(String hosts) {
        List<String> serverUrlList = new ArrayList<>();
        if (hosts != null && hosts.length() > 0) {
            try {
                JSONArray hostArray = new JSONArray(hosts);
                for (int i = 0; i < hostArray.length(); i++) {
                    JSONObject host = new JSONObject(hostArray.get(i).toString());
                    serverUrlList.add(host.optString("host") + " " + host.optString("port"));
                }
                return serverUrlList;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return serverUrlList;
    }

    public void updateAppStatus(int appStatus) {
        if (imsClient == null) {
            return;
        }

        imsClient.setAppStatus(appStatus);
    }
}
