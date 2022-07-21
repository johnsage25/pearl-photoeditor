package pearldrift.github.io.photoeditor.editimage

import android.Manifest
import pearldrift.github.io.photoeditor.editimage.utils.PermissionUtils.hasPermissions
import pearldrift.github.io.photoeditor.BaseActivity
import pearldrift.github.io.photoeditor.editimage.interfaces.OnLoadingDialogListener
import pearldrift.github.io.photoeditor.editimage.view.StickerView
import com.theartofdev.edmodo.cropper.CropImageView
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouch
import pearldrift.github.io.photoeditor.editimage.view.TextStickerView
import pearldrift.github.io.photoeditor.editimage.view.CustomPaintView
import pearldrift.github.io.photoeditor.editimage.view.BrightnessView
import pearldrift.github.io.photoeditor.editimage.view.SaturationView
import pearldrift.github.io.photoeditor.editimage.view.RotateImageView
import pearldrift.github.io.photoeditor.editimage.view.CustomViewPager
import pearldrift.github.io.photoeditor.editimage.fragment.crop.CropFragment
import pearldrift.github.io.photoeditor.editimage.fragment.paint.PaintFragment
import android.graphics.Bitmap
import pearldrift.github.io.photoeditor.editimage.widget.RedoUndoController
import pearldrift.github.io.photoeditor.editimage.interfaces.OnMainBitmapChangeListener
import io.reactivex.disposables.CompositeDisposable
import android.os.Bundle
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouch.OnImageFlingListener
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.content.pm.ActivityInfo
import android.content.DialogInterface
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import android.content.Intent
import android.app.Dialog
import android.content.Context
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import android.text.TextUtils
import androidx.annotation.StringRes
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentPagerAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import pearldrift.github.io.photoeditor.editimage.fragment.*
import pearldrift.github.io.photoeditor.editimage.utils.BitmapUtils
import io.reactivex.Single

class EditImageActivity : BaseActivity(), OnLoadingDialogListener {
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var sourceFilePath: String? = null
    var outputFilePath: String? = null
    var editorTitle: String? = null
    @JvmField
    var stickerView: StickerView? = null
    @JvmField
    var cropPanel: CropImageView? = null
    @JvmField
    var mainImage: ImageViewTouch? = null
    var textStickerView: TextStickerView? = null
    @JvmField
    var mode = MODE_NONE
    protected var isBeenSaved = false
    protected var isPortraitForced = false
    protected var isSupportActionBarEnabled = false
    var paintView: CustomPaintView? = null
    @JvmField
    var bannerFlipper: ViewFlipper? = null
    @JvmField
    var sendbuttonHolder: RelativeLayout? = null
    @JvmField
    var brightnessView: BrightnessView? = null
    @JvmField
    var saturationView: SaturationView? = null
    @JvmField
    var rotatePanel: RotateImageView? = null
    @JvmField
    var bottomGallery: CustomViewPager? = null
    @JvmField
    var applyButton: RelativeLayout? = null
    @JvmField
    var stickerFragment: StickerFragment? = null
    @JvmField
    var filterListFragment: FilterListFragment? = null
    @JvmField
    var cropFragment: CropFragment? = null
    @JvmField
    var rotateFragment: RotateFragment? = null
    @JvmField
    var addTextFragment: AddTextFragment? = null
    @JvmField
    var paintFragment: PaintFragment? = null
    @JvmField
    var beautyFragment: BeautyFragment? = null
    @JvmField
    var brightnessFragment: BrightnessFragment? = null
    @JvmField
    var saturationFragment: SaturationFragment? = null
    var toolsMenu : LinearLayout? = null
    protected var numberOfOperations = 0
    private var imageWidth = 0
    private var imageHeight = 0
    var mainBit: Bitmap? = null
        private set
    private var loadingDialog: Dialog? = null
    private var mainMenuFragment: MainMenuFragment? = null
    private var redoUndoController: RedoUndoController? = null

