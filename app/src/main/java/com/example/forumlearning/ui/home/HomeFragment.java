package com.example.forumlearning.ui.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forumlearning.CreateQuestionActivity;
import com.example.forumlearning.DetailQuestion;
import com.example.forumlearning.Question;
import com.example.forumlearning.QuestionAdapter;
import com.example.forumlearning.R;
import com.example.forumlearning.databinding.FragmentHomeBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private EditText edtQuestion;
    private Button btnCreateQuestion, btnSearchQustion;
    private RecyclerView rcQuestions;
    private QuestionAdapter mQuestionAdapter;
    private List<Question> mListQuestions;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

//        final String arr[] = {"B???o l?? ai?", "C?? bao nhi??u ng??nh ?????o t???o t???i UIT?", "K??? n??ng n??o c???n thi???t cho l???p tr??nh vi??n?", "Nh???ng ??i???u c???n l??m tr?????c khi th???c d???y?"};

//        ListView lvQuestion = (ListView) view.findViewById(R.id.list_question);

        rcQuestions = view.findViewById(R.id.rc_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rcQuestions.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        rcQuestions.addItemDecoration(dividerItemDecoration);

        mListQuestions = new ArrayList<>();
        mQuestionAdapter = new QuestionAdapter(mListQuestions, new QuestionAdapter.IClickListener() {
            @Override
            public void onClickItem(Question question) {
                Intent intent = new Intent(getActivity(), DetailQuestion.class);
                intent.putExtra("questionID", question.getId());
                startActivity(intent);
            }
        });
        rcQuestions.setAdapter(mQuestionAdapter);

        getListQuestionFromRealtimeDatabase();

//        //3. G??n Data source v??o ArrayAdapter
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>
//                (getActivity(), android.R.layout.simple_list_item_1, arr);
//        //4. ????a Data source v??o ListView
//        lvQuestion.setAdapter(adapter);
//        //5. Thi???t l???p s??? ki???n cho Listview, khi ch???n ph???n t??? n??o th?? hi???n th??? l??n TextView
//        lvQuestion.setOnItemClickListener(
//                new AdapterView.OnItemClickListener() {
//                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                        //?????i s??? arg2 l?? v??? tr?? ph???n t??? trong Data Source (arr)
//                        //Toast.makeText(getActivity(), "dangne", Toast.LENGTH_SHORT).show();
//                        Intent detailQuestion = new Intent(getActivity(), DetailQuestion.class);
//                        startActivity(detailQuestion);
//                    }
//                });

        btnCreateQuestion = view.findViewById(R.id.btn_create_question);
        btnCreateQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCreateQuestion();
            }
        });

        edtQuestion = view.findViewById(R.id.edt_question);
        btnSearchQustion = view.findViewById(R.id.btn_search_question);
        btnSearchQustion.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (edtQuestion.getText().toString().trim().equals("")) return;

                String searchInput = edtQuestion.getText().toString().trim();
                Predicate<Question> byTitle = ques -> !ques.getTitle().toLowerCase().contains(searchInput.toLowerCase());
//                List<Question> _m = mListQuestions;
                mListQuestions.removeAll(mListQuestions.stream().filter(byTitle)
                        .collect(Collectors.toList()));

                mQuestionAdapter.notifyDataSetChanged();
            }
        });

        edtQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")){
                    getListQuestionFromRealtimeDatabase();
                }
            }
        });

        return view;
    }

    private void getListQuestionFromRealtimeDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("questions");

        // My top posts by number of stars
        // Cach 1
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (mListQuestions != null) mListQuestions.clear();
//                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
//                    Question question = postSnapshot.getValue(Question.class);
//                    mListQuestions.add(question);
//                }
//                mQuestionAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getActivity(), "Get list questions faild", Toast.LENGTH_SHORT).show();
//            }
//        });

        // Cach 2
        Query query = myRef.orderByChild("time");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Question question = snapshot.getValue(Question.class);
                if (question != null) {
                    mListQuestions.add(0, question);
                }
                mQuestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Question question =  snapshot.getValue(Question.class);
                if (mListQuestions == null || mListQuestions.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListQuestions.size(); i++) {
                    if (question.getId().equals(mListQuestions.get(i).getId())) {
                        mListQuestions.set(i, question);
                        break;
                    }
                }

                mQuestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Question question =  snapshot.getValue(Question.class);
                if (mListQuestions == null || mListQuestions.isEmpty()) {
                    return;
                }
                for (int i=0; i< mListQuestions.size(); i++) {
                    if (question.getId().equals(mListQuestions.get(i).getId())) {
                        mListQuestions.remove(mListQuestions.get(i));
                        break;
                    }
                }

                mQuestionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onClickCreateQuestion() {
        Intent intent = new Intent(getActivity(), CreateQuestionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}