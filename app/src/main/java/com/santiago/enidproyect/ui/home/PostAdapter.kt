package com.santiago.enidproyect.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.santiago.enidproyect.R

class PostAdapter (val context: Context, val postList: ArrayList<Post>):
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    val storageRef = Firebase.storage.reference
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view: View = LayoutInflater.from(context).inflate(R.layout.post_layout, parent,false)
            return PostViewHolder(view)
        }
        override fun getItemCount(): Int {
            return postList.size
        }
        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val currentPost = postList[position]
            holder.postImage.visibility = View.GONE
            holder.textName.text = currentPost.name
            holder.fechaPost.text = currentPost.date
            holder.textPost.text = currentPost.post
            storageRef.child("users_profile_imges/${currentPost.email}.jpg").downloadUrl.addOnSuccessListener { uri ->
                Glide.with(context)
                    .load(uri)
                    .into(holder.foto_perfil)
            }
            if (currentPost.post_image!=""){
                storageRef.child("posts_images/${currentPost.post_image}").downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(context)
                        .load(uri)
                        .into(holder.postImage)
                }
                holder.postImage.visibility = View.VISIBLE
                //Glide.with(context).load(currentPost.foto_perfil).into(holder.foto_perfil);
                //binding.textViewInformacion.text=arrayList[position].Correo
            }
        }
        class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val textName = itemView.findViewById<TextView>(R.id.user_name)
            val foto_perfil = itemView.findViewById<ImageView>(R.id.profile_image)
            val fechaPost = itemView.findViewById<TextView>(R.id.date_of_post)
            val textPost = itemView.findViewById<TextView>(R.id.body_post)
            val postImage = itemView.findViewById<ImageView>(R.id.post_image_body)

        }
    }