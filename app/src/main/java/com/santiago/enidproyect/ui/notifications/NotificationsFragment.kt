package com.santiago.enidproyect.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.santiago.enidproyect.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    lateinit var auth: FirebaseAuth
    val storageRef = Firebase.storage.reference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val db =FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val email = user?.email.toString()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)


        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = FirebaseAuth.getInstance()


        val textUserName: TextView = binding.textUserName
        val textUserEmail: TextView = binding.textUserEmail
        val userPhoto: ImageView = binding.profileImage
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            userPhoto.setOnClickListener {
                actualizarImagen(userPhoto)
                Toast.makeText(this.context, "Cambiar imagen de perfil", Toast.LENGTH_SHORT).show()
            }
            textUserName.text = user?.displayName.toString()
            textUserEmail.text = email
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun actualizarImagen(userPhoto: ImageView){
        val pathReference = storageRef.child("images/${email}.jpg")
        db.collection("contactos").document(email).get()
            .addOnSuccessListener {
                pathReference.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this /* context */)
                        .load(uri.toString())
                        .into(userPhoto)
                }.addOnFailureListener {
                    Toast.makeText(this.context, "aun no tiene imagen de usuario bro", Toast.LENGTH_SHORT).show()
                }
                if (!it.exists()) {
                    Toast.makeText(this.context, "Ingrese sus datos! Porfis ", Toast.LENGTH_SHORT).show()
                }
            }
    }
}