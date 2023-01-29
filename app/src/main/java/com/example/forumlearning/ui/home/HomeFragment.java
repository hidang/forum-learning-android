package com.example.forumlearning.ui.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forumlearning.CreateQuestionActivity;
import com.example.forumlearning.DetailQuestion;
import com.example.forumlearning.MyQuestion;
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
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private EditText edtQuestion;
    private Button btnCreateQuestion, btnMyQuestion;
    private ImageView btnSearchQuestion;
    private RecyclerView rcQuestions;
    private QuestionAdapter mQuestionAdapter;
    private List<Question> mListQuestions;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

//        final String arr[] = {"Bảo là ai?", "Có bao nhiêu ngành đạo tạo tại UIT?", "Kỹ năng nào cần thiết cho lập trình viên?", "Những điều cần làm trước khi thức dậy?"};

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

        btnMyQuestion = view.findViewById(R.id.btn_my_question);
        btnMyQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickMyQuestion();
            }
        });

//        //3. Gán Data source vào ArrayAdapter
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>
//                (getActivity(), android.R.layout.simple_list_item_1, arr);
//        //4. Đưa Data source vào ListView
//        lvQuestion.setAdapter(adapter);
//        //5. Thiết lập sự kiện cho Listview, khi chọn phần tử nào thì hiển thị lên TextView
//        lvQuestion.setOnItemClickListener(
//                new AdapterView.OnItemClickListener() {
//                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                        //đối số arg2 là vị trí phần tử trong Data Source (arr)
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
        btnSearchQuestion = view.findViewById(R.id.img_btn_search_question);
        btnSearchQuestion.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (edtQuestion.getText().toString().trim().equals("")) return;

                String searchInput = edtQuestion.getText().toString().trim();
                Predicate<Question> byTitle = ques -> !ques.getTitle().toLowerCase().contains(searchInput.toLowerCase());
//                List<Question> _m = mListQuestions;
                // step 1: filter by title if needed
                mListQuestions.removeAll(mListQuestions
                        .stream()
                        .filter(byTitle)
                        .collect(Collectors.toList())
                );
                // step 2: sort by date and vote scores
//                mListQuestions.sort(Comparator
//                        .comparingLong(Question::getTime)
//                        .comparingInt(Question::getVoteScores)
//                        .reversed()
//                );
                notifyDataSetChanged();
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
                if (s.toString().equals("")) {
                    getListQuestionFromRealtimeDatabase();
                }
            }
        });

        return view;
    }

    private void getListQuestionFromRealtimeDatabase() {
        mListQuestions.clear();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query myRef = database.getReference("questions").orderByChild("time");

        // Cach 1
        myRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mListQuestions != null) mListQuestions.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Question question = postSnapshot.getValue(Question.class);
                    mListQuestions.add(question);
                    List<Question> tempListQuestions = mListQuestions
                            .stream().sorted(
                                    Comparator
                                            .comparing((Question q) -> q.getVoteScores() <= question.getVoteScores() && q.getTime() < question.getTime())
                            )
                            .collect(Collectors.toList());
                    mListQuestions.clear();
                    mListQuestions.addAll(tempListQuestions);
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getActivity(), "Get list questions fail", Toast.LENGTH_SHORT).show();
            }
        });

        // Cach 2
//        Query query = myRef.orderByChild("time");
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Question question = snapshot.getValue(Question.class);
//                if (question != null) {
//                    mListQuestions.add(0, question);
//                }
//                notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Question question = snapshot.getValue(Question.class);
//                if (mListQuestions == null || mListQuestions.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < mListQuestions.size(); i++) {
//                    if (question.getId().equals(mListQuestions.get(i).getId())) {
//                        mListQuestions.set(i, question);
//                        break;
//                    }
//                }
//
//                notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                Question question = snapshot.getValue(Question.class);
//                if (mListQuestions == null || mListQuestions.isEmpty()) {
//                    return;
//                }
//                for (int i = 0; i < mListQuestions.size(); i++) {
//                    if (question.getId().equals(mListQuestions.get(i).getId())) {
//                        mListQuestions.remove(mListQuestions.get(i));
//                        break;
//                    }
//                }
//
//                notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void notifyDataSetChanged() {
//        mListQuestions.sort(Comparator
//                        .comparingLong(Question::getTime)
//                        .thenComparingInt(Question::getVoteScores)
//                        .reversed());
        mQuestionAdapter.notifyDataSetChanged();
    }

    private void onClickCreateQuestion() {
        Intent intent = new Intent(getActivity(), CreateQuestionActivity.class);
        startActivity(intent);
    }

    private void onClickMyQuestion() {
        Intent intent = new Intent(getActivity(), MyQuestion.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}