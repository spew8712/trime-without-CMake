package com.osfans.trime.util

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.osfans.trime.databinding.DialogLoadingBinding

fun createLoadingDialog(context: Context, textId: Int): ProgressDialog {
    @Suppress("DEPRECATION")
    return ProgressDialog(context).apply {
        setMessage(context.getText(textId))
        setCancelable(false)
    }
}
