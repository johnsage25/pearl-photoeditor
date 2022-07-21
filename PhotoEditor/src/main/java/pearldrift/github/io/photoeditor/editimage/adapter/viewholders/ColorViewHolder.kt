package pearldrift.github.io.photoeditor.editimage.adapter.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import pearldrift.github.io.photoeditor.R

class ColorViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField var colorPanelView: View = itemView.findViewById(R.id.color_panel_view)
}