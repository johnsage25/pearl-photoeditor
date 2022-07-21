package pearldrift.github.io.photoeditor.editimage.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import pearldrift.github.io.photoeditor.editimage.view.BrightnessView
import android.graphics.drawable.BitmapDrawable
import android.view.View
import pearldrift.github.io.photoeditor.editimage.utils.Utils

class BrightnessFragment : BaseEditFragment() {
    private var mBrightnessView: BrightnessView? = null
    private var mSeekBar: SeekBar? = null
    private var mainView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_brightness, null)
        mappingView(mainView)
        return mainView
    }

    private fun mappingView(view: View?) {
        mSeekBar = view?.findViewById(R.id.seekBar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mBackToMenu = mainView!!.findViewById<View>(R.id.back_to_main)
        mBrightnessView = ensureEditActivity()!!.brightnessView
        mBackToMenu.setOnClickListener(BackToMenuClick())
        mSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = (progress - seekBar.max / 2).toFloat()
                activity!!.brightnessView!!.bright = value / 10f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        initView()
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_BRIGHTNESS
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        activity?.mainImage!!.visibility = View.GONE
        activity?.brightnessView!!.setImageBitmap(activity?.mainBit)
        activity?.applyButton?.visibility = View.VISIBLE
        activity?.brightnessView!!.visibility = View.VISIBLE
        initView()
        activity?.sendbuttonHolder!!.visibility = View.GONE
    }

    override fun backToMain() {
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = 0
        activity?.mainImage!!.visibility = View.VISIBLE
        activity?.brightnessView!!.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
        activity?.applyButton?.visibility = View.GONE
        activity?.brightnessView!!.bright = INITIAL_BRIGHTNESS.toFloat()
    }

    fun applyBrightness() {
        if (mSeekBar!!.progress == mSeekBar!!.max / 2) {
            backToMain()
            return
        }
        val bitmap = (mBrightnessView!!.drawable as BitmapDrawable).bitmap
        activity?.changeMainBitmap(Utils.brightBitmap(bitmap, mBrightnessView!!.bright), true)
        backToMain()
    }

    private fun initView() {
        mSeekBar!!.progress = mSeekBar!!.max / 2
    }

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_BRIGHTNESS
        val TAG = BrightnessFragment::class.java.name
        private const val INITIAL_BRIGHTNESS = 0
        fun newInstance(): BrightnessFragment {
            return BrightnessFragment()
        }
    }
}