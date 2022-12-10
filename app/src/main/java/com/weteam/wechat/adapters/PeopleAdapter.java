package com.weteam.wechat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.weteam.wechat.R;
import com.weteam.wechat.activities.ChatActivity;
import com.weteam.wechat.databinding.ItemPeopleBinding;
import com.weteam.wechat.models.User;

import java.util.List;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.ViewHolder> {
    private Context context;
    private List<User> userList;

    private static final String STATUS_DATABASE_ONLINE = "online";
//    private static final String STATUS_DATABASE_OFFLINE = "offline";

    public PeopleAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_people, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String avaString = userList.get(holder.getLayoutPosition()).getAvatar();
        if(avaString!=null && !avaString.isEmpty()) {
            Picasso.get()
                    .load(userList.get(position).getAvatar().trim())
                    .placeholder(R.drawable.ic_user_avatar)
                    .error(R.drawable.ic_user_avatar)
                    .into(holder.itemPeopleBinding.civAvatar);
        }
        holder.itemPeopleBinding.tvUserName.setText(userList.get(position).getName().trim());

        if (userList.get(position).getStatus().equals(STATUS_DATABASE_ONLINE.trim())) {
            holder.itemPeopleBinding.ivStatusOnline.setVisibility(View.VISIBLE);
        } else {
            holder.itemPeopleBinding.ivStatusOnline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("USER", userList.get(holder.getLayoutPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (userList != null) {
            return userList.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPeopleBinding itemPeopleBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemPeopleBinding = ItemPeopleBinding.bind(itemView);
            itemPeopleBinding.tvUserName.setSelected(true);
        }
    }
}
