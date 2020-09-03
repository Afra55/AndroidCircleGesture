package com.afra55.android_circlegesture

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress_circleview.listener = object :CircleListener{

            override fun progress(float: Float) {
                tv_progress.text = float.toString()
            }

        }
    }
}