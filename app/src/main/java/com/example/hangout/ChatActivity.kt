package com.example.hangout

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import com.example.hangout.databinding.ActivityChatBinding
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val cid = checkNotNull(intent.getStringExtra(CID_KEY)) {
            "Specifying a channel id is required when starting ChannelActivity"
        }
//        Create three separate ViewModels for the views so it's easy
        //          to customize them individually
        val factory=MessageListViewModelFactory(this,cid)
        val messageListHeaderViewModel:MessageListHeaderViewModel by viewModels { factory}
        val messageListViewModel:MessageListViewModel by viewModels {factory}
        val messageComposerViewModel:MessageComposerViewModel by viewModels { factory }

        val imgurAttachmentViewFactory = ImgurAttachmentFactory()
        val attachmentViewFactory = AttachmentFactoryManager(listOf(imgurAttachmentViewFactory))
        binding.messageListView.setAttachmentFactoryManager(attachmentViewFactory)

        //binding view with viewmodels
        messageListHeaderViewModel.bindView(binding.messageListHeaderView,this)
        messageListViewModel.bindView(binding.messageListView,this)
        messageComposerViewModel.bindView(binding.messageComposerView,this)
        //working with threads
        //for messageComposer and messageListView
        messageListViewModel.mode.observe(this){
            mode ->
            when(mode){
                is MessageMode.MessageThread -> {
                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                }
                is MessageMode.Normal -> {
                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                }
            }
        }
        //message editing
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }
        //handle navigate up state
        messageListViewModel.state.observe(this){ state ->
            if (state is MessageListViewModel.State.NavigateUp){
                finish()
            }
        }
        //handling back button when we are in the thread
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        binding.messageListHeaderView.setBackButtonClickListener(backHandler)
        onBackPressedDispatcher.addCallback(this) {
            backHandler()
        }
    }

    companion object {
        private const val CID_KEY = "key:cid"
        fun newIntent(context: Context, channel: io.getstream.chat.android.models.Channel): Intent =
            Intent(context, ChatActivity::class.java).putExtra(CID_KEY, channel.cid)
    }
}