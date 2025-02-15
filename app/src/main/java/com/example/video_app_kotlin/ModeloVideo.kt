package com.example.video_app_kotlin

class ModeloVideo {

    var id : String ?= null
    var titulo : String ?= null
    var autor : String ?= null
    var videoUri : String ?= null
    var tiempo : String ?= null

    constructor()


    constructor(id: String?, titulo: String?, autor: String?, videoUri: String?, tiempo: String?) {
        this.id = id
        this.titulo = titulo
        this.autor = autor
        this.videoUri = videoUri
        this.tiempo = tiempo
    }


}