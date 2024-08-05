package com.example.teabot;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private List<ChatMessage> chatMessages;
    private Context context;

    public ChatMessageAdapter(List<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_item, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);

        if (chatMessage.getImageUri() != null) {
            holder.chatImage.setVisibility(View.VISIBLE);
            holder.chatImage.setImageURI(chatMessage.getImageUri());
        } else {
            holder.chatImage.setVisibility(View.GONE);
        }

        holder.chatText.setText(chatMessage.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        ImageView chatImage;
        TextView chatText;

        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImage = itemView.findViewById(R.id.chat_image);
            chatText = itemView.findViewById(R.id.chat_text);
        }
    }
}
