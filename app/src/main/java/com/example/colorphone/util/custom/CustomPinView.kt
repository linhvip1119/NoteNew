package com.example.colorphone.util.custom

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.example.colorphone.R

class CustomPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var actionDoneListener: (() -> Unit)? = null

    private var codeInputCompleteListener: ((String) -> Unit)? = null

    private var codeInputIdSatisfiedListener: ((Boolean) -> Unit)? = null

    var isCodeInputSatisfied: Boolean = false

    var codeText: String?
        get() {
            return findViewById<EditText>(R.id.ghost_input)?.text?.toString().orEmpty()
        }
        set(value) {
            findViewById<EditText>(R.id.ghost_input)?.setText(value)
        }

    private val inputMask: Boolean
    private var ghostInput: EditText? = null
    private var code1: EditText? = null
    private var code2: EditText? = null
    private var code3: EditText? = null
    private var code4: EditText? = null

    init {
        var typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CommonCodeInput,
            defStyle,
            0
        )
        var codeDigits: Int

        try {
            codeDigits = typedArray.getInt(R.styleable.CommonCodeInput_codeDigits, 4)
            inputMask = typedArray.getBoolean(R.styleable.CommonCodeInput_inputMask, false)
        } finally {
            typedArray.recycle()
        }

        LayoutInflater.from(context).inflate(R.layout.tw_common_code_input_4, this, true)


        ghostInput = findViewById(R.id.ghost_input)
        code1 = findViewById(R.id.code1)
        code2 = findViewById(R.id.code2)
        code3 = findViewById(R.id.code3)
        code4 = findViewById(R.id.code4)

        ghostInput?.apply {
            requestFocus()
            post {
                showKeyboard(this)
                setSelection(text.length)
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    //Ignore
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //Ignore
                }

                override fun afterTextChanged(s: Editable?) {
                    code1?.setText(getCode(0, s), TextView.BufferType.NORMAL)
                    code2?.setText(getCode(1, s), TextView.BufferType.NORMAL)
                    code3?.setText(getCode(2, s), TextView.BufferType.NORMAL)
                    code4?.setText(getCode(3, s), TextView.BufferType.NORMAL)

                    (s?.length == codeDigits).also { satisfied ->
                        if (satisfied) {
                            hideKeyboard(this@apply)
                            postDelayed(
                                {
                                    codeInputCompleteListener?.invoke(s.toString())
                                }, 100
                            )
                        }
                        isCodeInputSatisfied = satisfied
                        codeInputIdSatisfiedListener?.invoke(isCodeInputSatisfied)
                    }
                }
            })

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        actionDoneListener?.invoke() ?: hideKeyboard(this)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    fun init() {
        codeText = ""
        showKeyboard(findViewById(R.id.ghost_input))
    }

    private fun getCode(index: Int, s: Editable?): String {
        return if (s?.getOrNull(index)?.toString()?.isNotEmpty() == true) {
            if (inputMask) "*" else s[index].toString()
        } else {
            ""
        }
    }

    fun clear() {
        ghostInput?.setText("")
        code1?.setText((""), TextView.BufferType.NORMAL)
        code2?.setText((""), TextView.BufferType.NORMAL)
        code3?.setText((""), TextView.BufferType.NORMAL)
        code4?.setText((""), TextView.BufferType.NORMAL)
    }

    private fun showKeyboard(target: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).run {
            showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard(target: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).run {
            hideSoftInputFromWindow(target.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    fun setOnCodeInputCompleteListener(listener: (codeText: String) -> Unit) {
        this.codeInputCompleteListener = listener
    }

    fun setOnCodeInputSatisfiedListener(listener: (satisfied: Boolean) -> Unit) {
        this.codeInputIdSatisfiedListener = listener
    }

    fun setOnActionDoneListener(listener: () -> Unit) {
        this.actionDoneListener = listener
    }
}