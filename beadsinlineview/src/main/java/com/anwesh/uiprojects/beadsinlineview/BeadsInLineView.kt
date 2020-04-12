package com.anwesh.uiprojects.beadsinlineview

/**
 * Created by anweshmishra on 13/04/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val beads : Int = 3
val scGap : Float = 0.02f
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val offsetFactor : Float = 4f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBeadInLine(i : Int, w : Float, scale : Float, paint : Paint) {
    val offset : Float = w / offsetFactor
    val gap : Float = offset / beads
    val sc : Float = scale.sinify().divideScale(i, beads)
    save()
    translate(gap / 2 + (beads - 1 - i) * gap + (w - offsetFactor) * sc, 0f)
    drawCircle(0f, 0f, gap / 2, paint)
    restore()
}

fun Canvas.drawBeadsInLine(w : Float, scale : Float, paint : Paint) {
    for (j in 0..(beads - 1)) {
        drawBeadInLine(j, w, scale, paint)
    }
}

fun Canvas.drawBILNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    paint.color = foreColor
    save()
    translate(0f, gap * (i + 1))
    drawBeadsInLine(w, scale, paint)
    restore()
}

class BeadsInLineView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}