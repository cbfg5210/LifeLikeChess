package com.ue.chess_life.entity;

import java.util.List;

/**
 * 一组表情所对应的实体类
 *
 */
public class EaseEmojiGroup {
    /**
     * 表情数据
     */
    private List<EaseEmoji> emojiconList;
    /**
     * 图片
     */
    private int icon;
    /**
     * 组名
     */
    private String name;
    /**
     * 表情类型
     */
    private EaseEmoji.Type type;
    
    public EaseEmojiGroup(){}
    
    public EaseEmojiGroup(int icon, List<EaseEmoji> emojiconList){
        this.icon = icon;
        this.emojiconList = emojiconList;
        type = EaseEmoji.Type.NORMAL;
    }
    
    public EaseEmojiGroup(int icon, List<EaseEmoji> emojiconList, EaseEmoji.Type type){
        this.icon = icon;
        this.emojiconList = emojiconList;
        this.type = type;
    }
    
    public List<EaseEmoji> getEmojiconList() {
        return emojiconList;
    }
    public void setEmojiconList(List<EaseEmoji> emojiconList) {
        this.emojiconList = emojiconList;
    }
    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public EaseEmoji.Type getType() {
        return type;
    }

    public void setType(EaseEmoji.Type type) {
        this.type = type;
    }
    
    
}
