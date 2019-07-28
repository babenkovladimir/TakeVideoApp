package com.babenkovladimir.cameraX.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babenkovladimir.R
import com.babenkovladimir.cameraX.main.MainActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        YoYo.with(Techniques.FadeIn)
            .duration(1200)
            .onEnd { playSecondAnimation() }
            .playOn(ivLogo)
    }

    private fun playSecondAnimation() {
        YoYo.with(Techniques.FadeOut)
            .onEnd {
                startActivity(Intent(this, MainActivity::class.java))
            }
            .duration(800)
            .playOn(ivLogo)
    }
}
