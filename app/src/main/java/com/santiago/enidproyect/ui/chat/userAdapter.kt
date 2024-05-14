package com.santiago.enidproyect.ui.chat

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.santiago.enidproyect.R


class userAdapter(val context: Context, val userList: ArrayList<User>):
    RecyclerView.Adapter<userAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent,false)
        return UserViewHolder(view)
    }
    override fun getItemCount(): Int {
        return userList.size
    }
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.textName.text = currentUser.name

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://enidproyect.appspot.com")
        val pathReference = storageRef.child("images/${currentUser.email}.jpg")
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val receiverUid = currentUser.uid
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        var senderRoom = receiverUid + senderUid
        var receiveRoom = senderUid + receiverUid
        pathReference.downloadUrl.addOnSuccessListener { uri ->
            uri?.let {
                holder.imagenUser?.let { imageView ->
                    Glide.with(this.context)
                        .load(it.toString())
                        .into(imageView)
                } ?: run {
                    // Manejar el caso cuando el ImageView es nulo
                    Log.e("userAdapter", "El ImageView es nulo")
                }
            } ?: run {
                // Manejar el caso cuando la URI es nula (el archivo no existe)
                holder.imagenUser?.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }.addOnFailureListener { exception ->
            // Manejar el error de descarga
            Log.e("userAdapter", "Error al descargar la imagen: ${exception.message}")
            holder.imagenUser?.setImageResource(R.drawable.ic_profile_placeholder)
        }
        // Asegúrate de que la referencia de la sala sea consistente con la forma en que guardas los mensajes
        val messageRoomRef = mDbRef.child("chats").child(senderRoom).child("messages")

        messageRoomRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                val lastMessageSnapshot = dataSnapshot.children.last()

                val messageText = lastMessageSnapshot.child("message").value?.toString() ?: "No Content"
                holder.lastMessage.text = messageText

                val timeText = if (lastMessageSnapshot.hasChild("time")) {
                    lastMessageSnapshot.child("time").value?.toString() ?: "No Time"
                } else {
                    "No Time"
                }
                holder.time.text = timeText
            } else {
                holder.lastMessage.text = "No messages"
                holder.time.text = "No Time"
            }
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid",currentUser.uid)
            context.startActivity(intent)
        }
    }
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.txt_name) ?: throw IllegalStateException("TextView1 not found")
        val imagenUser: ImageView = itemView.findViewById(R.id.img_user) ?: throw IllegalStateException("ImageView not found")
        val lastMessage: TextView = itemView.findViewById(R.id.txt_last_message) ?: throw IllegalStateException("TextView2 not found")
        val time: TextView = itemView.findViewById(R.id.txt_time) ?: throw IllegalStateException("TextView3 not found")
    }


}