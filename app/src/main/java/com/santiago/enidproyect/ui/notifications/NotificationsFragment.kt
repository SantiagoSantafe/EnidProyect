package com.santiago.enidproyect.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.santiago.enidproyect.LogIn
import com.santiago.enidproyect.databinding.FragmentNotificationsBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private val storageRef = Firebase.storage.reference
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email ?: "defaultEmail"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.textUserName.text = user?.displayName ?: "Anónimo"
        binding.textUserEmail.text = email

        binding.profileImage.setOnClickListener {
            Toast.makeText(context, "Cambiar imagen de perfil", Toast.LENGTH_SHORT).show()
            actualizeImage(binding.profileImage)
        }
        binding.buttonLogout.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LogIn::class.java)
                    startActivity(intent)
                    activity?.finish() // Cierra la actividad actual para evitar que el usuario regrese al presionar el botón Atrás
                } else {
                    // Manejo del error de cierre de sesión
                    Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun actualizeImage(userPhoto: ImageView) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "defaultEmail"
        val pathReference = storageRef.child("images/${email}.jpg")

        db.collection("contactos").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    pathReference.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this).load(uri.toString()).into(userPhoto)
                    }.addOnFailureListener {
                        Toast.makeText(context, "Aún no tiene imagen de usuario bro", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Ingrese sus datos! Porfis ", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
