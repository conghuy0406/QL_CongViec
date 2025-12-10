package com.example.ql_congviec.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ql_congviec.R;
import com.example.ql_congviec.model.UserTask;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<UserTask> taskList;

    public TaskAdapter(List<UserTask> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_full, parent, false); // layout bạn đã dùng
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        UserTask task = taskList.get(position);
        holder.txtTitle.setText(task.getTitle());
        holder.txtDescription.setText(task.getDescription());
        String info = "Due: " + task.getDueDate()
                + " • Priority: " + task.getPriority()
                + " • Status: " + task.getStatus()
                + " • Category: " + task.getCategoryName();
        holder.txtInfo.setText(info);


        holder.checkBox.setChecked(task.getStatus().equalsIgnoreCase("DONE"));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtInfo;
        CheckBox checkBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.task_title);
            txtDescription = itemView.findViewById(R.id.task_description);
            txtInfo = itemView.findViewById(R.id.task_info);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}