    private var onMainBitmapChangeListener: OnMainBitmapChangeListener? = null
    private val compositeDisposable = CompositeDisposable()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_edit)
        data
        initView()
    }

    override fun showLoadingDialog() {
        loadingDialog!!.show()
    }

    override fun dismissLoadingDialog() {
        loadingDialog!!.dismiss()
    }

    private val data: Unit
        private get() {
            isPortraitForced =
                intent.getBooleanExtra(ImageEditorIntentBuilder.FORCE_PORTRAIT, false)
            isSupportActionBarEnabled =
                intent.getBooleanExtra(ImageEditorIntentBuilder.SUPPORT_ACTION_BAR_VISIBILITY,
                    false)
            sourceFilePath = intent.getStringExtra(ImageEditorIntentBuilder.SOURCE_PATH)
            outputFilePath = intent.getStringExtra(ImageEditorIntentBuilder.OUTPUT_PATH)
            editorTitle = intent.getStringExtra(ImageEditorIntentBuilder.EDITOR_TITLE)
        }

    private fun initView() {
        val titleView = findViewById<TextView>(R.id.title)
        if (editorTitle != null) {
            titleView.text = editorTitle
        }
        loadingDialog = getLoadingDialog(this, R.string.iamutkarshtiwari_github_io_ananas_loading,
            false)

        if (supportActionBar != null) {
            if (isSupportActionBarEnabled) {
                supportActionBar!!.show()
            } else {
                supportActionBar!!.hide()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels / 2
        imageHeight = metrics.heightPixels / 2
        sendbuttonHolder = findViewById(R.id.sendbuttonHolder)
        bannerFlipper = findViewById(R.id.banner_flipper)
        bannerFlipper?.setInAnimation(this, R.anim.in_bottom_to_top)
        bannerFlipper?.setOutAnimation(this, R.anim.out_bottom_to_top)
        val applyBtn = findViewById<View>(R.id.apply)
        applyBtn.setOnClickListener(ApplyBtnClick())
        val saveBtn = findViewById<View>(R.id.save_btn)
        saveBtn.setOnClickListener(SaveBtnClick())
        mainImage = findViewById(R.id.main_image)
        val backBtn = findViewById<View>(R.id.back_btn)
        backBtn.setOnClickListener { v: View? -> onBackPressed() }
        stickerView = findViewById(R.id.sticker_panel)
        cropPanel = findViewById(R.id.crop_panel)
        rotatePanel = findViewById(R.id.rotate_panel)
        applyButton = findViewById(R.id.applyButton)
        textStickerView = findViewById(R.id.text_sticker_panel)
        paintView = findViewById(R.id.custom_paint_view)
        brightnessView = findViewById(R.id.brightness_panel)
        saturationView = findViewById(R.id.contrast_panel)
        bottomGallery = findViewById(R.id.bottom_gallery)
        mainMenuFragment = MainMenuFragment.newInstance()
        mainMenuFragment?.setArguments(intent.extras)

        toolsMenu = findViewById(R.id.toolsMenu)

        val bottomGalleryAdapter = BottomGalleryAdapter(
            this.supportFragmentManager)

        stickerFragment = StickerFragment.newInstance()
        filterListFragment = FilterListFragment.newInstance()
        cropFragment = CropFragment.newInstance()
        rotateFragment = RotateFragment.newInstance()
        paintFragment = PaintFragment.newInstance()
        beautyFragment = BeautyFragment.newInstance()
        brightnessFragment = BrightnessFragment.newInstance()
        saturationFragment = SaturationFragment.newInstance()
        addTextFragment = AddTextFragment.newInstance()
        setOnMainBitmapChangeListener(addTextFragment)
        bottomGallery?.setAdapter(bottomGalleryAdapter)
        mainImage?.setFlingListener(OnImageFlingListener { e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float ->
            if (velocityY > 1) {
                closeInputMethod()
            }
        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            redoUndoController = RedoUndoController(this, findViewById(R.id.redo_undo_panel))
        }
        if (!hasPermissions(this, requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSIONS_REQUEST_CODE)
        }
        loadImageFromFile(sourceFilePath)
    }

    private fun setOnMainBitmapChangeListener(listener: OnMainBitmapChangeListener?) {
        onMainBitmapChangeListener = listener
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (!(grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        // Lock orientation for this activity
        if (isPortraitForced) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            setLockScreenOrientation(true)
        }
    }

    private fun closeInputMethod() {
        if (addTextFragment!!.isAdded) {
            addTextFragment!!.hideInput()
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
    override fun onBackPressed() {

        // Back action on mainmenu
        mainMenuFragment!!.backToMain()

        toolsMenu?.visibility = View.VISIBLE
        when (mode) {
            MODE_STICKERS -> stickerFragment!!.backToMain()
            MODE_FILTER -> filterListFragment!!.backToMain()
            MODE_CROP -> cropFragment!!.backToMain()
            MODE_ROTATE -> rotateFragment!!.backToMain()
            MODE_TEXT -> addTextFragment!!.backToMain()
            MODE_PAINT -> paintFragment!!.backToMain()
            MODE_BEAUTY -> beautyFragment!!.backToMain()
            MODE_BRIGHTNESS -> brightnessFragment!!.backToMain()
            MODE_SATURATION -> saturationFragment!!.backToMain()
            else -> if (canAutoExit()) {
                onSaveTaskDone()
            } else {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setMessage(R.string.iamutkarshtiwari_github_io_ananas_exit_without_save)
                    .setCancelable(false)
                    .setPositiveButton(R.string.iamutkarshtiwari_github_io_ananas_confirm) { dialog: DialogInterface?, id: Int -> finish() }
                    .setNegativeButton(R.string.iamutkarshtiwari_github_io_ananas_cancel) { dialog: DialogInterface, id: Int -> dialog.cancel() }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    fun changeMainBitmap(newBit: Bitmap?, needPushUndoStack: Boolean) {
        if (newBit == null) return
        if (mainBit == null || mainBit != newBit) {
            if (needPushUndoStack) {
                redoUndoController!!.switchMainBit(mainBit, newBit)
                increaseOpTimes()
            }
            mainBit = newBit
            mainImage!!.setImageBitmap(mainBit)
            mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
            if (mode == MODE_TEXT) {
                onMainBitmapChangeListener!!.onMainBitmapChange()
            }
        }
    }

    protected fun onSaveTaskDone() {
        val returnIntent = Intent()
        returnIntent.putExtra(ImageEditorIntentBuilder.SOURCE_PATH, sourceFilePath)
        returnIntent.putExtra(ImageEditorIntentBuilder.OUTPUT_PATH, outputFilePath)
        returnIntent.putExtra(IS_IMAGE_EDITED, numberOfOperations > 0)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    protected fun doSaveImage() {
        if (numberOfOperations <= 0) return
        val saveImageDisposable = saveImage(mainBit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ result: Boolean ->
                if (result) {
                    resetOpTimes()
                    onSaveTaskDone()
                } else {
                    showToast(R.string.iamutkarshtiwari_github_io_ananas_save_error)
                }
            }) { e: Throwable? -> showToast(R.string.iamutkarshtiwari_github_io_ananas_save_error) }
        compositeDisposable.add(saveImageDisposable)
    }

    private fun saveImage(finalBitmap: Bitmap?): Single<Boolean> {
        return Single.fromCallable {
            if (TextUtils.isEmpty(outputFilePath)) return@fromCallable false
            BitmapUtils.saveBitmap(finalBitmap, outputFilePath)
        }
    }

    private fun loadImageFromFile(filePath: String?) {
        val loadImageDisposable = loadImage(filePath)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? ->
                loadingDialog!!.show()
                mainMenuFragment!!.setMenuOptionsClickable(false)
            }
            .doOnSuccess { bitmap: Bitmap? -> mainMenuFragment!!.setMenuOptionsClickable(true) }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ processedBitmap: Bitmap? ->
                changeMainBitmap(processedBitmap,
                    false)
            }) { e: Throwable ->
                showToast(R.string.iamutkarshtiwari_github_io_ananas_load_error)
                Log.wtf("Error", e.message)
            }
        compositeDisposable.add(loadImageDisposable)
    }

    private fun loadImage(filePath: String?): Single<Bitmap> {
        return Single.fromCallable {
            BitmapUtils.getSampledBitmap(filePath, imageWidth,
                imageHeight)
        }
    }

    private fun showToast(@StringRes resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        if (redoUndoController != null) {
            redoUndoController!!.onDestroy()
        }
        if (!isPortraitForced) {
            setLockScreenOrientation(false)
        }
    }

    protected fun setLockScreenOrientation(lock: Boolean) {
        if (Build.VERSION.SDK_INT >= 18) {
            requestedOrientation =
                if (lock) ActivityInfo.SCREEN_ORIENTATION_LOCKED else ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            return
        }
        if (lock) {
            when (windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                Surface.ROTATION_90 -> requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Surface.ROTATION_180 -> requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                Surface.ROTATION_270 -> requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        } else requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    fun increaseOpTimes() {
        numberOfOperations++
        isBeenSaved = false
    }

    fun canAutoExit(): Boolean {
        return isBeenSaved || numberOfOperations == 0
    }

    fun resetOpTimes() {
        isBeenSaved = true
    }

    private inner class BottomGalleryAdapter internal constructor(fm: FragmentManager?) :
        FragmentPagerAdapter(
            fm!!) {
        override fun getItem(index: Int): Fragment {
            when (index) {
                MainMenuFragment.INDEX -> return mainMenuFragment!!
                StickerFragment.INDEX -> return stickerFragment!!
                FilterListFragment.INDEX -> return filterListFragment!!
                CropFragment.INDEX -> return cropFragment!!
                RotateFragment.INDEX -> return rotateFragment!!
                AddTextFragment.INDEX -> return addTextFragment!!
                PaintFragment.INDEX -> return paintFragment!!
                BeautyFragment.INDEX -> return beautyFragment!!
                BrightnessFragment.INDEX -> return brightnessFragment!!
                SaturationFragment.INDEX -> return saturationFragment!!
            }
            return MainMenuFragment.newInstance()
        }

        override fun getCount(): Int {
            return 10
        }
    }

    private inner class SaveBtnClick : View.OnClickListener {
        override fun onClick(v: View) {
            if (numberOfOperations == 0) {
                onSaveTaskDone()
            } else {
                doSaveImage()
            }
        }
    }

    private inner class ApplyBtnClick : View.OnClickListener {
        override fun onClick(v: View) {
            when (mode) {
                MODE_STICKERS -> stickerFragment!!.applyStickers()
                MODE_FILTER -> filterListFragment!!.applyFilterImage()
                MODE_CROP -> cropFragment!!.applyCropImage()
                MODE_ROTATE -> rotateFragment!!.applyRotateImage()
                MODE_TEXT -> addTextFragment!!.applyTextImage()
                MODE_PAINT -> paintFragment!!.savePaintImage()
                MODE_BEAUTY -> beautyFragment!!.applyBeauty()
                MODE_BRIGHTNESS -> brightnessFragment!!.applyBrightness()
                MODE_SATURATION -> saturationFragment!!.applySaturation()
                else -> {}
            }
        }
    }

    companion object {
        const val IS_IMAGE_EDITED = "is_image_edited"
        const val MODE_NONE = 0
        const val MODE_STICKERS = 1
        const val MODE_FILTER = 2
        const val MODE_CROP = 3
        const val MODE_ROTATE = 4
        const val MODE_TEXT = 5
        const val MODE_PAINT = 6
        const val MODE_BEAUTY = 7
        const val MODE_BRIGHTNESS = 8
        const val MODE_SATURATION = 9
        private const val PERMISSIONS_REQUEST_CODE = 110
        @JvmStatic
        fun start(launcher: ActivityResultLauncher<Intent?>, intent: Intent, context: Context?) {
            if (TextUtils.isEmpty(intent.getStringExtra(ImageEditorIntentBuilder.SOURCE_PATH))) {
                Toast.makeText(context,
                    R.string.iamutkarshtiwari_github_io_ananas_not_selected,
                    Toast.LENGTH_SHORT).show()
                return
            }
            launcher.launch(intent)
        }
    }
}