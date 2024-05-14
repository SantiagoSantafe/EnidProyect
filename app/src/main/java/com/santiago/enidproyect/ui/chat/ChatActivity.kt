package com.santiago.enidproyect.ui.chat

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.santiago.enidproyect.R
import java.text.SimpleDateFormat


class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiveRoom: String? = null
    var senderRoom: String? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        mDbRef = FirebaseDatabase.getInstance().getReference()
        senderRoom = receiverUid + senderUid
        receiveRoom = senderUid + receiverUid
        supportActionBar?.title = name
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter
//logica data recycleview
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(postSnapshot in snapshot.children){
                        val Message = postSnapshot.getValue(Message::class.java)
                        messageList.add(Message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        sendButton.setOnClickListener {
            val message = messageBox.text.toString()
            val timestamp = Timestamp.now()
            val date = timestamp.toDate()
            val format = SimpleDateFormat("hh:mm")
            val formattedTime = format.format(date)
            val messageObject = Message(message,senderUid,formattedTime)
            mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnCompleteListener {
                    mDbRef.child("chats").child(receiveRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")
            chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }
}