package kr.ac.gachon.sw.mirrorassistant.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import kr.ac.gachon.sw.mirrorassistant.databinding.DialogLoadingBinding

class LoadingDialog(context: Context): Dialog(context) {
    private val viewBinding: DialogLoadingBinding

    init {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewBinding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}