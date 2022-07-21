package pearldrift.github.io.photoeditor.editimage.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rtugeek.android.colorseekbar.ColorSeekBar
import com.rtugeek.android.colorseekbar.thumb.DefaultThumbDrawer
import pearldrift.github.io.photoeditor.R
import pearldrift.github.io.photoeditor.editimage.adapter.ColorPickerAdapter
import pearldrift.github.io.photoeditor.editimage.interfaces.OnTextEditorListener


class TextEditorDialogFragment : DialogFragment() {
    private var addTextEditText: EditText? = null
    private var inputMethodManager: InputMethodManager? = null
    private var colorCode = 0
//    private var colorSeekBar: ColorSeekBar?= null
    private var onTextEditorListener: OnTextEditorListener? = null
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //Make dialog full screen with transparent background
        if (dialog != null) {

            var metric = context?.getResources()?.getDisplayMetrics();

            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            val window = dialog.window
            if (window != null) {
                dialog.window.setLayout(width, height)
                dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_text_sticker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTextEditText = view.findViewById(R.id.add_text_edit_text)
        inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val addTextDoneTv = view.findViewById<TextView>(R.id.add_text_done_tv)

        //Setup the color picker for text color
        val addTextColorPickerRecyclerView =
            view.findViewById<RecyclerView>(R.id.add_text_color_picker_recycler_view)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(requireContext())

        var colorSeekBar = view.findViewById<ColorSeekBar>(R.id.colorSeekBar)
        colorSeekBar.thumbDrawer = DefaultThumbDrawer(80,Color.WHITE,Color.BLUE)

        colorSeekBar.setOnColorChangeListener { progress, color ->
             colorCode = color
            addTextEditText?.setTextColor(color)
            Log.i("TAG", "===progress:$progress-color:$color===")
        }


// Listen individual pickers or groups for changes
//        group.addListener(
//            object : ColorSeekBar.OnColorPickListener<ColorSeekBar<IntegerHSLColor>, IntegerHSLColor> {
//                override fun onColorChanged(
//                    picker: ColorSeekBar<IntegerHSLColor>,
//                    color: IntegerHSLColor,
//                    value: Int,
//                ) {
//
//                }
//
//                override fun onColorPicked(
//                    picker: ColorSeekBar<IntegerHSLColor>,
//                    color: IntegerHSLColor,
//                    value: Int,
//                    fromUser: Boolean,
//                ) {
//
//
//
//
//                }
//
//                override fun onColorPicking(
//                    picker: ColorSeekBar<IntegerHSLColor>,
//                    color: IntegerHSLColor,
//                    value: Int,
//                    fromUser: Boolean,
//                ) {
//
//
//                }
//            }
//        )
        //This listener will change the text color when clicked on any color from picker
//        colorPickerAdapter.setOnColorPickerClickListener { colorCode: Int ->
//            this.colorCode = colorCode
//            addTextEditText?.setTextColor(colorCode)
//        }
//        addTextColorPickerRecyclerView.adapter = colorPickerAdapter
        addTextEditText?.setText(arguments?.getString(EXTRA_INPUT_TEXT))
        colorCode = requireArguments().getInt(EXTRA_COLOR_CODE)
        addTextEditText?.setTextColor(colorCode)
        inputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing
        addTextDoneTv.setOnClickListener { view1: View ->
            inputMethodManager!!.hideSoftInputFromWindow(view1.windowToken, 0)
            val inputText = addTextEditText?.getText().toString()
            if (!TextUtils.isEmpty(inputText) && onTextEditorListener != null) {
                onTextEditorListener!!.onDone(inputText, colorCode)
            }
            dismiss()
        }
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(onTextEditorListener: OnTextEditorListener?) {
        this.onTextEditorListener = onTextEditorListener
    }

    companion object {
        val TAG = TextEditorDialogFragment::class.java.simpleName
        private const val EXTRA_INPUT_TEXT = "extra_input_text"
        private const val EXTRA_COLOR_CODE = "extra_color_code"

        //Show dialog with provide text and text color
        //Show dialog with default text input as empty and text color white
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt initialColorCode: Int = ContextCompat.getColor(appCompatActivity,
                R.color.white),
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, initialColorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}