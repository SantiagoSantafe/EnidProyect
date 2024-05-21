package com.santiago.enidproyect.ui.home

class Post {
    var name: String? = null
    var email: String? = null
    var uid_usuario: String? = null
    var foto_perfil: String? = null
    var post: String? = null
    var post_image: String? = null
    var date: String? = null

    constructor(){}

    constructor(name: String?, email: String?, uid: String?, foto_perfil: String?, post: String?, post_image: String?, date: String?){
        this.name = name
        this.email = email
        this.foto_perfil = foto_perfil
        this.uid_usuario = uid
        this.post = post
        this.post_image = post_image
        this.date = date
    }

}