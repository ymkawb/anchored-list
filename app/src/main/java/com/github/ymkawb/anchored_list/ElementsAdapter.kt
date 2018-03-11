package com.github.ymkawb.anchored_list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


class ElementsAdapter(private val inflater : LayoutInflater,
                      private val viewModelObserver : Observable<ViewModelUpdate>) : RecyclerView.Adapter<ElementViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var currentData : List<ViewModel> = emptyList()
    private var subscription : Disposable? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ElementViewHolder =
            ElementViewHolder(inflater.inflate(R.layout.item, parent, false))


    override fun getItemCount(): Int = currentData.size

    override fun onBindViewHolder(holder: ElementViewHolder?, position: Int) {
        holder?.bind(currentData[position])
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        subscription?.dispose()
        subscription = null
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        subscription = viewModelObserver.observeOn(AndroidSchedulers.mainThread()).subscribe {
            currentData = it.first
            it.second.dispatchUpdatesTo(this)
        }
    }

    override fun getItemId(position: Int): Long = currentData[position].id
}