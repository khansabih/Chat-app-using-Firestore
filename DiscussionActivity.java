package com.example.sabih.updatingnewstart;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DiscussionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabase;

    private EditText msg;
    private Button sendBtn;
    private ImageView addDocs;
    private Uri imageUri;
    private String url;

    private RecyclerView mChatContainer;
    private List<chatModel> chatList;
    private chatAdapter adapter;
    private int position = 0;
    Query first;
    DocumentSnapshot lastVisible;

    String getMessage, getTitle, getDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        msg = (EditText) findViewById(R.id.textContent);
        sendBtn = (Button) findViewById(R.id.sendMsg);
        addDocs = (ImageView) findViewById(R.id.include_documents);

        getTitle = getIntent().getExtras().getString("Title");
        int getCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        int getCurrentMonth = Calendar.getInstance().get(Calendar.MONTH);
        int getCurrentDate = Calendar.getInstance().get(Calendar.DATE);
        getDate = getCurrentDate + getCurrentMonth + getCurrentYear + "";

        mChatContainer = (RecyclerView) findViewById(R.id.chatContainer);
        chatList = new ArrayList<>();

        first = mFirestore.collection("Ideas").document(getTitle).collection("Discussions")
                .orderBy("Time", Query.Direction.ASCENDING);
        showMessage();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = msg.getText().toString().trim();

                int random = new Random().nextInt();
                Map chat = new HashMap();
                chat.put("Message", message);
                chat.put("sender", mAuth.getCurrentUser().getUid());
                chat.put("Time", FieldValue.serverTimestamp());
                chat.put("Type", "text");
                chat.put("id", String.valueOf(random));

                mFirestore.collection("Ideas").document(getTitle).collection("Discussions")
                        .add(chat).addOnCompleteListener(DiscussionActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            msg.setText("");
                        }
                    }
                });
                mChatContainer.smoothScrollToPosition(chatList.size() - 1);
            }
        });

        addDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discussionOptionsSheetDialog obj = new discussionOptionsSheetDialog();
                Bundle bundle = new Bundle();
                bundle.putString("Title", getTitle);
                bundle.putString("id", mAuth.getCurrentUser().getUid());
                obj.setArguments(bundle);
                obj.show(getSupportFragmentManager(), "discussionOptionsBottomSheet");
            }
        });

        adapter = new chatAdapter(chatList, getTitle,mAuth.getCurrentUser().getUid());
        mChatContainer.setHasFixedSize(true);
        mChatContainer.setLayoutManager(new LinearLayoutManager(DiscussionActivity.this));
        mChatContainer.setAdapter(adapter);

    }

    public void showMessage() {
        first.addSnapshotListener(DiscussionActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments()
                            .get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            chatModel obj = doc.getDocument().toObject(chatModel.class);
                            chatList.add(obj);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });
    }

}
