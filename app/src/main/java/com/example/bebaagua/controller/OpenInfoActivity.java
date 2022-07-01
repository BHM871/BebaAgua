package com.example.bebaagua.controller;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bebaagua.R;
import com.example.bebaagua.model.InfoList;

import java.util.ArrayList;
import java.util.List;

public class OpenInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_info);

        Toolbar toolbar = findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        RecyclerView rvInfo = findViewById(R.id.recycler_view_info);

        List<InfoList> infos = new ArrayList<>();
        infos.add(new InfoList(1, getString(R.string.why), getString(R.string.why_response)));
        infos.add(new InfoList(2, getString(R.string.benefits), getString(R.string.benefits_response)));
        infos.add(new InfoList(3, getString(R.string.how_much), getString(R.string.how_muck_response)));

        rvInfo.setLayoutManager(new LinearLayoutManager(this));
        MainAdapter adapter = new MainAdapter(infos);
        rvInfo.setAdapter(adapter);
    }

    public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder>{

        private final List<InfoList> infoList;

        public MainAdapter(List<InfoList> infoList){
            this.infoList = infoList;
        }

        @NonNull
        @Override
        public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MainViewHolder(getLayoutInflater().inflate(R.layout.list_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
            InfoList infoListCurrent = infoList.get(position);
            holder.title.setText(infoListCurrent.title);
            holder.text.setText(infoListCurrent.text);
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        public class MainViewHolder extends RecyclerView.ViewHolder{

            TextView title = itemView.findViewById(R.id.title_info);
            TextView text = itemView.findViewById(R.id.text_info);

            public MainViewHolder(@NonNull View itemView) {
                super(itemView);
            }

        }

    }

}