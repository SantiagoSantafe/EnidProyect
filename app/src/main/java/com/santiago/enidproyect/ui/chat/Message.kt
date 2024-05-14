package com.santiago.enidproyect.ui.chat

import com.google.firebase.Timestamp


class Message {
    var message: String? = null
    var senderID: String? = null
    var time: String? = null
    constructor()

    constructor(message: String?, senderID: String?,time: String?){
        this.message=message
        this.senderID=senderID
        this.time =time
    }

}