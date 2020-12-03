package com.plcoding.spotifyclone

open class Event<out T>(private val data: T) {//can inherit from it

    var hasBeenHandled = false//will triggered event
        private set //useful for snack bars


    fun getContentIFNotHandled(): T? {
        return if(hasBeenHandled){
            null
        }else{
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data


}