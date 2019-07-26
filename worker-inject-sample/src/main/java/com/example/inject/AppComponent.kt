package com.example.inject

import com.example.App
import dagger.Component

@Component(modules = [AssistModule::class])
interface AppComponent {
    fun inject(target: App)
}

