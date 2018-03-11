package com.github.ymkawb.anchored_list

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

enum class ScrollState {
    Init {
        override fun <T : RecyclerView.ViewHolder> nextState(nextScroll: Int, layoutManager: LinearLayoutManager, adapter: RecyclerView.Adapter<T>): ScrollState = when (nextScroll) {
            RecyclerView.SCROLL_STATE_DRAGGING -> Dragging
            else -> this
        }

        override fun shouldScroll(layoutManager: LinearLayoutManager): Boolean = true
    },
    Dragging {
        override fun <T : RecyclerView.ViewHolder> nextState(nextScroll: Int, layoutManager: LinearLayoutManager, adapter: RecyclerView.Adapter<T>): ScrollState = when (nextScroll) {
            RecyclerView.SCROLL_STATE_SETTLING -> DraggingSettling
            RecyclerView.SCROLL_STATE_IDLE ->
                if (layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1) Init else ScrollLocked
            else -> this
        }

        override fun shouldScroll(layoutManager: LinearLayoutManager): Boolean = false
    },
    ScrollLocked {
        override fun <T : RecyclerView.ViewHolder> nextState(nextScroll: Int, layoutManager: LinearLayoutManager, adapter: RecyclerView.Adapter<T>): ScrollState = when (nextScroll) {
            RecyclerView.SCROLL_STATE_DRAGGING -> Dragging
            else -> this
        }

        override fun shouldScroll(layoutManager: LinearLayoutManager): Boolean = false
    },
    DraggingSettling {
        override fun shouldScroll(layoutManager: LinearLayoutManager): Boolean = false
        override fun <T : RecyclerView.ViewHolder> nextState(nextScroll: Int, layoutManager: LinearLayoutManager, adapter: RecyclerView.Adapter<T>): ScrollState = when (nextScroll) {
            RecyclerView.SCROLL_STATE_IDLE ->
                if (layoutManager.findLastVisibleItemPosition() == adapter.itemCount - 1) Init else ScrollLocked
            RecyclerView.SCROLL_STATE_DRAGGING -> Dragging
            else -> this
        }
    };


    abstract fun <T : RecyclerView.ViewHolder> nextState(nextScroll: Int, layoutManager: LinearLayoutManager, adapter: RecyclerView.Adapter<T>): ScrollState
    abstract fun shouldScroll(layoutManager: LinearLayoutManager): Boolean;

}