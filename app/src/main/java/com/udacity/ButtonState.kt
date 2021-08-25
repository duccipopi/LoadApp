package com.udacity


sealed class ButtonState(val clickable: Boolean, val animate: Boolean, val textId: Int) {
    object Clicked : ButtonState(false, true, R.string.button_name)
    object Loading : ButtonState(false, false, R.string.button_loading)
    object Completed : ButtonState(true, false, R.string.button_name)
}