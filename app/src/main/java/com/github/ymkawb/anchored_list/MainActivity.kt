package com.github.ymkawb.anchored_list

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
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var repository: Repository
    private var scrollState: ScrollState = ScrollState.Init
    private var lastList: List<ViewModel> = emptyList()

    fun log(msg: () -> String) {
        Log.i("MainActivity", msg())
    }

    companion object {
        const val MILLISECONDS_PER_INCH = 250f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val lastPosition = list.adapter.itemCount
            list.smoothScrollToPosition(lastPosition)
        }
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
