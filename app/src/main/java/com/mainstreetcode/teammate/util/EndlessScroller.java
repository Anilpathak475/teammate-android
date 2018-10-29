package com.mainstreetcode.teammate.util;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class EndlessScroller extends RecyclerView.OnScrollListener {

    private static final int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.

    private final boolean isReverse;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.

    private RecyclerView.LayoutManager layoutManager;

    EndlessScroller(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        isReverse = isReverse();
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (Math.abs(dy) < 3) return;

        int visibleItemCount = recyclerView.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = getFirstVisiblePosition();

        int numScrolled = totalItemCount - visibleItemCount;
        int refreshTrigger = isReverse ? totalItemCount - firstVisibleItem : firstVisibleItem;
        refreshTrigger += visibleThreshold;

        if (loading && totalItemCount > previousTotal) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && numScrolled <= refreshTrigger) {
            loading = true;
            onLoadMore(totalItemCount);
        }
    }

    void reset() {loading = false;}

    public abstract void onLoadMore(int currentItemCount);

    private boolean isReverse() {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getStackFromEnd();
        }
        else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getReverseLayout();
        }
        return false;
    }

    private int getFirstVisiblePosition() {
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager gridLayoutManager = ((StaggeredGridLayoutManager) layoutManager);
            int[] store = new int[gridLayoutManager.getSpanCount()];
            gridLayoutManager.findFirstVisibleItemPositions(store);
            List<Integer> list = new ArrayList<>(store.length);
            for (int value : store) list.add(value);
            return Collections.min(list);
        }
        return 0;
    }
}
