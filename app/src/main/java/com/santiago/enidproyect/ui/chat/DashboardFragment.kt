package com.santiago.enidproyect.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.santiago.enidproyect.R
import com.santiago.enidproyect.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: userAdapter
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var search_btn: ImageView
    private lateinit var search_bar: EditText


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)


        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root


        search_btn = requireActivity().findViewById(R.id.search_btn)
        search_bar = requireActivity().findViewById(R.id.search_bar)


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Este método se llama antes de que se realice un cambio de texto.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Este método se llama mientras se realiza un cambio de texto.
            }

            override fun afterTextChanged(s: Editable?) {
                // Este método se llama después de que se haya realizado un cambio de texto.
                val nuevoTexto = s.toString()
                filterUsers(nuevoTexto)
            }
        }

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference()
        userList = ArrayList()
        val context = this.requireContext()
        adapter = userAdapter(context, userList)
        userRecyclerView = _binding!!.userRecycler
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.adapter = adapter
        search_btn.setOnClickListener {
            if (search_bar.visibility == View.VISIBLE) {
                search_bar.visibility = View.GONE
                search_bar.setText("")
            } else {
                search_bar.visibility = View.VISIBLE
                search_bar.addTextChangedListener(textWatcher)
            }
        }
        //Aqui tengo que añadir la logica para
        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for(postsnapshot in snapshot.children){
                    val currentUser = postsnapshot.getValue(User::class.java)
                    if(mAuth.currentUser?.uid != currentUser?.uid){
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun filterUsers(texto: String) {
        mDbRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postsnapshot in snapshot.children) {
                    val currentUser = postsnapshot.getValue(User::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        // Add user to list if not the current user
                        userList.add(currentUser!!)
                    }
                }
                val filteredList = userList.toList()
                    .filter { usuario -> usuario.name?.contains(texto, ignoreCase = true) ?: false }
                adapter.update(filteredList)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }
}