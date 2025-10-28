package com.example.hashilink.ui.seller

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
import com.example.hashilink.ui.buyer.ChatFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SellerChatHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SellerChatHistoryAdapter
    private val chatList = mutableListOf<ChatInfo>()
    private val TAG = "SellerChatHistory"

    data class ChatInfo(
        val buyerId: String,
        val buyerName: String = "Buyer",
        val lastMessage: String = "",
        val lastMessageTime: Long = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_seller_chat_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvSellerChatHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SellerChatHistoryAdapter(chatList) { chatInfo ->
            // Open chat with buyer
            Log.d(TAG, "Opening chat with buyer: ${chatInfo.buyerId}")
            val chatFragment = ChatFragment.newInstance(chatInfo.buyerId, chatInfo.buyerName)
            (parentFragment as? SellerHomeFragment)?.loadChildFragment(chatFragment)
        }
        recyclerView.adapter = adapter

        loadChatHistory()
    }

    override fun onResume() {
        super.onResume()
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
        Log.d(TAG, "Loading chat history for seller: $currentUserId")

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
                    val buyerId = child.key ?: continue
                    val chatRoomId = child.getValue(String::class.java) ?: continue

                    Log.d(TAG, "Found chat with buyer: $buyerId, chatRoomId: $chatRoomId")

                    // Fetch buyer name and last message info
                    fetchBuyerInfo(buyerId, chatRoomId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat history: ${error.message}", error.toException())
                Toast.makeText(requireContext(), "Failed to load chat history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun fetchBuyerInfo(buyerId: String, chatRoomId: String) {
        val database = FirebaseDatabase.getInstance()

        // Fetch buyer name
        database.getReference("users").child(buyerId).get()
            .addOnSuccessListener { userSnapshot ->
                val buyerName = userSnapshot.child("name").getValue(String::class.java) ?: "Buyer"

                // Fetch last message info
                database.getReference("chats").child(chatRoomId).child("info").get()
                    .addOnSuccessListener { infoSnapshot ->
                        val lastMessage = infoSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastMessageTime = infoSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0L

                        val chatInfo = ChatInfo(buyerId, buyerName, lastMessage, lastMessageTime)

                        // Check if already exists and update, otherwise add
                        val existingIndex = chatList.indexOfFirst { it.buyerId == buyerId }
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
                        val chatInfo = ChatInfo(buyerId, buyerName)
                        if (chatList.none { it.buyerId == buyerId }) {
                            chatList.add(chatInfo)
                            adapter.notifyItemInserted(chatList.size - 1)
                        }
                    }
            }
            .addOnFailureListener { ex ->
                Log.e(TAG, "Failed to fetch buyer info: ${ex.message}")
                // Still add the chat even if we can't get buyer name
                val chatInfo = ChatInfo(buyerId)
                if (chatList.none { it.buyerId == buyerId }) {
                    chatList.add(chatInfo)
                    adapter.notifyItemInserted(chatList.size - 1)
                }
            }
    }

    inner class SellerChatHistoryAdapter(
        private val chats: List<ChatInfo>,
        private val onItemClick: (ChatInfo) -> Unit
    ) : RecyclerView.Adapter<SellerChatHistoryAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvBuyerName: TextView = itemView.findViewById(R.id.tvBuyerName)
            val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seller_chat_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatInfo = chats[position]
            holder.tvBuyerName.text = chatInfo.buyerName
            holder.tvLastMessage.text = chatInfo.lastMessage.ifEmpty { "No messages yet" }
            holder.itemView.setOnClickListener { onItemClick(chatInfo) }
        }

        override fun getItemCount() = chats.size
    }
}