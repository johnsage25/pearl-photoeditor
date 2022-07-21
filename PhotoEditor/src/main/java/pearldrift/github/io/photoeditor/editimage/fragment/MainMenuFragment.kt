package pearldrift.github.io.photoeditor.editimage.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.ImageEditorIntentBuilder
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import pearldrift.github.io.photoeditor.editimage.fragment.crop.CropFragment
import pearldrift.github.io.photoeditor.editimage.fragment.mainmenu.MenuSectionnActions
import pearldrift.github.io.photoeditor.editimage.fragment.paint.PaintFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class MainMenuFragment : BaseEditFragment(), View.OnClickListener, MenuSectionnActions {
    private var mainView: View? = null
    private var stickerBtn: View? = null
    private var filterBtn: View? = null
    private var cropBtn: View? = null
    private var rotateBtn: View? = null
    private var textBtn: View? = null
    private var paintBtn: View? = null
    private var beautyBtn: View? = null
    private var brightnessBtn: View? = null
    private var saturationBtn: View? = null
    var displayedChild = 0
//    private var simpleViewFlipper: ViewFlipper?= null
    private var intentBundle: Bundle? = null
    var toolsMenu : LinearLayout? = null
    var showMoreButton: RelativeLayout? =null
    private val menuOptionsClickableSubject = BehaviorSubject.create<Boolean>()
    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_main_menu,
            null)
        intentBundle = arguments
        return mainView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stickerBtn = mainView!!.findViewById(R.id.btn_stickers)
        filterBtn = mainView!!.findViewById(R.id.btn_filter)
        cropBtn = mainView!!.findViewById(R.id.btn_crop)
        rotateBtn = mainView!!.findViewById(R.id.btn_rotate)
        textBtn = mainView!!.findViewById(R.id.btn_text)
        paintBtn = mainView!!.findViewById(R.id.btn_paint)
        beautyBtn = mainView!!.findViewById(R.id.btn_beauty)
        brightnessBtn = mainView!!.findViewById(R.id.btn_brightness)
        showMoreButton = mainView?.findViewById(R.id.showMore)
        saturationBtn = mainView!!.findViewById(R.id.btn_contrast)
//        simpleViewFlipper = mainView?.findViewById(R.id.simpleViewFlipper)
        toolsMenu = mainView?.findViewById(R.id.toolsMenu)

        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.STICKER_FEATURE, false)) {
            stickerBtn?.setVisibility(View.VISIBLE)
            stickerBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.FILTER_FEATURE, false)) {
            filterBtn?.setVisibility(View.VISIBLE)
            filterBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.CROP_FEATURE, false)) {
            cropBtn?.setVisibility(View.VISIBLE)
            cropBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.ROTATE_FEATURE, false)) {
            rotateBtn?.setVisibility(View.VISIBLE)
            rotateBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.ADD_TEXT_FEATURE, false)) {
            textBtn?.setVisibility(View.VISIBLE)
            textBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.PAINT_FEATURE, false)) {
            paintBtn?.setVisibility(View.VISIBLE)
            paintBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.BEAUTY_FEATURE, false)) {
            beautyBtn?.setVisibility(View.VISIBLE)
            beautyBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.BRIGHTNESS_FEATURE, false)) {
            brightnessBtn?.setVisibility(View.VISIBLE)
            brightnessBtn?.setOnClickListener(this)
        }
        if (intentBundle!!.getBoolean(ImageEditorIntentBuilder.SATURATION_FEATURE, false)) {
            saturationBtn?.setVisibility(View.VISIBLE)
            saturationBtn?.setOnClickListener(this)
        }

        showMoreButton?.setVisibility(View.VISIBLE)


//        showMoreButton?.setOnClickListener({
//            simpleViewFlipper?.saveHierarchyState(SparseArray())
//            simpleViewFlipper?.restoreDefaultFocus()
//            simpleViewFlipper?.restoreHierarchyState(SparseArray())
//            simpleViewFlipper?.displayedChild = displayedChild
//            simpleViewFlipper?.setInAnimation(activity, R.anim.in_bottom_to_top)
//            simpleViewFlipper?.setOutAnimation(activity, R.anim.out_bottom_to_top)
//        })

        subscribeMenuOptionsSubject()
    }

    private fun subscribeMenuOptionsSubject() {
        disposable.add(
            menuOptionsClickableSubject
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe({ isClickable: Boolean? ->
                    stickerBtn!!.isClickable = isClickable!!
                    filterBtn!!.isClickable = isClickable
                    cropBtn!!.isClickable = isClickable
                    rotateBtn!!.isClickable = isClickable
                    textBtn!!.isClickable = isClickable
                    paintBtn!!.isClickable = isClickable
                    beautyBtn!!.isClickable = isClickable
                    brightnessBtn!!.isClickable = isClickable
                    saturationBtn!!.isClickable = isClickable
                }
                ) { error: Throwable? -> }
        )
    }

    override fun setMenuOptionsClickable(isClickable: Boolean) {
        menuOptionsClickableSubject.onNext(isClickable)
    }

    override fun onShow() {
        // do nothing

    }

    override fun backToMain() {
        //do nothing
    }

    override fun onClick(v: View) {
        if (v === stickerBtn) {
            onStickClick()
        } else if (v === filterBtn) {
            onFilterClick()
        } else if (v === cropBtn) {
            onCropClick()
        } else if (v === textBtn) {
            onAddTextClick()
        }
        else if (v === rotateBtn) {
            onRotateClick()
        }
        else if (v === paintBtn) {
            onPaintClick()
        } else if (v === beautyBtn) {
            onBeautyClick()
        } else if (v === brightnessBtn) {
            onBrightnessClick()
        } else if (v === saturationBtn) {
            onContrastClick()
        }
    }

    private fun onStickClick() {
        activity?.bottomGallery!!.currentItem = StickerFragment.Companion.INDEX
        activity?.stickerFragment!!.onShow()
    }

    private fun onFilterClick() {
        activity?.bottomGallery!!.currentItem = FilterListFragment.INDEX
        activity?.filterListFragment!!.onShow()
    }

    private fun onCropClick() {
        activity?.bottomGallery!!.currentItem = CropFragment.INDEX
        activity?.cropFragment!!.onShow()

    }

    private fun onRotateClick() {
        activity?.bottomGallery!!.currentItem = RotateFragment.Companion.INDEX
        activity?.rotateFragment!!.onShow()
    }

    private fun onAddTextClick() {
        activity?.bottomGallery!!.currentItem = AddTextFragment.Companion.INDEX
        activity?.addTextFragment!!.onShow()
    }

    private fun onPaintClick() {
        activity?.bottomGallery!!.currentItem = PaintFragment.INDEX
        activity?.paintFragment!!.onShow()
    }

    private fun onBeautyClick() {
        activity?.bottomGallery!!.currentItem = BeautyFragment.Companion.INDEX
        activity?.beautyFragment!!.onShow()
    }

    private fun onBrightnessClick() {
        activity?.bottomGallery!!.currentItem = BrightnessFragment.Companion.INDEX
        activity?.brightnessFragment!!.onShow()
    }

    private fun onContrastClick() {
        activity?.bottomGallery!!.currentItem = SaturationFragment.Companion.INDEX
        activity?.saturationFragment!!.onShow()
    }

    override fun onDestroyView() {
        disposable.dispose()
        super.onDestroyView()
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_MAIN
        val TAG = MainMenuFragment::class.java.name
        fun newInstance(): MainMenuFragment {
            return MainMenuFragment()
        }
    }
}