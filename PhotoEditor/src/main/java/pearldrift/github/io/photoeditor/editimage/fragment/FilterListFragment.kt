package pearldrift.github.io.photoeditor.editimage.fragment

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pearldrift.github.io.photoeditor.BaseActivity
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.EditImageActivity
import pearldrift.github.io.photoeditor.editimage.ModuleConfig
import pearldrift.github.io.photoeditor.editimage.adapter.FilterAdapter
import pearldrift.github.io.photoeditor.editimage.fliter.PhotoProcessing
import pearldrift.github.io.photoeditor.editimage.view.imagezoom.ImageViewTouchBase
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class FilterListFragment : BaseEditFragment() {
    private var mainView: View? = null
    private var filterBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null
    private var loadingDialog: Dialog? = null
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mainView = inflater.inflate(R.layout.fragment_edit_image_fliter, null)
        loadingDialog = BaseActivity.getLoadingDialog(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_loading,
            false)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val filterRecyclerView = mainView!!.findViewById<RecyclerView>(R.id.filter_recycler)
        val filterAdapter = FilterAdapter(this, context)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        filterRecyclerView.layoutManager = layoutManager
        filterRecyclerView.adapter = filterAdapter
        val backBtn = mainView!!.findViewById<View>(R.id.back_to_main)
        backBtn.setOnClickListener { v: View? -> backToMain() }
    }

    override fun onShow() {
        activity?.mode = EditImageActivity.MODE_FILTER
        activity?.filterListFragment!!.setCurrentBitmap(activity?.mainBit)
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        activity?.mainImage!!.setScaleEnabled(false)
        activity?.applyButton?.visibility = View.VISIBLE

        activity?.sendbuttonHolder!!.visibility = View.GONE
    }

    override fun backToMain() {
        currentBitmap = activity?.mainBit
        filterBitmap = null
        activity?.applyButton?.visibility = View.GONE
        activity?.mainImage!!.setImageBitmap(activity?.mainBit)
        activity?.mode = EditImageActivity.MODE_NONE
        activity?.bottomGallery!!.currentItem = 0
        activity?.mainImage!!.setScaleEnabled(true)
        activity?.sendbuttonHolder!!.visibility = View.VISIBLE
    }

    fun applyFilterImage() {
        if (currentBitmap == activity?.mainBit) {
            backToMain()
        } else {
            activity?.changeMainBitmap(filterBitmap, true)
            backToMain()
        }
    }

    override fun onDestroy() {
        tryRecycleFilterBitmap()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun tryRecycleFilterBitmap() {
        if (filterBitmap != null && !filterBitmap!!.isRecycled) {
            filterBitmap!!.recycle()
        }
    }

    fun enableFilter(filterIndex: Int) {
        if (filterIndex == NULL_FILTER_INDEX) {
            activity?.mainImage!!.setImageBitmap(activity?.mainBit)
            currentBitmap = activity?.mainBit
            return
        }
        val applyFilterDisposable = applyFilter(filterIndex)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { subscriber: Disposable? -> loadingDialog!!.show() }
            .doFinally { loadingDialog!!.dismiss() }
            .subscribe({ bitmapWithFilter: Bitmap? -> updatePreviewWithFilter(bitmapWithFilter) }
            ) { e: Throwable? -> showSaveErrorToast() }
        compositeDisposable.add(applyFilterDisposable)
    }

    private fun updatePreviewWithFilter(bitmapWithFilter: Bitmap?) {
        if (bitmapWithFilter == null) return
        if (filterBitmap != null && !filterBitmap!!.isRecycled) {
            filterBitmap!!.recycle()
        }
        filterBitmap = bitmapWithFilter
        activity?.mainImage!!.setImageBitmap(filterBitmap)
        currentBitmap = filterBitmap
    }

    private fun showSaveErrorToast() {
        Toast.makeText(getActivity(),
            R.string.iamutkarshtiwari_github_io_ananas_save_error,
            Toast.LENGTH_SHORT).show()
    }

    private fun applyFilter(filterIndex: Int): Single<Bitmap> {
        return Single.fromCallable {
            val srcBitmap = Bitmap.createBitmap(activity?.mainBit!!.copy(
                Bitmap.Config.RGB_565, true))
            PhotoProcessing.filterPhoto(srcBitmap, filterIndex)
        }
    }

    fun setCurrentBitmap(currentBitmap: Bitmap?) {
        this.currentBitmap = currentBitmap
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_FILTER
        const val NULL_FILTER_INDEX = 0
        val TAG = FilterListFragment::class.java.name
        fun newInstance(): FilterListFragment {
            return FilterListFragment()
        }
    }
}