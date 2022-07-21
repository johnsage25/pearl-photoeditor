package pearldrift.github.io.photoeditor.editimage.fragment


import io.reactivex.disposables.CompositeDisposable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import android.app.Dialog
import android.graphics.*
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import pearldrift.github.io.photoeditor.BaseActivity
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import pearldrift.github.io.photoeditor.editimage.view.RotateImageView
import android.view.View
import android.widget.*
import io.reactivex.Single

class RotateFragment : BaseEditFragment(), View.OnClickListener {
    private var mainView: View? = null
    private var rotatePanel: RotateImageView? = null
    private var loadingDialog: Dialog? = null
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_rotate, null)
        loadingDialog = BaseActivity.getLoadingDialog(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_loading,
            false)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        rotatePanel = ensureEditActivity()!!.rotatePanel
        setClickListeners()
    }

    private fun setClickListeners() {
        val backToMenu = mainView!!.findViewById<View>(R.id.back_to_main)
        backToMenu.setOnClickListener(BackToMenuClick())
        val rotateLeft = mainView!!.findViewById<ImageView>(R.id.rotate_left)
        val rotateRight = mainView!!.findViewById<ImageView>(R.id.rotate_right)
        rotateLeft.setOnClickListener(this)
        rotateRight.setOnClickListener(this)
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_ROTATE
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        activity?.mainImage!!.visibility = View.GONE
        activity?.rotatePanel!!.addBit(activity?.mainBit,
            activity?.mainImage!!.bitmapRect)
        activity?.rotatePanel!!.reset()
        activity?.rotatePanel!!.visibility = View.VISIBLE
        activity?.sendbuttonHolder!!.visibility = View.GONE
        activity?.applyButton?.visibility = View.VISIBLE
    }

    override fun backToMain() {
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = 0
        activity?.mainImage!!.visibility = View.VISIBLE
        activity?.applyButton?.visibility = View.GONE
        rotatePanel!!.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.rotate_left) {
            val updatedAngle = rotatePanel!!.rotateAngle - RIGHT_ANGLE
            rotatePanel!!.rotateImage(updatedAngle)
        } else if (id == R.id.rotate_right) {
            val updatedAngle = rotatePanel!!.rotateAngle + RIGHT_ANGLE
            rotatePanel!!.rotateImage(updatedAngle)
        }
    }

    fun applyRotateImage() {
        if (rotatePanel!!.rotateAngle == 0 || rotatePanel!!.rotateAngle % 360 == 0) {
            backToMain()
        } else {
            val applyRotationDisposable = applyRotation(activity?.mainBit)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
                .doFinally { loadingDialog!!.dismiss() }
                .subscribe({ processedBitmap: Bitmap? ->
                    if (processedBitmap == null) return@subscribe
                    applyAndExit(processedBitmap)
                }) { e: Throwable? -> }
            compositeDisposable.add(applyRotationDisposable)
        }
    }

    private fun applyRotation(sourceBitmap: Bitmap?): Single<Bitmap> {
        return Single.fromCallable {
            val imageRect = rotatePanel!!.imageNewRect
            val resultBitmap = Bitmap.createBitmap(imageRect.width().toInt(),
                imageRect.height().toInt(),
                Bitmap.Config.ARGB_4444)
            val canvas = Canvas(resultBitmap)
            val w = sourceBitmap!!.width shr 1
            val h = sourceBitmap.height shr 1
            val centerX = imageRect.width() / 2
            val centerY = imageRect.height() / 2
            val left = centerX - w
            val top = centerY - h
            val destinationRect = RectF(left, top, left + sourceBitmap.width, top
                    + sourceBitmap.height)
            canvas.save()
            canvas.rotate(
                rotatePanel!!.rotateAngle.toFloat(),
                imageRect.width() / 2,
                imageRect.height() / 2
            )
            canvas.drawBitmap(
                sourceBitmap,
                Rect(
                    0,
                    0,
                    sourceBitmap.width,
                    sourceBitmap.height),
                destinationRect,
                null)
            canvas.restore()
            resultBitmap
        }
    }

    private fun applyAndExit(resultBitmap: Bitmap) {
        activity?.changeMainBitmap(resultBitmap, true)
        backToMain()
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_ROTATE
        val TAG = RotateFragment::class.java.name
        private const val RIGHT_ANGLE = 90
        fun newInstance(): RotateFragment {
            return RotateFragment()
        }
    }
}