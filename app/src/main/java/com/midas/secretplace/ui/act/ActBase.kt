package com.midas.secretplace.ui.act

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/*
base activity
 */
abstract class ActBase <T: ViewDataBinding>:  AppCompatActivity(){
    lateinit var viewBinding: T
    abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, layoutResourceId)
    }
}