package com.example.hangout

import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.example.hangout.databinding.AttachmentImgurBinding
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.InnerAttachmentViewHolder

class ImgurAttachmentFactory: AttachmentFactory {
    override fun canHandle(message: Message): Boolean {
        val imgurAttachment = message.attachments.firstOrNull { it.isImgurAttachment() }
        return imgurAttachment != null
    }

    override fun createViewHolder(
        message: Message,
        listeners: MessageListListenerContainer?,
        parent: ViewGroup
    ): InnerAttachmentViewHolder {
        val imgurAttachment = message.attachments.first() { it.isImgurAttachment() }
        val binding = AttachmentImgurBinding
            .inflate(LayoutInflater.from(parent.context), null, false)
        return ImgurAttachmentViewHolder(
            imgurAttachment = imgurAttachment,
            binding = binding
        )
    }
    private fun Attachment.isImgurAttachment(): Boolean = imageUrl?.contains("imgur") == true

    private class ImgurAttachmentViewHolder(
        binding: AttachmentImgurBinding,
        imgurAttachment: Attachment
    ) :
        InnerAttachmentViewHolder(binding.root) {

        init {
            binding.ivMediaThumb.apply {
                shapeAppearanceModel = shapeAppearanceModel
                    .toBuilder()
                    .setAllCornerSizes(resources.getDimension(io.getstream.chat.android.ui.R.dimen.stream_ui_selected_attachment_corner_radius))
                    .build()
                load(imgurAttachment.imageUrl) {
                    allowHardware(false)
                    crossfade(true)
                    placeholder(io.getstream.chat.android.ui.R.drawable.stream_ui_picture_placeholder)
                }
            }
        }
    }
}