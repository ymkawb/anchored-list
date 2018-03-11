package com.github.ymkawb.anchored_list

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.facebook.drawee.view.SimpleDraweeView

/**
 * Created by nivanov on 10.03.2018.
 */

class ElementViewHolder(root : View) : RecyclerView.ViewHolder(root) {

    val title : TextView = root.findViewById(R.id.title)
    var body : TextView = root.findViewById(R.id.body)
    val avatar : SimpleDraweeView = root.findViewById(R.id.avatar)

    var currentViewModel : ViewModel? = null

    init {
        root.setOnClickListener {
            adapterPosition
            Toast.makeText(title.context, "Click at $adapterPosition binded to  id=${currentViewModel?.id}"
                    , Toast.LENGTH_LONG).show()
        }
    }

    fun bind(viewModel : ViewModel) {
        title.text = viewModel.title
        body.text = viewModel.body
        currentViewModel = viewModel
        avatar.setImageURI(viewModel.imageUri)
    }
}