package com.ue.chess_life.event;

import com.hyphenate.chat.EMMessage;

/**
 * Created by hawk on 2017/11/10.
 */

public class InvitedEvent {
    public EMMessage invitedMsg;

    public InvitedEvent(EMMessage invitedMsg) {
        this.invitedMsg = invitedMsg;
    }
}
