package com.midas.secretplace.structure.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel


class couple : ViewModel() {

    // Create a LiveData with a String
    val couple_key: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    // Rest of the ViewModel...
}