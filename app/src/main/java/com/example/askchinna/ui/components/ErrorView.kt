package com.example.askchinna.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.askchinna.R
import com.example.askchinna.databinding.ViewErrorBinding

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewErrorBinding.inflate(LayoutInflater.from(context), this, true)
    private var retryClickListener: (() -> Unit)? = null

    init {
        binding.buttonRetry.setOnClickListener {
            retryClickListener?.invoke()
        }
    }

    fun setError(message: String) {
        binding.textErrorMessage.text = message
    }

    fun setRetryClickListener(listener: () -> Unit) {
        retryClickListener = listener
    }
} 