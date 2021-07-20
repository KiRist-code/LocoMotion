package me.aflak.bluetoothterminal;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class result extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    String dbName;
    String whoami;

    TextView whoami_text;

    public static class Post {
        public int result;

        public Post() {}

        public Post(int result){
            this.result = result;
        }

        public int getResult(){return result;}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbName = getIntent().getStringExtra("dbName");
        whoami_text = (TextView)findViewById(R.id.whoami);

        // Get a reference to our posts
        databaseReference.child("entry").child(dbName).child("result").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    switch(Integer.parseInt(String.valueOf(task.getResult().getValue()))){
                        case 0:
                            whoami = "정원";
                            whoami_text.setText(whoami + "님 환영합니다!");
                            break;
                        case 1:
                            whoami = "김민준";
                            whoami_text.setText(whoami + "님 환영합니다!");
                            break;
                        case 2:
                            whoami = "고수완";
                            whoami_text.setText(whoami + "님 환영합니다!");
                            break;
                        default:
                            break;
                    }
                }
            }
        });

    }
}
