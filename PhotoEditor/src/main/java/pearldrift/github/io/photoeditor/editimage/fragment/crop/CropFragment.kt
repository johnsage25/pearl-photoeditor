package pearldrift.github.io.photoeditor.editimage.fragment.crop


import android.annotation.SuppressLint
import pearldrift.github.io.photoeditor.editimage.fragment.BaseEditFragment
import com.theartofdev.edmodo.cropper.CropImageView
import pearldrift.github.io.photoeditor.editimage.interfaces.OnLoadingDialogListener
import io.reactivex.disposables.CompositeDisposable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import pearldrift.github.io.photoeditor.R
import android.view.Gravity
import android.graphics.Typeface
import android.graphics.Bitmap
import android.view.View
import android.widget.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import io.reactivex.Single

class CropFragment : BaseEditFragment() {
    private var mainView: View? = null
    private var ratioList: LinearLayout? = null
    private var cropPanel: CropImageView? = null
    private var loadingDialogListener: OnLoadingDialogListener? = null
    private val cropRatioClick = CropRationClick()
    private var selectedTextView: TextView? = null
    private val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_crop, null)
        return mainView
    }

    private fun setUpRatioList() {
        ratioList!!.removeAllViews()
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        params.leftMargin = 20
        params.rightMargin = 20
        val ratioTextList = RatioText.values()
        for (i in ratioTextList.indices) {

            val text = TextView(activity)
            val image = ImageView(activity)
            image.setImageDrawable(resources.getDrawable(R.drawable.ic_aspect_ratio))
            toggleButtonStatus(text, false)
            text.textSize = 15f
            text.isAllCaps = true
            text.setTypeface(text.typeface, Typeface.BOLD)
            text.text = resources.getText(ratioTextList[i].ratioTextId)


            ratioList!!.addView(text, params)

            if (i == 0) {
                selectedTextView = text
            }
            text.tag = ratioTextList[i]
            text.setOnClickListener(cropRatioClick)
        }
        toggleButtonStatus(selectedTextView, true)
    }

    private inner class CropRationClick : View.OnClickListener {
        override fun onClick(view: View) {
            toggleButtonStatus(selectedTextView, false)
            val currentTextView = view as TextView
            toggleButtonStatus(currentTextView, true)
            selectedTextView = currentTextView
            val ratioText = currentTextView.tag as RatioText
            if (ratioText === RatioText.FREE) {
                cropPanel!!.setFixedAspectRatio(false)
            } else if (ratioText === RatioText.FIT_IMAGE) {
                val currentBmp = ensureEditActivity()?.mainBit
                cropPanel!!.setAspectRatio(currentBmp!!.width, currentBmp.height)
            } else {
                val (aspectX, aspectY) = ratioText.aspectRatio
                cropPanel!!.setAspectRatio(aspectX, aspectY)
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun toggleButtonStatus(view: TextView?, isActive: Boolean) {
        view!!.setTextColor(getColorFromRes(if (isActive) SELECTED_COLOR else UNSELECTED_COLOR))
        view.typeface = if (isActive) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun getColorFromRes(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(requireContext(), resId)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadingDialogListener = ensureEditActivity()
        val backToMenu = mainView!!.findViewById<View>(R.id.back_to_main)
        ratioList = mainView!!.findViewById(R.id.ratio_list_group)
        setUpRatioList()
        cropPanel = ensureEditActivity()?.cropPanel
        backToMenu.setOnClickListener(BackToMenuClick())
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_CROP
        activity?.mainImage!!.visibility = View.GONE
        cropPanel!!.visibility = View.VISIBLE
        activity?.applyButton?.visibility = View.VISIBLE
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        activity?.mainImage!!.setScaleEnabled(false)
        activity?.sendbuttonHolder!!.visibility = View.GONE
        cropPanel!!.setImageBitmap(activity?.mainBit)
        cropPanel!!.setFixedAspectRatio(false)
    }

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    }

    override fun backToMain() {
        activity?.mode = EditImageActivity.MODE_NONE
        cropPanel!!.visibility = View.GONE
        activity?.mainImage!!.visibility = View.VISIBLE
        activity?.mainImage!!.setScaleEnabled(true)
        activity?.bottomGallery!!.currentItem = 0
        if (selectedTextView != null) {
            selectedTextView!!.setTextColor(getColorFromRes(UNSELECTED_COLOR))
        }
        activity?.applyButton?.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    fun applyCropImage() {
        disposables.add(croppedBitmap
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialogListener!!.showLoadingDialog() }
            .doFinally { loadingDialogListener!!.dismissLoadingDialog() }
            .subscribe({ bitmap: Bitmap? ->
                activity?.changeMainBitmap(bitmap, true)
                backToMain()
            }) { e: Throwable ->
                e.printStackTrace()
                backToMain()
                Toast.makeText(context, "Error while saving image", Toast.LENGTH_SHORT).show()
            })
    }

    private val croppedBitmap: Single<Bitmap>
        private get() = Single.fromCallable { cropPanel!!.croppedImage }

    override fun onStop() {
        disposables.clear()
        super.onStop()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_CROP
        val TAG = CropFragment::class.java.name
        private val SELECTED_COLOR = R.color.white
        private val UNSELECTED_COLOR = R.color.text_color_gray_3
        fun newInstance(): CropFragment {
            return CropFragment()
        }
    }
}