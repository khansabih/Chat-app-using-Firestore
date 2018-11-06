package com.example.sabih.updatingnewstart;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<chatAdapter.ViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private Context context;

    private static final int SENT = 0;
    private static final int RECEIVED = 1;
    private String userID;

    private List<chatModel> chatMsgs;
    private String title;

    public chatAdapter(List<chatModel> chatMsgs,String title,String userID) {
        this.chatMsgs = chatMsgs;
        this.title = title;
        this.userID = userID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        View view = null;
        if(viewType == SENT)
        {
           view = LayoutInflater.from(context).inflate(R.layout.chat_box_layout,parent,false);
        }
        if(viewType == RECEIVED)
        {
            view = LayoutInflater.from(context).inflate(R.layout.other_user_chat_box,parent,false);
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String getSenderId = chatMsgs.get(position).getSender();
        final String getSenderMessage = chatMsgs.get(position).getMessage();
        final String getID = chatMsgs.get(position).getId();
        final String theType = chatMsgs.get(position).getType();

        if(theType.equals("text"))
        {
            //To get name...
            mFirestore.collection("Users").document(getSenderId).get()
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String getFirstName = task.getResult().getString("Firstname");
                                String getLastName = task.getResult().getString("Lastname");

                                holder.mName.setText(getFirstName.charAt(0) + getFirstName.substring(1).toLowerCase());
                            }
                        }
                    });
            holder.mContent.setText(getSenderMessage);
        }

    }

    @Override
    public int getItemCount() {
        return chatMsgs.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMsgs.get(position).getSender() . equals(userID))
            return SENT;
        else
            return RECEIVED;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        //Variables for my chat lists...
        private TextView mName,mContent;
        private ImageView mDelete,mEdit;
        private RelativeLayout mBox;

        public ViewHolder(View itemView) {
            super(itemView);

            //variable initialization
            mName = (TextView)itemView.findViewById(R.id.name);
            mContent = (TextView)itemView.findViewById(R.id.chatContent);
            mDelete = (ImageView)itemView.findViewById(R.id.delete);
            mEdit = (ImageView)itemView.findViewById(R.id.editContent);
            mBox = (RelativeLayout)itemView.findViewById(R.id.userChatBox);

        }
    }
}
