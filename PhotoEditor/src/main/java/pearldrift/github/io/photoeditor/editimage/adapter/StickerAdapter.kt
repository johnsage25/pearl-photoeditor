package pearldrift.github.io.photoeditor.editimage.adapter

import pearldrift.github.io.photoeditor.editimage.fragment.StickerFragment
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.adapter.viewholders.StickerViewHolder
import java.util.ArrayList

class StickerAdapter(private val stickerFragment: StickerFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val imageClick: ImageClick = ImageClick()
    private val pathList: MutableList<String> = ArrayList()
    override fun getItemCount(): Int {
        return pathList.size
    }

    override fun getItemViewType(position: Int): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewtype: Int): RecyclerView.ViewHolder {
        val view: View
        view = LayoutInflater.from(parent.context).inflate(
            R.layout.view_sticker_item, parent, false)
        return StickerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val stickerViewHolder = viewHolder as StickerViewHolder
        val path = pathList[position]
        val imageUrl = "drawable/$path"
        val imageKey =
            stickerFragment.resources.getIdentifier(imageUrl, "drawable", stickerFragment.requireContext()
                .packageName)
        stickerViewHolder.image.setImageDrawable(stickerFragment.resources.getDrawable(imageKey))
        stickerViewHolder.image.tag = imageUrl
        stickerViewHolder.image.setOnClickListener(imageClick)
    }

    fun addStickerImages(folderPath: String, stickerCount: Int) {
        pathList.clear()
        for (i in 0 until stickerCount) {
            pathList.add(folderPath + "_" + Integer.toString(i + 1))
        }
        notifyDataSetChanged()
    }

    private inner class ImageClick : View.OnClickListener {
        override fun onClick(v: View) {
            val data = v.tag as String
            stickerFragment.selectedStickerItem(data)
        }
    }
}