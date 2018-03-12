package com.ue.chess_life;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.ue.chess_life.constant.ActionFlags;
import com.ue.chess_life.constant.EaseConstants;
import com.ue.chess_life.event.EMMessageEventAdapter;
import com.ue.chess_life.event.InvitedEvent;
import com.ue.chess_life.feature.login.LoginActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class EaseDelegate {
    private static EaseDelegate instance;

    private Context appContext;
    //init flag: test if the sdk has been inited before, we don't need to init again
    private boolean sdkInited;
    private long enterTime;

    private EMConnectionListener connectionListener;
    private EMMessageListener messageListener;

    private EaseDelegate() {
    }

    public static EaseDelegate getInstance() {
        if (instance == null) {
            synchronized (EaseDelegate.class) {
                if (instance == null) {
                    instance = new EaseDelegate();
                }
            }
        }
        return instance;
    }

    /**
     * init helper
     *
     * @param context application mContext
     */
    public synchronized void init(Context context) {
        if (sdkInited) {
            return;
        }
        appContext = context;
        EMOptions options = initChatOptions();
        EMClient.getInstance().init(context, options);

        sdkInited = true;
        //debug mode, you'd better set it to false, if you want release your App officially.
        EMClient.getInstance().setDebugMode(false);
        setGlobalListeners();
    }


    private EMOptions initChatOptions() {
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        options.setRequireAck(true);
        options.setRequireDeliveryAck(false);

        //you need apply & set your own id if you want to use google cloud messaging.
//        options.setGCMNumber("324169311137");
        //you need apply & set your own id if you want to use Mi push notification
//        options.setMipushConfig("2882303761517426801", "5381742660801");
        //you need apply & set your own id if you want to use Huawei push notification
//        options.setHuaweiPushAppId("10492024");

        options.allowChatroomOwnerLeave(true);
        options.setDeleteMessagesAsExitGroup(true);
        options.setAutoAcceptGroupInvitation(true);

        return options;
    }

    /**
     * set global listener
     */
    private void setGlobalListeners() {
        // create the global connection listener
        connectionListener = new EMConnectionListener() {
            @Override
            public void onDisconnected(int error) {
                Log.e("global listener", "onDisconnect" + error);
                if (error == EMError.USER_REMOVED) {
                    onUserException(EaseConstants.ACCOUNT_REMOVED);
                    return;
                }
                if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onUserException(EaseConstants.ACCOUNT_CONFLICT);
                    return;
                }
                if (error == EMError.SERVER_SERVICE_RESTRICTED) {
                    onUserException(EaseConstants.ACCOUNT_FORBIDDEN);
                    return;
                }
            }

            @Override
            public void onConnected() {
            }
        };

        enterTime = System.currentTimeMillis();
        messageListener = new EMMessageEventAdapter() {
            @Override
            public void onCmdMessageReceived(List<EMMessage> list) {
                for (EMMessage emMessage : list) {
                    if (emMessage.getMsgTime() < enterTime) {
                        //过滤掉以前的透传消息
                        continue;
                    }
                    if (emMessage.getFrom().equals(EMClient.getInstance().getCurrentUser())) {
                        //过滤掉自己发出的消息
                        continue;
                    }

                    EMCmdMessageBody cmdMessageBody = (EMCmdMessageBody) emMessage.getBody();
                    String action = cmdMessageBody.action();

                    if (TextUtils.isEmpty(action)) {
                        continue;
                    }
                    String inviteFlag = "" + ActionFlags.INVITE;
                    if (!action.equals(inviteFlag)) {
                        continue;
                    }
                    EventBus.getDefault().post(new InvitedEvent(emMessage));
                    //只处理一个符合条件的消息
                    break;
                }
            }
        };

        EMClient.getInstance().addConnectionListener(connectionListener);
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    /**
     * user met some exception: conflict, removed or forbidden
     */
    private void onUserException(final String exception) {
        Log.e("EaseDelegate", "onUserException: " + exception);
        //@param unbindDeviceToken whether you need unbind your device token
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                Intent intent = new Intent(appContext, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(exception, true);
                intent.putExtra(EaseConstants.IS_CONFLICT, true);
                appContext.startActivity(intent);
            }

            @Override
            public void onError(int i, String s) {
            }

            @Override
            public void onProgress(int i, String s) {
            }
        });
    }
}
