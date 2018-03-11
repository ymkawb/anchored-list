package com.example.nivanov.myapplication

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by nivanov on 10.03.2018.
 */

class ElementViewHolder(root : View) : RecyclerView.ViewHolder(root) {
    val title : TextView = root.findViewById(R.id.title)
    var body : TextView = root.findViewById(R.id.body)
    var currentViewModel : ViewModel? = null

    init {
        root.setOnClickListener {
            adapterPosition
            Toast.makeText(title.context,"Click at $adapterPosition binded to  id=${currentViewModel?.id}"
            ,Toast.LENGTH_LONG).show()
        }
    }

    fun bind(viewModel : ViewModel) {
        title.text = viewModel.title
        body.text = viewModel.body
        currentViewModel = viewModel
    }
}

class DiffCallback(val oldList : List<ViewModel>, val newList : List<ViewModel>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]

}
data class ViewModel(val title : CharSequence, val body : CharSequence,val id : Long)

typealias ViewModelUpdate = Pair<List<ViewModel>,DiffUtil.DiffResult>


class ElementsAdapter(private val inflater : LayoutInflater,
                      private val viewModelObserver : Observable<ViewModelUpdate>) : RecyclerView.Adapter<ElementViewHolder>() {

    init {
        setHasStableIds(true)
    }

    private var currentData : List<ViewModel> = emptyList()
    private var subscription : Disposable? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ElementViewHolder =
        ElementViewHolder(inflater.inflate(R.layout.item,parent,false))


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