package com.github.ymkawb.anchored_list

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
                TimeUnit.MILLISECONDS.sleep((sleep))
                val size = sizeIncreaseGen.nextInt(5)
                for (i in 0..size) {
                    val nextId = id++
                    lastModel.add(ViewModel(title = "title at ${id}",
                            body = "Body at ${id}",
                            id = nextId,
                            imageUri = "https://placeimg.com/640/480/people/$nextId"
                    ))
                }
                if (lastModel.size < 200)
                    t?.onNext(lastModel.toList())
                else
                    t?.onComplete()
            }
}