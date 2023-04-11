package com.example.spotifyclone.utils

open class Event<out T>(private val data:T) {

    var hasBeenHandeled=false
    private set

    fun getContentIfNotHandeled():T?{
        return if(hasBeenHandeled){
            null
        } else{
            hasBeenHandeled=true
            data
        }
    }

    fun peekContent()=data
}