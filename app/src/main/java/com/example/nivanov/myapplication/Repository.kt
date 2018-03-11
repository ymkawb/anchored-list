package com.example.nivanov.myapplication

import android.util.Log
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by nivanov on 10.03.2018.
 */
class Repository {
    private val lastModel: MutableList<ViewModel> = mutableListOf()
    private var id: Long = 0
    private var sizeIncreaseGen: Random = Random()
    val viewModel: Observable<List<ViewModel>> =
            Observable.generate { t ->
                val sleep = sizeIncreaseGen.nextInt(10) * 100L
                Log.i("Repository","Sleeping at ${Thread.currentThread().name} for $sleep ms")
                TimeUnit.MILLISECONDS.sleep((sleep))
                val size = sizeIncreaseGen.nextInt(5)
                Log.i("Repository", "increasing size by $size")
                for (i in 0..size) {
                    lastModel.add(ViewModel("title at ${id}", "Body at ${id}", id++))
                }
                if(lastModel.size < 200)
                    t?.onNext(lastModel.toList())
                else
                    t?.onComplete()
            }
}