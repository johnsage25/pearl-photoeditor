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
import android.widget.Toast
import android.app.Dialog
import android.graphics.*
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import pearldrift.github.io.photoeditor.BaseActivity
import android.widget.ViewFlipper
import pearldrift.github.io.photoeditor.editimage.view.StickerView
import pearldrift.github.io.photoeditor.editimage.adapter.StickerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import pearldrift.github.io.photoeditor.editimage.adapter.StickerTypeAdapter
import pearldrift.github.io.photoeditor.editimage.utils.Matrix3
import android.view.View
import io.reactivex.Single

class StickerFragment : BaseEditFragment() {
    private var mainView: View? = null
    private var flipper: ViewFlipper? = null
    private var stickerView: StickerView? = null
    private var stickerAdapter: StickerAdapter? = null
    private val compositeDisposable = CompositeDisposable()
    private var loadingDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mainView = inflater.inflate(R.layout.fragment_edit_image_sticker_type,
            null)
        loadingDialog = BaseActivity.getLoadingDialog(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_saving_image,
            false)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        stickerView = activity?.stickerView
        flipper = mainView!!.findViewById(R.id.flipper)
        flipper?.setInAnimation(activity, R.anim.in_bottom_to_top)
        flipper?.setOutAnimation(activity, R.anim.out_bottom_to_top)
        val typeList = mainView!!
            .findViewById<RecyclerView>(R.id.stickers_type_list)
        typeList.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        typeList.layoutManager = mLayoutManager
        typeList.adapter = StickerTypeAdapter(this)
        val stickerList = mainView!!.findViewById<RecyclerView>(R.id.stickers_list)
        stickerList.setHasFixedSize(true)
        val stickerListLayoutManager = LinearLayoutManager(
            activity)
        stickerListLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        stickerList.layoutManager = stickerListLayoutManager
        stickerAdapter = StickerAdapter(this)
        stickerList.adapter = stickerAdapter
        val backToMenu = mainView!!.findViewById<View>(R.id.back_to_main)
        backToMenu.setOnClickListener(BackToMenuClick())
        val backToType = mainView!!.findViewById<View>(R.id.back_to_type)
        backToType.setOnClickListener { v: View? -> flipper?.showPrevious() }
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_STICKERS
        activity?.stickerFragment!!.stickerView!!.visibility = View.VISIBLE
        activity?.sendbuttonHolder!!.visibility = View.GONE
    }

    fun swipToStickerDetails(path: String?, stickerCount: Int) {
        stickerAdapter!!.addStickerImages(path!!, stickerCount)
        flipper!!.showNext()
    }

    fun selectedStickerItem(path: String?) {
        val imageKey = resources.getIdentifier(path, "drawable", context?.packageName)
        val bitmap = BitmapFactory.decodeResource(resources, imageKey)
        stickerView!!.addBitImage(bitmap)
    }

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun backToMain() {
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = 0
        stickerView!!.clear()
        stickerView!!.visibility = View.GONE
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    fun applyStickers() {
        compositeDisposable.clear()
        val saveStickerDisposable = applyStickerToImage(activity?.mainBit)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ bitmap: Bitmap? ->
                if (bitmap == null) {
                    return@subscribe
                }
                stickerView!!.clear()
                activity?.changeMainBitmap(bitmap, true)
                backToMain()
            }) { e: Throwable? ->
                Toast.makeText(getActivity(),
                    R.string.iamutkarshtiwari_github_io_ananas_save_error,
                    Toast.LENGTH_SHORT).show()
            }
        compositeDisposable.add(saveStickerDisposable)
    }

    private fun applyStickerToImage(mainBitmap: Bitmap?): Single<Bitmap> {
        return Single.fromCallable {
            val context = requireActivity() as EditImageActivity
            val touchMatrix = context.mainImage!!.imageViewMatrix
            val resultBitmap = Bitmap.createBitmap(mainBitmap).copy(
                Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(resultBitmap)
            val data = FloatArray(9)
            touchMatrix.getValues(data)
            val cal = Matrix3(data)
            val inverseMatrix = cal.inverseMatrix()
            val m = Matrix()
            m.setValues(inverseMatrix.values)
            handleImage(canvas, m)
            resultBitmap
        }
    }

    private fun handleImage(canvas: Canvas, m: Matrix) {
        val addItems = stickerView!!.bank
        for (id in addItems.keys) {
            val item = addItems[id]
            item!!.matrix.postConcat(m)
            canvas.drawBitmap(item.bitmap, item.matrix, null)
        }
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_STICKER
        val TAG = StickerFragment::class.java.name
        fun newInstance(): StickerFragment {
            return StickerFragment()
        }
    }
}