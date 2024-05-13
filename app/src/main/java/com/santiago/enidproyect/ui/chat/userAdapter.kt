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


        pathReference.downloadUrl.addOnSuccessListener { uri ->
            uri?.let {
                holder.imagenUsuario?.let { imageView ->
                    Glide.with(this.context)
                        .load(it.toString())
                        .into(imageView)
                } ?: run {
                    // Manejar el caso cuando el ImageView es nulo
                    Log.e("userAdapter", "El ImageView es nulo")
                }
            } ?: run {
                // Manejar el caso cuando la URI es nula (el archivo no existe)
                holder.imagenUsuario?.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }.addOnFailureListener { exception ->
            // Manejar el error de descarga
            Log.e("userAdapter", "Error al descargar la imagen: ${exception.message}")
            holder.imagenUsuario?.setImageResource(R.drawable.ic_profile_placeholder)
        }



        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name",currentUser.name)
            intent.putExtra("uid",currentUser.uid)
            context.startActivity(intent)
        }
    }
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textName = itemView.findViewById<TextView>(R.id.txt_name)
        val imagenUsuario =itemView.findViewById<ImageView>(R.id.img_user)
    }
}