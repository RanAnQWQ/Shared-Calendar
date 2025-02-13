package com.app.login.groupEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.app.login.MainActivity;
import com.app.login.R;
import com.app.login.SelectFriendActivity;
import com.app.login.dao.GroupEventDAO;
import com.app.login.dao.UserDao;
import com.app.login.entity.GroupEvent;
import com.app.login.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroupEventFragment extends Fragment {

    private ListView groupEventListView;
    private Button selectFriendButton;
    private GroupEventAdapter groupEventAdapter;
    private int currentUserId;

    public GroupEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_event, container, false);

        groupEventListView = rootView.findViewById(R.id.event_list_view);
        selectFriendButton = rootView.findViewById(R.id.select_friend_button);
        currentUserId = getCurrentUserId();
        Log.e("GroupEventFragment---loadGroupEvents","currentUserId："+currentUserId);
        // 初始加载用户相关的群体事件
        loadGroupEvents();

        selectFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectFriendActivity.class);
            startActivityForResult(intent, 1);
        });

        return rootView;
    }

    public  void onDestroy(){
        super.onDestroy();
        ((MainActivity) requireActivity()).hideToolbar();
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).hideToolbar();
        loadGroupEvents();
    }
    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity) requireActivity()).hideToolbar();
    }

    public void onDetach(){
        super.onDetach();
        ((MainActivity) requireActivity()).hideToolbar();
    }
    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).hideToolbar();
    }

    // 加载群体事件
    private void loadGroupEvents() {
        Log.e("loadGroupEvents","loadGroupEvents111111112222");

        // 使用 ExecutorService 异步加载用户相关的群体事件
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            GroupEventDAO groupEventDAO = new GroupEventDAO();
            List<GroupEvent> userRelatedEvents = groupEventDAO.getUserRelatedGroupEvents(currentUserId);

            // 更新 UI 线程
            getActivity().runOnUiThread(() -> {
                groupEventAdapter = new GroupEventAdapter(getActivity(), userRelatedEvents);
                groupEventListView.setAdapter(groupEventAdapter);
                groupEventListView.setOnItemClickListener((parent, view, position, id) -> {
                    GroupEvent selectedEvent = userRelatedEvents.get(position);
                    Intent intent = new Intent(getActivity(), GroupEventDetailsActivity.class);

                    intent.putExtra("eventId", selectedEvent.getId());
                    intent.putExtra("startDate", selectedEvent.getTentativeStartDate());  // 将 startDate 传递
                    Log.e("startDateQQQQQQQQQQQQQ"," "+selectedEvent.getTentativeStartDate());
                    intent.putExtra("endDate", selectedEvent.getTentativeEndDate());  // 将 endDate 传递
                    Log.e("endDateQQQQQQQQQQQQQQQ"," "+selectedEvent.getTentativeEndDate());
                    intent.putExtra("title",selectedEvent.getTitle());
                    intent.putExtra("description",selectedEvent.getDescription());

                    startActivity(intent);
                });
            });
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            // 重新加载群体事件列表
            loadGroupEvents();
        }
    }

    private int getCurrentUserId() {
        String accountName = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("currentUserId", null);

        if (accountName == null) {
            throw new IllegalStateException("Account name is not set in SharedPreferences");
        }

        final int[] userId = new int[1];
        // 使用Thread来进行异步操作
        Thread thread = new Thread(() -> {
            UserDao dao = new UserDao();
            User user = dao.findUser(accountName);
            if (user != null) {
                userId[0] = user.getId();  // 获取userId并存储
            } else {
                // 注意这里的修复：使用 getActivity().runOnUiThread() 来更新 UI
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show()
                );
                userId[0] = 0;  // 如果没有找到用户，返回0
            }
        });

        thread.start();  // 启动线程

        try {
            thread.join();  // 等待线程执行完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return userId[0];  // 返回获取到的userId
    }
}
