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
val scGap : Float = 0.02f / beads
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val offsetFactor : Float = 4f
val strokeFactor : Int = 90

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBeadInLine(i : Int, w : Float, scale : Float, paint : Paint) {
    val offset : Float = w / offsetFactor
    val gap : Float = offset / beads
    val sc : Float = scale.sinify().divideScale(i, beads)
    save()
    translate(gap / 2 + (beads - 1 - i) * gap + (w - offset) * sc, 0f)
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
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(0f, gap * (i + 1))
    drawLine(0f, 0f, w, 0f, paint)
    drawBeadsInLine(w, scale, paint)
    restore()
}

class BeadsInLineView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BILNode(var i : Int, val state : State = State()) {

        private var next : BILNode? = null
        private var prev : BILNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BILNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBILNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BILNode {
            var curr : BILNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BeadsInLine(var i : Int) {

        private val root : BILNode = BILNode(0)
        private var curr : BILNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BeadsInLineView) {

        private val animator : Animator = Animator(view)
        private val bil : BeadsInLine = BeadsInLine(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bil.draw(canvas, paint)
            animator.animate {
                bil.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bil.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BeadsInLineView {
            var view : BeadsInLineView = BeadsInLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}