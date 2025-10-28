package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.example.hashilink.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var otherUserId: String
    private lateinit var otherUserName: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var currentUserId: String
    private val TAG = "ChatFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Support both old parameter names (sellerId/sellerName) and new generic names (otherUserId/otherUserName)
        otherUserId = arguments?.getString("otherUserId") 
            ?: arguments?.getString("sellerId") ?: ""
        otherUserName = arguments?.getString("otherUserName") 
            ?: arguments?.getString("sellerName") ?: "User"
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        
        Log.d(TAG, "ChatFragment initialized - otherUserId: $otherUserId, currentUserId: $currentUserId")
        
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Current user ID is empty!")
        }
        if (otherUserId.isEmpty()) {
            Log.e(TAG, "Other user ID is empty!")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set other user name in toolbar if available
        view.findViewById<TextView>(R.id.tvChatTitle)?.text = otherUserName

        recyclerView = view.findViewById(R.id.rvMessages)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        adapter = MessageAdapter(messages, currentUserId)
        recyclerView.adapter = adapter

        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                etMessage.text.clear()
            }
        }

        loadMessages()
    }

    private fun loadMessages() {
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot load messages - currentUserId is empty")
            Toast.makeText(requireContext(), "Please log in to chat", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (otherUserId.isEmpty()) {
            Log.e(TAG, "Cannot load messages - otherUserId is empty")
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a chat room ID that's consistent for both users
        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        Log.d(TAG, "Loading messages for chat room: $chatRoomId")
        
        val messagesRef = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(chatRoomId)
            .child("messages")

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    Log.d(TAG, "Message received: ${message.text}")
                    messages.add(message)
                    adapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                } else {
                    Log.w(TAG, "Received null message from snapshot")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    val index = messages.indexOfFirst { it.messageId == message.messageId }
                    if (index != -1) {
                        messages[index] = message
                        adapter.notifyItemChanged(index)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load messages: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Failed to load messages: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(text: String) {
        if (currentUserId.isEmpty()) {
            Log.e(TAG, "Cannot send message - currentUserId is empty")
            Toast.makeText(requireContext(), "Please log in to send messages", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (otherUserId.isEmpty()) {
            Log.e(TAG, "Cannot send message - otherUserId is empty")
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val chatRoomId = getChatRoomId(currentUserId, otherUserId)
        Log.d(TAG, "Sending message to chat room: $chatRoomId")
        
        val database = FirebaseDatabase.getInstance()
        val messagesRef = database.getReference("chats")
            .child(chatRoomId)
            .child("messages")

        val messageId = messagesRef.push().key
        if (messageId == null) {
            Log.e(TAG, "Failed to generate message ID")
            Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = otherUserId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        messagesRef.child(messageId).setValue(message)
            .addOnSuccessListener {
                Log.d(TAG, "Message sent successfully")
                
                // Update last message info for chat history
                val chatInfoRef = database.getReference("chats").child(chatRoomId).child("info")
                val chatInfo = mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to message.timestamp,
                    "participants" to mapOf(
                        currentUserId to true,
                        otherUserId to true
                    )
                )
                chatInfoRef.updateChildren(chatInfo)

                // Create chat entry for both users in their chat lists
                database.getReference("userChats").child(currentUserId).child(otherUserId).setValue(chatRoomId)
                database.getReference("userChats").child(otherUserId).child(currentUserId).setValue(chatRoomId)
            }
            .addOnFailureListener { ex ->
                Log.e(TAG, "Failed to send message: ${ex.message}", ex)
                Toast.makeText(requireContext(), "Failed to send message: ${ex.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getChatRoomId(userId1: String, userId2: String): String {
        // Create consistent chat room ID regardless of who initiates
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    companion object {
        fun newInstance(otherUserId: String, otherUserName: String = "User"): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString("otherUserId", otherUserId)
            args.putString("otherUserName", otherUserName)
            // Also set old parameter names for backward compatibility
            args.putString("sellerId", otherUserId)
            args.putString("sellerName", otherUserName)
            fragment.arguments = args
            return fragment
        }
    }

    class MessageAdapter(
        private val messages: List<Message>,
        private val currentUserId: String
    ) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        private val VIEW_TYPE_SENT = 1
        private val VIEW_TYPE_RECEIVED = 2
        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
            val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        }

        override fun getItemViewType(position: Int): Int {
            val message = messages[position]
            return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val layoutId = if (viewType == VIEW_TYPE_SENT) {
                R.layout.item_message_sent
            } else {
                R.layout.item_message_received
            }
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            val message = messages[position]
            holder.tvMessage.text = message.text
            holder.tvTime.text = dateFormat.format(Date(message.timestamp))
        }

        override fun getItemCount() = messages.size
    }
}