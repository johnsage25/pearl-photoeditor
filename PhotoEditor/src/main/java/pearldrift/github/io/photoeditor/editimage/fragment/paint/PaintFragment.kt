package pearldrift.github.io.photoeditor.editimage.fragment.paint

import android.app.Dialog

import pearldrift.github.io.photoeditor.editimage.fragment.BaseEditFragment
import pearldrift.github.io.photoeditor.editimage.view.CustomPaintView
import android.widget.LinearLayout
import io.reactivex.disposables.CompositeDisposable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import pearldrift.github.io.photoeditor.editimage.fragment.MainMenuFragment
import io.reactivex.disposables.Disposable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.view.View
import android.widget.ImageView
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import pearldrift.github.io.photoeditor.editimage.utils.Matrix3
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import io.reactivex.Single

class PaintFragment : BaseEditFragment(), View.OnClickListener, BrushConfigDialog.Properties,
    EraserConfigDialog.Properties {
    private var mainView: View? = null
    private var isEraser = false
    private var backToMenu: View? = null
    private var customPaintView: CustomPaintView? = null
    private var eraserView: LinearLayout? = null
    private var brushView: LinearLayout? = null
    private var brushConfigDialog: BrushConfigDialog? = null
    private var eraserConfigDialog: EraserConfigDialog? = null
    private var loadingDialog: Dialog? = null
    private var brushSize = INITIAL_WIDTH
    private var eraserSize = INITIAL_WIDTH
    private var brushAlpha = MAX_ALPHA
    private var brushColor = Color.WHITE
    private val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_paint, null)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadingDialog = BaseActivity.getLoadingDialog(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_loading,
            false)
        customPaintView = ensureEditActivity()!!.findViewById(R.id.custom_paint_view)
        backToMenu = mainView!!.findViewById(R.id.back_to_main)
        eraserView = mainView!!.findViewById(R.id.eraser_btn)
        brushView = mainView!!.findViewById(R.id.brush_btn)
        mainView!!.findViewById<View>(R.id.settings).setOnClickListener(this)
        setupOptionsConfig()
        backToMenu?.setOnClickListener(this)
        setClickListeners()
        initStroke()
    }

    private fun setupOptionsConfig() {
        brushConfigDialog = BrushConfigDialog()
        brushConfigDialog!!.setPropertiesChangeListener(this)
        eraserConfigDialog = EraserConfigDialog()
        eraserConfigDialog!!.setPropertiesChangeListener(this)
    }

    private fun setClickListeners() {
        brushView!!.setOnClickListener(this)
        eraserView!!.setOnClickListener(this)
    }

    private fun initStroke() {
        customPaintView!!.setWidth(INITIAL_WIDTH)
        customPaintView!!.setColor(Color.WHITE)
        customPaintView!!.setStrokeAlpha(MAX_ALPHA)
        customPaintView!!.setEraserStrokeWidth(INITIAL_WIDTH)
    }

    override fun onClick(view: View) {
        if (view === backToMenu) {
            backToMain()
        } else if (view === eraserView) {
            if (!isEraser) {
                toggleButtons()
            }
        } else if (view === brushView) {
            if (isEraser) {
                toggleButtons()
            }
        } else if (view.id == R.id.settings) {
            showDialog(if (isEraser) eraserConfigDialog else brushConfigDialog)
        }
    }

    private fun showDialog(dialogFragment: BottomSheetDialogFragment?) {
        val tag = dialogFragment!!.tag

        // Avoid IllegalStateException "Fragment already added"
        if (dialogFragment.isAdded) return
        dialogFragment.show(requireFragmentManager(), tag)
        if (isEraser) {
            updateEraserSize()
        } else {
            updateBrushParams()
        }
    }

    override fun backToMain() {
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = MainMenuFragment.INDEX
        activity?.mainImage!!.visibility = View.VISIBLE
        activity?.bannerFlipper!!.showPrevious()
        customPaintView!!.reset()
        customPaintView!!.visibility = View.GONE
        activity?.applyButton?.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_PAINT
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.sendbuttonHolder!!.visibility = View.GONE
        activity?.applyButton?.visibility = View.VISIBLE
        customPaintView!!.visibility = View.VISIBLE
    }

    private fun toggleButtons() {
        isEraser = !isEraser
        customPaintView!!.setEraser(isEraser)
        (eraserView!!.findViewById<View>(R.id.eraser_icon) as ImageView).setImageResource(if (isEraser) R.drawable.ic_eraser_enabled else R.drawable.ic_eraser_disabled)
        (brushView!!.findViewById<View>(R.id.brush_icon) as ImageView).setImageResource(if (isEraser) R.drawable.ic_brush_grey_24dp else R.drawable.ic_brush_white_24dp)
    }

    fun savePaintImage() {
        val applyPaintDisposable = applyPaint(activity?.mainBit)
            .flatMap { bitmap: Bitmap? ->
                if (bitmap == null) {
                    return@flatMap Single.error<Bitmap>(Throwable("Error occurred while applying paint"))
                } else {
                    return@flatMap Single.just(bitmap)
                }
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ bitmap: Bitmap? ->
                customPaintView!!.reset()
                activity?.changeMainBitmap(bitmap, true)
                backToMain()
            }) { e: Throwable? -> }
        compositeDisposable.add(applyPaintDisposable)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun applyPaint(mainBitmap: Bitmap?): Single<Bitmap> {
        return Single.fromCallable {
            val touchMatrix = activity?.mainImage!!.imageViewMatrix
            val resultBit = Bitmap.createBitmap(mainBitmap).copy(
                Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(resultBit)
            val data = FloatArray(9)
            touchMatrix.getValues(data)
            val cal = Matrix3(data)
            val inverseMatrix = cal.inverseMatrix()
            val matrix = Matrix()
            matrix.setValues(inverseMatrix.values)
            handleImage(canvas, matrix)
            resultBit
        }
    }

    private fun handleImage(canvas: Canvas, matrix: Matrix) {
        val f = FloatArray(9)
        matrix.getValues(f)
        val dx = f[Matrix.MTRANS_X].toInt()
        val dy = f[Matrix.MTRANS_Y].toInt()
        val scale_x = f[Matrix.MSCALE_X]
        val scale_y = f[Matrix.MSCALE_Y]
        canvas.save()
        canvas.translate(dx.toFloat(), dy.toFloat())
        canvas.scale(scale_x, scale_y)
        if (customPaintView!!.paintBit != null) {
            canvas.drawBitmap(customPaintView!!.paintBit, 0f, 0f, null)
        }
        canvas.restore()
    }

    override fun onColorChanged(colorCode: Int) {
        brushColor = colorCode
        updateBrushParams()
    }

    override fun onOpacityChanged(opacity: Int) {
        brushAlpha = opacity / MAX_PERCENT * MAX_ALPHA
        updateBrushParams()
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        if (isEraser) {
            eraserSize = brushSize.toFloat()
            updateEraserSize()
        } else {
            this.brushSize = brushSize.toFloat()
            updateBrushParams()
        }
    }

    private fun updateBrushParams() {
        customPaintView!!.setColor(brushColor)
        customPaintView!!.setWidth(brushSize)
        customPaintView!!.setStrokeAlpha(brushAlpha)
    }

    private fun updateEraserSize() {
        customPaintView!!.setEraserStrokeWidth(eraserSize)
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_PAINT
        val TAG = PaintFragment::class.java.name
        private const val MAX_PERCENT = 100f
        private const val MAX_ALPHA = 255f
        private const val INITIAL_WIDTH = 10f
        fun newInstance(): PaintFragment {
            return PaintFragment()
        }
    }
}