package com.ue.chess_life.event;

import com.ue.resource.model.AppUser;

/**
 * Created by hawk on 2017/6/18.
 */

public class ChangePlayerEvent {
    public AppUser newPlayer;

    public ChangePlayerEvent(AppUser newPlayer) {
        this.newPlayer = newPlayer;
    }
}
