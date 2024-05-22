package com.santiago.enidproyect.ui.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.santiago.enidproyect.R
import com.santiago.enidproyect.databinding.FragmentHomeBinding

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sentPost: ImageView
    private lateinit var textPost: MultiAutoCompleteTextView
    private lateinit var picture_btn: ImageView
    private lateinit var profile_image: ImageView
    private lateinit var img: ImageView


    private lateinit var userRecyclerView: RecyclerView
    private lateinit var postList: ArrayList<Post>
    private lateinit var adapter: PostAdapter

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference
    private lateinit var takePictureLauncher: ActivityResultLauncher<Void?>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            //poner la imagen en el imageview
            img.setImageBitmap(bitmap)
            img.visibility = View.VISIBLE
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                img.setImageURI(it)
                img.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mAuth = FirebaseAuth.getInstance()
        storageRef = Firebase.storage.reference

        initializeViews(root)
        setClickListeners()
        initializeRecyclerView()
        loadProfileImage()
        retrievePostsFromDatabase()

        return root
    }

    private fun initializeViews(root: View) {
        profile_image = root.findViewById(R.id.profile_image)
        sentPost = root.findViewById(R.id.sent_btn)
        textPost = root.findViewById(R.id.multiAutoCompleteTextView)
        picture_btn = root.findViewById(R.id.camara_btn)
        img = root.findViewById(R.id.img_camara)
        img.visibility = View.GONE
    }

    private fun setClickListeners() {
        picture_btn.setOnClickListener {
            showImageOptionsDialog()
        }
        sentPost.setOnClickListener {
            sendPost()
        }
    }

    private fun initializeRecyclerView() {
        postList = ArrayList()
        adapter = PostAdapter(requireContext(), postList)
        userRecyclerView = binding.root.findViewById(R.id.postRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter
    }

    private fun loadProfileImage() {
        storageRef.child("images/${mAuth.currentUser?.email}.jpg").downloadUrl.addOnSuccessListener { uri ->
            Glide.with(requireContext())
                .load(uri)
                .into(profile_image)
        }
    }

    private fun retrievePostsFromDatabase() {
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child("post").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPosts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val currentPost = postSnapshot.getValue(Post::class.java)
                    newPosts.add(0, currentPost!!)
                }
                postList.clear()
                postList.addAll(newPosts)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Imprimir el error en la consola para depuración
                Log.e("DatabaseError", error.message)

                // Mostrar un mensaje al usuario
                Toast.makeText(context, "Ha ocurrido un error al recuperar los datos: ${error.message}", Toast.LENGTH_LONG).show()
            }

        })
    }


    private fun showImageOptionsDialog() {
        val options = arrayOf("Tomar foto", "Elegir una foto")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Elige una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePictureLauncher.launch(null)
                1 -> pickImageLauncher.launch("image/*")
            }
        }
        builder.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendPost() {
        val timestamp = Timestamp.now()
        val date = timestamp.toDate()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
        val formattedTime = format.format(date)
        val formattedTimeForFirebase = formattedTime.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
        var name_file_post_picture = ""
        if (img.visibility == View.VISIBLE) {
            name_file_post_picture = "${mAuth.currentUser?.email}_${formattedTimeForFirebase}.jpg"
            uploadImageToFirebaseStorage(name_file_post_picture)
        }
        if (textPost.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "No puedes enviar un post vacío", Toast.LENGTH_SHORT)
                .show()
        } else {
            getUserName { name ->
                addPostToDatabase(
                    name!!,
                    mAuth.currentUser?.email.toString(),
                    mAuth.currentUser?.uid!!,
                    "foto_perfil",
                    textPost.text.toString(),
                    name_file_post_picture,
                    formattedTimeForFirebase
                )
                img.visibility = View.GONE
                img.setImageBitmap(null)
                textPost.text.clear()
            }
        }
    }

    private fun getUserName(callback: (String?) -> Unit) {
        val user = mAuth.currentUser
        val name = user?.displayName
        callback(name)
    }

    private fun addPostToDatabase(
        name: String, email: String, uid: String,
        foto_perfil: String, post: String,
        foto_post: String, date: String
    ) {
        mDbRef.child("post").child(date)
            .setValue(Post(name, email, uid, foto_perfil, post, foto_post, date))
    }

    private fun uploadImageToFirebaseStorage(file_name: String) {
        val imageRef = storageRef.child("posts_images/$file_name")
        val bitmap = (img.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(requireContext(), "No se pudo subir la imagen", Toast.LENGTH_SHORT)
                .show()
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Se subió la foto", Toast.LENGTH_SHORT).show()
        }
    }
}