package com.vtrixdigital.chatr.ui.activities

import android.animation.Animator
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.vtrixdigital.chatr.databinding.ActivitySplashBinding


class SplashActivity : AppCompatActivity() {

    private lateinit var springForce: SpringForce
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        Handler(Looper.getMainLooper()).postDelayed({
            springForce = SpringForce(0f)
            binding!!.relativeLayout.pivotX = 0f
            binding!!.relativeLayout.pivotY = 0f
            val springAnim =
                SpringAnimation(binding!!.relativeLayout, DynamicAnimation.ROTATION).apply {
                    springForce.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                    springForce.stiffness = SpringForce.STIFFNESS_VERY_LOW
                }
            springAnim.spring = springForce
            springAnim.setStartValue(80f)
            springAnim.addEndListener{ animation, canceled, value, velocity ->
                val height: Float
                val width: Int
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    height = windowManager.currentWindowMetrics.bounds.height().toFloat()
                    width = windowManager.currentWindowMetrics.bounds.width()
                } else {
                    @Suppress("DEPRECATION")
                    val display = windowManager.defaultDisplay
                    val size = Point()
                    display.getRealSize(size)
                    width  = size.x
                    height = size.y.toFloat()
                }
                binding!!.relativeLayout.animate()
                    .setStartDelay(1)
                    .translationXBy(width.toFloat() / 2)
                    .translationYBy(height)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(p0: Animator?) {

                        }

                        override fun onAnimationEnd(p0: Animator?) {
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            finish()
                            startActivity(intent)
                            overridePendingTransition(0, 0)
                        }

                        override fun onAnimationCancel(p0: Animator?) {

                        }

                        override fun onAnimationStart(p0: Animator?) {

                        }
                    })
                    .setInterpolator(DecelerateInterpolator(1f))
                    .start()
            }
            springAnim.start()
        }, 1000)
    }
}