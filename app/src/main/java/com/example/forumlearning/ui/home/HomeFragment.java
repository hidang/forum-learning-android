package com.example.forumlearning.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.forumlearning.DetailQuestion;
import com.example.forumlearning.ProfileUpdate;
import com.example.forumlearning.R;
import com.example.forumlearning.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        final String arr[] = {"Bảo là ai?", "Có bao nhiêu ngành đạo tạo tại UIT?", "Kỹ năng nào cần thiết cho lập trình viên?", "Những điều cần làm trước khi thức dậy?"};

        ListView lvQuestion = (ListView) view.findViewById(R.id.list_question);

        //3. Gán Data source vào ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_1, arr);

        //4. Đưa Data source vào ListView
        lvQuestion.setAdapter(adapter);

        //5. Thiết lập sự kiện cho Listview, khi chọn phần tử nào thì hiển thị lên TextView
        lvQuestion.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        //đối số arg2 là vị trí phần tử trong Data Source (arr)
                        //Toast.makeText(getActivity(), "dangne", Toast.LENGTH_SHORT).show();
                        Intent detailQuestion = new Intent(getActivity(), DetailQuestion.class);
                        startActivity(detailQuestion);
                    }
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}