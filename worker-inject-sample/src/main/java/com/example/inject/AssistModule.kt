package com.example.inject

import com.vikingsen.inject.worker.WorkerModule
import dagger.Module

@WorkerModule
@Module(includes = [WorkerInject_AssistModule::class])
class AssistModule