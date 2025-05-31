package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import Model.City;

/**
 * This Adapter class is a helper to data bind the cities
 * to the recycler view to show the search results
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.VH>
{

    private final List<City> cities = new ArrayList<>();
    private final Consumer<City> clickCb;
    private String query;

    public CityAdapter(Consumer<City> clickCb)
    {
        this.clickCb = clickCb;
        query = "";
    }

    /**
     * Call this from your Activity’s TextWatcher
     */
    public void setQuery(String q)
    {
        this.query = q != null ? q : "";
    }

    /**
     * Replace contents & refresh list
     */
    public void setCities(List<City> newCities)
    {
        cities.clear();
        cities.addAll(newCities);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // using Android’s simple_list_item_1 layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1,
                        parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position)
    {
        City c = cities.get(position);
        String display;
        if (!query.isEmpty() && Character.UnicodeBlock.of(query.charAt(0)) == Character.UnicodeBlock.BASIC_LATIN)
            display = c.getEnglishName();
        else
            display = c.getName();

        holder.label.setText(display);
        holder.itemView.setOnClickListener(v -> clickCb.accept(c));
    }

    @Override
    public int getItemCount()
    {
        // cap at 3 results if your repo doesn’t already
        return Math.min(cities.size(), 3);
    }

    static class VH extends RecyclerView.ViewHolder
    {
        TextView label;

        VH(View itemView)
        {
            super(itemView);
            label = itemView.findViewById(android.R.id.text1);
        }
    }
}
