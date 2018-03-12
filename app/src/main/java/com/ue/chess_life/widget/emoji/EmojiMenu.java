package com.ue.chess_life.widget.emoji;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.ue.chess_life.R;
import com.ue.chess_life.entity.EaseEmoji;
import com.ue.chess_life.entity.EaseEmojiGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Emojicon menu
 */
public class EmojiMenu extends EmojiMenuBase {
	
	private int emojiconColumns;
	private int bigEmojiconColumns;
    private EmojiScrollTabBar tabBar;
    private EmojiIndicatorView indicatorView;
    private EmojiPagerView pagerView;
    
    private List<EaseEmojiGroup> emojiconGroupList = new ArrayList<EaseEmojiGroup>();
	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public EmojiMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public EmojiMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public EmojiMenu(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context context, AttributeSet attrs){
		LayoutInflater.from(context).inflate(R.layout.view_emojicon, this);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EmojiMenu);
        int defaultColumns = 7;
        emojiconColumns = ta.getInt(R.styleable.EmojiMenu_emojiconColumns, defaultColumns);
        int defaultBigColumns = 4;
        bigEmojiconColumns = ta.getInt(R.styleable.EmojiMenu_bigEmojiconRows, defaultBigColumns);
		ta.recycle();
		
		pagerView = (EmojiPagerView) findViewById(R.id.pager_view);
		indicatorView = (EmojiIndicatorView) findViewById(R.id.indicator_view);
		tabBar = (EmojiScrollTabBar) findViewById(R.id.tab_bar);
		
	}
	
	public void init(List<EaseEmojiGroup> groupEntities){
	    if(groupEntities == null || groupEntities.size() == 0){
	        return;
	    }
	    for(EaseEmojiGroup groupEntity : groupEntities){
	        emojiconGroupList.add(groupEntity);
	        tabBar.addTab(groupEntity.getIcon());
	    }
	    
	    pagerView.setPagerViewListener(new EmojiconPagerViewListener());
        pagerView.init(emojiconGroupList, emojiconColumns,bigEmojiconColumns);
        
        tabBar.setTabBarItemClickListener(new EmojiScrollTabBar.EaseScrollTabBarItemClickListener() {
            
            @Override
            public void onItemClick(int position) {
                pagerView.setGroupPostion(position);
            }
        });
	    
	}
	
	
	/**
     * add emojicon group
     * @param groupEntity
     */
    public void addEmojiconGroup(EaseEmojiGroup groupEntity){
        emojiconGroupList.add(groupEntity);
        pagerView.addEmojiconGroup(groupEntity, true);
        tabBar.addTab(groupEntity.getIcon());
    }
    
    /**
     * add emojicon group list
     * @param groupEntitieList
     */
    public void addEmojiconGroup(List<EaseEmojiGroup> groupEntitieList){
        for(int i= 0; i < groupEntitieList.size(); i++){
            EaseEmojiGroup groupEntity = groupEntitieList.get(i);
            emojiconGroupList.add(groupEntity);
            pagerView.addEmojiconGroup(groupEntity, i == groupEntitieList.size()-1 ? true : false);
            tabBar.addTab(groupEntity.getIcon());
        }
        
    }
    
    /**
     * remove emojicon group
     * @param position
     */
    public void removeEmojiconGroup(int position){
        emojiconGroupList.remove(position);
        pagerView.removeEmojiconGroup(position);
        tabBar.removeTab(position);
    }
    
    public void setTabBarVisibility(boolean isVisible){
        if(!isVisible){
            tabBar.setVisibility(View.GONE);
        }else{
            tabBar.setVisibility(View.VISIBLE);
        }
    }
	
	
	private class EmojiconPagerViewListener implements EmojiPagerView.EaseEmojiconPagerViewListener {

        @Override
        public void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize) {
            indicatorView.init(groupMaxPageSize);
            indicatorView.updateIndicator(firstGroupPageSize);
            tabBar.selectedTo(0);
        }

        @Override
        public void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup) {
            indicatorView.updateIndicator(pagerSizeOfGroup);
            tabBar.selectedTo(groupPosition);
        }

        @Override
        public void onGroupInnerPagePostionChanged(int oldPosition, int newPosition) {
            indicatorView.selectTo(oldPosition, newPosition);
        }

        @Override
        public void onGroupPagePostionChangedTo(int position) {
            indicatorView.selectTo(position);
        }

        @Override
        public void onGroupMaxPageSizeChanged(int maxCount) {
            indicatorView.updateIndicator(maxCount);
        }

        @Override
        public void onDeleteImageClicked() {
            if(listener != null){
                listener.onDeleteImageClicked();
            }
        }

        @Override
        public void onExpressionClicked(EaseEmoji emojicon) {
            if(listener != null){
                listener.onExpressionClicked(emojicon);
            }
        }
	    
	}
	
}
