package com.ramya.learnhub

import android.app.Application

class LearnHubApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
