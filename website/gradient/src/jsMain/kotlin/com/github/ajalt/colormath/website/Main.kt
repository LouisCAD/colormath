@file:NoLiveLiterals // work around some compose bug

package com.github.ajalt.colormath.website

import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.ajalt.colormath.CssColors
import com.github.ajalt.colormath.RGB
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.borderRadius
import org.jetbrains.compose.web.css.margin
import org.jetbrains.compose.web.css.marginTop
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.events.WrappedEvent
import org.jetbrains.compose.web.renderComposable
import org.khronos.webgl.Uint8ClampedArray
import org.w3c.dom.HTMLCanvasElement

fun main() {
    var color1: RGB by mutableStateOf(CssColors.rebeccapurple)
    var color2: RGB by mutableStateOf(CssColors.aliceblue)

    renderComposable(rootElementId = "root") {
        Div(attrs = { style { margin(16.px) } }) {
            Div {
                Input(InputType.Color, value = color1.toHex(), attrs = {
                    onChange { color1 = RGB(it.stringValue) }
                })
                Input(InputType.Color, value = color2.toHex(), attrs = {
                    onChange { color2 = RGB(it.stringValue) }
                })
            }

            Div {
                Canvas(attrs = {
                    this.attr("width", "800")
                    this.attr("height", "150")

                    style {
                        marginTop(16.px)
                        borderRadius(4.px)
                    }
                }) {
                    DomSideEffect(color1 to color2) { updateCanvas(it, color1, color2) }
                }
            }
        }
    }
}

private fun updateCanvas(canvas: HTMLCanvasElement, color1: RGB, color2: RGB) {
    canvas.edit2dImageData {
        repeat(width) { x ->
            repeat(height) { y ->
                data.setColor(x, y, width, color1.blend(color2, x / width.toFloat()))
            }
        }
    }
}

private fun Uint8ClampedArray.setColor(x: Int, y: Int, width: Int, rgb: RGB) {
    val i = (y * width + x) * 4
    with(asDynamic()) {
        this[i] = rgb.redInt
        this[i + 1] = rgb.greenInt
        this[i + 2] = rgb.blueInt
        this[i + 3] = rgb.alphaInt
    }
}

private val WrappedEvent.stringValue get() = nativeEvent.target.asDynamic().value as String