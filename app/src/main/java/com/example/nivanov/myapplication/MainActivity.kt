package com.example.nivanov.myapplication

import android.os.Bundle
import android.support.design.widget.Snackbar
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

sealed class ScrollState {
    abstract fun nextState(nextScroll : Int, layoutManager: LinearLayoutManager ) : ScrollState
    abstract fun shouldScroll() : Boolean
}

class Init : ScrollState() {
    override fun nextState(nextScroll: Int, layoutManager: LinearLayoutManager): ScrollState = this
    override fun shouldScroll(): Boolean = true
}
class MainActivity : AppCompatActivity() {

    lateinit var list: RecyclerView
    lateinit var repository: Repository
    var scrollState : ScrollState = Init()
    var lastList: List<ViewModel> = emptyList()
    val zipper: BiFunction<List<ViewModel>, DiffUtil.DiffResult, ViewModelUpdate> = BiFunction { p0, p1 ->
        Pair(p0, p1)
    }

    fun log(msg : () -> String) {
        Log.i("MainActivity",msg())
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
        val linearLayoutManager = object : LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false){
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
        list.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                log{"onScrollStateChanged[newState=$newState]"}
            }
        })
        repository = Repository()
        val payload: Observable<ViewModelUpdate> = repository.viewModel
                .subscribeOn(Schedulers.computation())
                .map {
                    log { "Calcing diff on ${Thread.currentThread().name}"}
                    val calculateDiff: DiffUtil.DiffResult = DiffUtil.calculateDiff(DiffCallback(lastList, it))
                    Pair(it, calculateDiff)
                }.doAfterNext {
            val oldSize = lastList.size
            lastList = it.first
            val lastPosition = lastList.size
            list.post{
                log{"scroll to position $lastPosition"}
                list.smoothScrollToPosition(lastPosition)
            }
        }
        val adapter = ElementsAdapter(layoutInflater,payload)
        list.adapter = adapter
    }
}
