package com.example.nivanov.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_main.*
import java.util.function.BiFunction

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

};


class MainActivity : AppCompatActivity() {

    lateinit var list: RecyclerView
    lateinit var repository: Repository
    var scrollState: ScrollState = ScrollState.Init
    var lastList: List<ViewModel> = emptyList()

    fun log(msg: () -> String) {
        Log.i("MainActivity", msg())
    }


    private val MILLISECONDS_PER_INCH = 250f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val lastPosition = list.adapter.itemCount
            list.smoothScrollToPosition(lastPosition)
        }
        list = findViewById(R.id.list)
        val scroller = object : LinearSmoothScroller(this) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return MILLISECONDS_PER_INCH / displayMetrics!!.densityDpi;
            }
        }
        val linearLayoutManager = object : LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
                scroller.targetPosition = position
                startSmoothScroll(scroller)
            }
        }

        list.layoutManager = linearLayoutManager
        list.isScrollContainer = true
        list.itemAnimator = object : DefaultItemAnimator() {
            override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean = false
        }

        repository = Repository()
        val payload: Observable<ViewModelUpdate> = repository.viewModel
                .subscribeOn(Schedulers.computation())
                .map { Pair(it, DiffUtil.calculateDiff(DiffCallback(lastList, it))) }
                .doAfterNext {
                    lastList = it.first
                    val lastPosition = lastList.size
                    list.post {
                        if (scrollState.shouldScroll(linearLayoutManager)) {
                            log { "scroll to position $lastPosition" }
                            list.smoothScrollToPosition(lastPosition)
                        }
                    }
                }
        val adapter = ElementsAdapter(layoutInflater, payload)
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                val oldState = scrollState
                scrollState = scrollState.nextState(newState, linearLayoutManager, adapter)
                log { "onScrollStateChanged[newState=$newState, scrollState=$oldState -> $scrollState]" }
            }
        })
        list.adapter = adapter

    }
}
