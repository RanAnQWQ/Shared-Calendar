package com.app.login.groupEvent;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.app.login.R;
import com.app.login.dao.GroupEventDAO;
import com.app.login.dao.GroupEventParticipantDAO;
import com.app.login.entity.GroupEvent;

import java.util.List;

public class GroupEventAdapter extends BaseAdapter {

    private Context context;  // 上下文
    private List<GroupEvent> events;  // 集体事件列表
    private LayoutInflater inflater;  // 用于加载视图的 LayoutInflater
    private GroupEventDAO groupEventDAO;  // DAO 用于操作数据库
    private GroupEventParticipantDAO groupEventParticipantDAO;
    private static final String TAG = "GroupEventAdapter";

    // 构造函数，接受上下文和事件列表
    public GroupEventAdapter(Context context, List<GroupEvent> events) {
        this.context = context;
        this.events = events;
        this.inflater = LayoutInflater.from(context);  // 从上下文中获取 LayoutInflater
        this.groupEventDAO = new GroupEventDAO();  // 初始化 DAO
        this.groupEventParticipantDAO = new GroupEventParticipantDAO();  // 初始化 DAO
    }

    @Override
    public int getCount() {
        return events.size();  // 返回事件列表的大小
    }

    @Override
    public GroupEvent getItem(int position) {
        return events.get(position);  // 获取指定位置的事件
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();  // 获取事件的 ID 作为项 ID
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 如果 convertView 为 null，则加载一个新的视图
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);  // 加载列表项布局
        }

        // 获取当前事件对象
        GroupEvent event = getItem(position);

        // 查找布局中的 TextView 控件
        TextView titleTextView = convertView.findViewById(R.id.event_title);
        TextView statusTextView = convertView.findViewById(R.id.event_status);

        // 设置标题
        titleTextView.setText(event.getTitle());

        // 异步检查和更新状态
        //new UpdateEventStatusTask(event, statusTextView).execute();
        statusTextView.setText(event.getStatus());
        // 返回设置好的视图
        return convertView;
    }

    /**
     * 异步任务，用于检查和更新事件的状态
     */
    private class UpdateEventStatusTask extends AsyncTask<Void, Void, String> {
        private GroupEvent event;
        private TextView statusTextView;

        public UpdateEventStatusTask(GroupEvent event, TextView statusTextView) {
            this.event = event;
            this.statusTextView = statusTextView;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // 检查是否所有参与者都提交了期望时间段
                boolean allPreferredTimeSelected = groupEventParticipantDAO.canVote(event.getId());
                if (allPreferredTimeSelected && event.getStatus().equals("ONGOING")) {
                    // 检查是否所有参与者都完成了投票
                    boolean allVoted = groupEventParticipantDAO.allParticipantsVoted(event.getId());
                    if (allVoted) {
                        // 更新事件状态为 COMPLETED
                        groupEventDAO.updateEventStatus(event.getId(), "COMPLETED");
                        event.setStatus("COMPLETED");
                        return "COMPLETED";
                    }
                }
                return event.getStatus();  // 如果未更新，则返回当前状态
            } catch (Exception e) {
                Log.e(TAG, "Error updating event status for event ID: " + event.getId(), e);
                return event.getStatus();  // 返回当前状态
            }
        }

        @Override
        protected void onPostExecute(String status) {
            // 更新状态视图
            statusTextView.setText(status);
        }
    }
}
