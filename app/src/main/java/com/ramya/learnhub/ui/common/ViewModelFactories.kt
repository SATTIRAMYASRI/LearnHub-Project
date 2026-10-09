package com.ramya.learnhub.ui.common

import androidx.fragment.app.Fragment
import com.ramya.learnhub.AppContainer
import com.ramya.learnhub.LearnHubApp

val Fragment.appContainer: AppContainer
    get() = (requireActivity().application as LearnHubApp).container
