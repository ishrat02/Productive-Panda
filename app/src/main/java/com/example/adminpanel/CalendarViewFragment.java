package com.example.adminpanel;

import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.Nullable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CalendarViewFragment extends BottomSheetDialogFragment {
    private DatabaseReference databaseReference;
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ImageView backBtn;
    private CalendarView calendarView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar_view_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();//getting current logged in user
        backBtn = view.findViewById(R.id.Calendarback);
        calendarView = view.findViewById(R.id.calendarViewGet);
        databaseReference= FirebaseDatabase.getInstance().getReference("tasks").child(firebaseUser.getUid());//oi user er joto task ase apatoto recycler view te eigula fetch kortesi
        backBtn.setOnClickListener(v -> dismiss());
        calendarView.setOnDateChangeListener((calendarView, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
            //Toast.makeText(getContext(), "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();//ei toast er dorkar nai
            fetchEventsForDate(selectedDate,firebaseUser);
        });
    }

    private void fetchEventsForDate(String selectedDate, FirebaseUser firebaseUser) {
        if(firebaseUser==null){
            Toast.makeText(getContext(),"User not logged in",Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> events=new ArrayList<>();
                for(DataSnapshot taskSnapshot:snapshot.getChildren()){
                    String taskDate=taskSnapshot.child("date").getValue(String.class);
                    String taskEvent=taskSnapshot.child("event").getValue(String.class);
                    if(selectedDate.equals(taskDate)){
                        events.add(taskEvent);
                    }
                }
                if(events.isEmpty()){
                    Toast.makeText(getContext(),"No events on "+selectedDate,Toast.LENGTH_SHORT).show();
                }else{
                    StringBuilder eventsList=new StringBuilder("");
                    for(String event:events){
                        eventsList.append(event).append("\n");
                        Toast.makeText(getContext(),selectedDate+" : "+event,Toast.LENGTH_SHORT).show();
                    }
                    //Toast.makeText(getContext(),eventsList.toString(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),"Failed to fetch data",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
