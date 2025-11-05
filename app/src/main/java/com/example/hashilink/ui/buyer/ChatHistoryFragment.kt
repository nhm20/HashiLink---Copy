package com.example.hashilink.ui.buyer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hashilink.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatHistoryAdapter
    private val chatList = mutableListOf<ChatInfo>()
    private val TAG = "ChatHistoryFragment"

    data class ChatInfo(
        val sellerId: String,
        val sellerName: String = "Seller",
        val lastMessage: String = "",
        val lastMessageTime: Long = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvChatHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatHistoryAdapter(chatList) { chatInfo ->
            // Open chat with seller
            Log.d(TAG, "Opening chat with seller: ${chatInfo.sellerId}")
            val chatFragment = ChatFragment.newInstance(chatInfo.sellerId, chatInfo.sellerName)
            (parentFragment as? BuyerHomeFragment)?.loadFragment(chatFragment)
        }
        recyclerView.adapter = adapter

        loadChatHistory()
    }

    private fun loadChatHistory() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "Current user is null")
            Toast.makeText(requireContext(), "Please log in to view chat history", Toast.LENGTH_SHORT).show()
            return
        }
        
        val currentUserId = currentUser.uid
        Log.d(TAG, "Loading chat history for user: $currentUserId")
        
        // Use userChats reference which maps userId -> otherUserId -> chatRoomId
        val userChatsRef = FirebaseDatabase.getInstance()
            .getReference("userChats")
            .child(currentUserId)

        userChatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Chat history data received, children count: ${snapshot.childrenCount}")
                chatList.clear()
                
                if (!snapshot.exists()) {
                    Log.d(TAG, "No chat history found")
                    adapter.notifyDataSetChanged()
                    return
                }
                
                for (child in snapshot.children) {
                    val sellerId = child.key ?: continue
                    val chatRoomId = child.getValue(String::class.java) ?: continue
                    
                    Log.d(TAG, "Found chat with seller: $sellerId, chatRoomId: $chatRoomId")
                    
                    // Fetch seller name and last message info
                    fetchSellerInfo(sellerId, chatRoomId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat history: ${error.message}", error.toException())
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Failed to load chat history: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchSellerInfo(sellerId: String, chatRoomId: String) {
        val database = FirebaseDatabase.getInstance()
        
        // Fetch seller name
        database.getReference("users").child(sellerId).get()
            .addOnSuccessListener { userSnapshot ->
                val sellerName = userSnapshot.child("name").getValue(String::class.java) ?: "Seller"
                
                // Fetch last message info
                database.getReference("chats").child(chatRoomId).child("info").get()
                    .addOnSuccessListener { infoSnapshot ->
                        val lastMessage = infoSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastMessageTime = infoSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0L
                        
                        val chatInfo = ChatInfo(sellerId, sellerName, lastMessage, lastMessageTime)
                        
                        // Check if already exists and update, otherwise add
                        val existingIndex = chatList.indexOfFirst { it.sellerId == sellerId }
                        if (existingIndex != -1) {
                            chatList[existingIndex] = chatInfo
                            adapter.notifyItemChanged(existingIndex)
                        } else {
                            chatList.add(chatInfo)
                            adapter.notifyItemInserted(chatList.size - 1)
                        }
                        
                        // Sort by last message time
                        chatList.sortByDescending { it.lastMessageTime }
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { ex ->
                        Log.e(TAG, "Failed to fetch chat info: ${ex.message}")
                        // Still add the chat even if we can't get the info
                        val chatInfo = ChatInfo(sellerId, sellerName)
                        if (chatList.none { it.sellerId == sellerId }) {
                            chatList.add(chatInfo)
                            adapter.notifyItemInserted(chatList.size - 1)
                        }
                    }
            }
            .addOnFailureListener { ex ->
                Log.e(TAG, "Failed to fetch seller info: ${ex.message}")
                // Still add the chat even if we can't get seller name
                val chatInfo = ChatInfo(sellerId)
                if (chatList.none { it.sellerId == sellerId }) {
                    chatList.add(chatInfo)
                    adapter.notifyItemInserted(chatList.size - 1)
                }
            }
    }

    inner class ChatHistoryAdapter(
        private val chats: List<ChatInfo>,
        private val onItemClick: (ChatInfo) -> Unit
    ) : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvSellerName: TextView = itemView.findViewById(R.id.tvSellerName)
            val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatInfo = chats[position]
            holder.tvSellerName.text = chatInfo.sellerName
            holder.tvLastMessage.text = chatInfo.lastMessage.ifEmpty { "No messages yet" }
            holder.itemView.setOnClickListener { onItemClick(chatInfo) }
        }

        override fun getItemCount() = chats.size
    }
}