package com.example.lyp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class ViewSongOnList(val name: String, val length: Int)

class SongListAdapter(dataSet: ArrayList<ViewSongOnList>, mContext: Context) : ArrayAdapter<ViewSongOnList>(mContext, R.layout.song_in_list, dataSet), View.OnClickListener {

    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var lastPosition = -1

    // View lookup cache
    private class ViewHolder {
        internal var txtName: TextView? = null
        internal var tatLength: TextView? = null
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        // Get the data item for this position
        val dataModel = getItem(position)
        // Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.songs_list, parent, false)
            viewHolder.txtName = convertView!!.findViewById(R.id.list_item) as TextView
            viewHolder.tatLength = convertView.findViewById(R.id.list_item_lenght) as TextView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        lastPosition = position

        viewHolder.txtName!!.text = dataModel?.name
        viewHolder.tatLength!!.text = dataModel?.length.toString()
        // Return the completed view to render on screen
        return convertView
    }
}