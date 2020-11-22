package org.sugus.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.sugus.myapplication.adapter.CityAdapter;
import org.sugus.myapplication.entity.City;
import org.sugus.myapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CityMenuActivity extends AppCompatActivity{


    private List<City> cities;
    private CityAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_menu);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        initNavBar();
        SearchView searchView = findViewById(R.id.s_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final List<City> cityList = filter(cities, newText);
                adapter.setFilter(cityList);
                return true;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityMenuActivity.this.onClick();
            }
        });
        this.setTitle("选择城市");
    }

    public void onClick(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    public void initNavBar(){
        cities = Utils.getXMLInfo(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view1);

        LinearLayoutManager layout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);

        adapter = new CityAdapter(cities);

        recyclerView.setAdapter(adapter);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private List<City> filter(List<City> models, String query) {
        //查找搜索栏中对应的城市，然后返回合适的列表到适配器进行显示
        final ArrayList<City> filteredModelList = new ArrayList<>();
        if(!models.isEmpty()) {
            for (City model : models) {

                final String text = model.getCityName();

                if (text.contains(query)) {
                    filteredModelList.add(model);
                }

            }
            return filteredModelList;
        }
       return cities;
    }
}
