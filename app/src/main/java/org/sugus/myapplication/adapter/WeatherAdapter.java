package org.sugus.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import org.sugus.myapplication.R;
import org.sugus.myapplication.utils.Utils;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    List<LocalDayWeatherForecast> list;

    static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView week;
        public TextView date;
        public TextView f_temp;
        public ImageView f_weather;
        public TextView f_weather_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            week = itemView.findViewById(R.id.week);
            date = itemView.findViewById(R.id.date);
            f_temp = itemView.findViewById(R.id.f_temp);
            f_weather = itemView.findViewById(R.id.f_weather);
            f_weather_text = itemView.findViewById(R.id.f_weather_text);
        }
    }

    public WeatherAdapter(List<LocalDayWeatherForecast> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.circular_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        holder.itemView.setLayoutParams(layoutParams);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDayWeatherForecast localDayWeatherForecast = list.get(position);
        holder.week.setText(localDayWeatherForecast.getWeek());
        holder.date.setText(localDayWeatherForecast.getDate());
        holder.f_temp.setText(localDayWeatherForecast.getDayTemp() + "°/" + localDayWeatherForecast.getNightTemp() + "°");
        holder.f_weather_text.setText(localDayWeatherForecast.getDayWeather());
        holder.f_weather.setImageResource(Utils.getWeatherResource(localDayWeatherForecast.getDayWeather()));
    }

    @Override
    public int getItemCount() {
        if(list != null) {
            return list.size();
        }
        return 0;
    }
}
