package com.github.ymkawb.anchored_list

import com.facebook.drawee.backends.pipeline.Fresco

/**
 * Created by nivanov on 10.03.2018.
 */
class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}