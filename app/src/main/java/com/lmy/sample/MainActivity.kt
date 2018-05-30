/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.sample

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import com.lmy.codec.CameraPreviewPresenter
import com.lmy.codec.loge
import com.lmy.codec.texture.impl.filter.*
import com.lmy.codec.util.debug_v
import com.lmy.sample.helper.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var mPresenter: CameraPreviewPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        oneBar.setOnSeekBarChangeListener(this)
        twoBar.setOnSeekBarChangeListener(this)
        thBar.setOnSeekBarChangeListener(this)
        fBar.setOnSeekBarChangeListener(this)
        loge("Permission: " + !PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
        if (!PermissionHelper.requestPermissions(this, PermissionHelper.PERMISSIONS_BASE))
            return
        mPresenter = CameraPreviewPresenter(com.lmy.codec.entity.Parameter(this))
        val mTextureView = TextureView(this)
        mTextureContainer.addView(mTextureView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))
        mTextureView.keepScreenOn = true
        mTextureView.surfaceTextureListener = this
        mTextureView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mPresenter.encoder?.start()
                    mPresenter.audioEncoder?.start()
                }
                MotionEvent.ACTION_UP -> {
                    mPresenter.encoder?.pause()
                    mPresenter.audioEncoder?.pause()
                }
            }
            return@setOnTouchListener true
        }
        changeBtn.setOnClickListener({
            showFilterDialog()
        })
    }

    private fun showFilterDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Change filter")
            setItems(arrayOf("Normal", "Grey", "Beauty", "Pixelation", "Hue",
                    "Gamma", "Brightness", "Sepia")) { dialog, which ->
                choose(which)
                dialog.dismiss()
            }
        }.show()
    }

    private fun choose(which: Int) {
        when (which) {
            0 -> {
                mPresenter.setFilter(NormalFilter::class.java)
                show(0)
            }
            1 -> {
                mPresenter.setFilter(GreyFilter::class.java)
                show(0)
            }
            2 -> {
                mPresenter.setFilter(BeautyFilter::class.java)
                show(4)
                oneBar.progress = 0
                twoBar.progress = 50
                thBar.progress = 0
                fBar.progress = 50
            }
            3 -> {
                mPresenter.setFilter(PixelationFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            4 -> {
                mPresenter.setFilter(HueFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
            5 -> {
                mPresenter.setFilter(GammaFilter::class.java)
                show(1)
                oneBar.progress = 33
            }
            6 -> {
                mPresenter.setFilter(BrightnessFilter::class.java)
                show(1)
                oneBar.progress = 50
            }
            7 -> {
                mPresenter.setFilter(SepiaFilter::class.java)
                show(1)
                oneBar.progress = 0
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {
        mPresenter.updatePreview(p1, p2)
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        mPresenter.stopPreview()
        return true
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
        if (null != p0)
            mPresenter.startPreview(p0, p1, p2)
        debug_v("onSurfaceTextureAvailable")
    }

    private fun showPermissionsDialog() {
        AlertDialog.Builder(this)
                .setMessage("Please grant permission in the permission settings")
                .setNegativeButton("cancel", { dialog, which -> finish() })
                .setPositiveButton("enter", { dialog, which ->
                    PermissionHelper.gotoPermissionManager(this@MainActivity)
                    finish()
                })
                .show()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        mPresenter.getFilter()?.setValue(progressLayout.indexOfChild(seekBar), progress)
    }

    private fun show(count: Int) {
        oneBar.visibility = if (count > 0) View.VISIBLE else View.GONE
        twoBar.visibility = if (count > 1) View.VISIBLE else View.GONE
        thBar.visibility = if (count > 2) View.VISIBLE else View.GONE
        fBar.visibility = if (count > 3) View.VISIBLE else View.GONE
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (null == grantResults || grantResults.isEmpty()) return
        when (requestCode) {
            PermissionHelper.REQUEST_MY -> {
                if (PermissionHelper.checkGrantResults(grantResults)) {
                    initView()
                } else {
                    showPermissionsDialog()
                }
            }
        }
    }
}
