package pearldrift.github.io.photoeditor.editimage.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import pearldrift.github.io.photoeditor.editimage.EditImageActivity

abstract class BaseEditFragment : Fragment() {
    @JvmField
    protected var activity: EditImageActivity? = null
    protected fun ensureEditActivity(): EditImageActivity? {
        if (activity == null) {
            activity = getActivity() as EditImageActivity?
        }
        return activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ensureEditActivity()
    }

    override fun onResume() {
        super.onResume()
        ensureEditActivity()
    }

    abstract fun onShow()
    abstract fun backToMain()
}