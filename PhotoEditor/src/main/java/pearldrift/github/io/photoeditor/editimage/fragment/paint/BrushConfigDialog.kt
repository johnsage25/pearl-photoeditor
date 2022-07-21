package pearldrift.github.io.photoeditor.editimage.fragment.paint

import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.SeekBar.OnSeekBarChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pearldrift.github.io.photoeditor.R
import android.widget.SeekBar
import com.rtugeek.android.colorseekbar.ColorSeekBar
import com.rtugeek.android.colorseekbar.thumb.DefaultThumbDrawer

class BrushConfigDialog : BottomSheetDialogFragment(), OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onBrushSizeChanged(brushSize: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brush_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var colorSeekBar = view.findViewById<ColorSeekBar>(R.id.colorSeekBar)
        colorSeekBar.thumbDrawer = DefaultThumbDrawer(80, Color.WHITE, Color.BLUE)

        colorSeekBar.setOnColorChangeListener { progress, color ->
//            dismiss()
            mProperties!!.onColorChanged(color)
        }

        val sbOpacity = view.findViewById<SeekBar>(R.id.sbOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sbSize)
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)


    }

    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        val id = seekBar.id
        if (id == R.id.sbOpacity) {
            if (mProperties != null) {
                mProperties!!.onOpacityChanged(i)
            }
        } else if (id == R.id.sbSize) {
            if (mProperties != null) {
                mProperties!!.onBrushSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}