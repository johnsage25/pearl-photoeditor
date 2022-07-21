package pearldrift.github.io.photoeditor.editimage.fragment


import io.reactivex.disposables.CompositeDisposable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import io.reactivex.disposables.Disposable
import android.graphics.Bitmap
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import android.app.Dialog
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.SeekBar
import pearldrift.github.io.photoeditor.BaseActivity
import pearldrift.github.io.photoeditor.editimage.fliter.PhotoProcessing
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import android.view.View
import io.reactivex.Single

class BeautyFragment : BaseEditFragment(), OnSeekBarChangeListener {
    private var mainView: View? = null
    private var dialog: Dialog? = null
    private var smoothValueBar: SeekBar? = null
    private var whiteValueBar: SeekBar? = null
    private val disposable = CompositeDisposable()
    private var beautyDisposable: Disposable? = null
    private var finalBmp: Bitmap? = null
    private var smooth = 0
    private var whiteSkin = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_beauty, null)
        smoothValueBar = mainView?.findViewById(R.id.smooth_value_bar)
        whiteValueBar = mainView?.findViewById(R.id.white_skin_value_bar)
        dialog = BaseActivity.getLoadingDialog(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_loading,
            false)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val backToMenu = mainView!!.findViewById<View>(R.id.back_to_main)
        backToMenu.setOnClickListener(BackToMenuClick()) // 返回主菜单
        smoothValueBar!!.setOnSeekBarChangeListener(this)
        whiteValueBar!!.setOnSeekBarChangeListener(this)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        doBeautyTask()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    protected fun doBeautyTask() {
        if (beautyDisposable != null && !beautyDisposable!!.isDisposed) {
            beautyDisposable!!.dispose()
        }
        smooth = smoothValueBar!!.progress
        whiteSkin = whiteValueBar!!.progress
        if (smooth == 0 && whiteSkin == 0) {
            activity?.mainImage!!.setImageBitmap(activity?.mainBit)
            return
        }
        beautyDisposable = beautify(smooth, whiteSkin)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> dialog!!.show() }
            .doFinally { dialog!!.dismiss() }
            .subscribe({ bitmap: Bitmap? ->
                if (bitmap == null) return@subscribe
                activity?.mainImage!!.setImageBitmap(bitmap)
                finalBmp = bitmap
            }) { e: Throwable? -> }
        disposable.add(beautyDisposable!!)
    }

    private fun beautify(smoothVal: Int, whiteSkinVal: Int): Single<Bitmap> {
        return Single.fromCallable {
            val srcBitmap = Bitmap.createBitmap(
                activity?.mainBit!!.copy(
                    Bitmap.Config.ARGB_8888, true)
            )
            PhotoProcessing.handleSmoothAndWhiteSkin(srcBitmap,
                smoothVal.toFloat(),
                whiteSkinVal.toFloat())
            srcBitmap
        }
    }

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    }

    override fun backToMain() {
        smooth = 0
        whiteSkin = 0
        smoothValueBar!!.progress = 0
        whiteValueBar!!.progress = 0
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = MainMenuFragment.Companion.INDEX
        activity?.mainImage!!.setImageBitmap(activity?.mainBit) // 返回原图
        activity?.mainImage!!.visibility = View.VISIBLE
        activity?.mainImage!!.setScaleEnabled(true)
        activity?.applyButton?.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_BEAUTY
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        activity?.mainImage!!.setScaleEnabled(false)
        activity?.applyButton?.visibility = View.VISIBLE
        activity?.sendbuttonHolder!!.visibility = View.GONE
    }

    fun applyBeauty() {
        if (finalBmp != null && (smooth != 0 || whiteSkin != 0)) {
            activity?.changeMainBitmap(finalBmp, true)
        }
        backToMain()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    companion object {
        val TAG = BeautyFragment::class.java.name
        const val INDEX = ModuleConfig.INDEX_BEAUTY
        fun newInstance(): BeautyFragment {
            return BeautyFragment()
        }
    }
}