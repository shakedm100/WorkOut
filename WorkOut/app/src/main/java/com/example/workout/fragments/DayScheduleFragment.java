package com.example.workout.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workout.R;
import Adapters.ClassAdapter;
import com.example.workout.models.ClassItem;

import java.util.ArrayList;
import java.util.List;

public class DayScheduleFragment extends Fragment {

    private static final String ARG_DAY_NAME = "dayName";
    private String dayName;
    private RecyclerView recyclerView;
    private ClassAdapter adapter;
    private List<ClassItem> classList = new ArrayList<>();

    public static DayScheduleFragment newInstance(String dayName) {
        DayScheduleFragment fragment = new DayScheduleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DAY_NAME, dayName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayName = getArguments().getString(ARG_DAY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_schedule, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // דמו של שיעורים
        classList = loadClassesForDay(dayName);

        adapter = new ClassAdapter(classList, getContext(), this::showPopupMenu);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<ClassItem> loadClassesForDay(String dayName) {
        // כאן אמורים לטעון את השיעורים בפועל (מ־Repository או Firebase)
        // בינתיים ניצור רשימה לדוגמה
        List<ClassItem> list = new ArrayList<>();
        list.add(new ClassItem("Yoga", "13:00 - 14:00"));
        list.add(new ClassItem("Zumba", "14:30 - 15:30"));
        return list;
    }

    private void showPopupMenu(View view, ClassItem item) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.class_item_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_edit) {
                Toast.makeText(getContext(), "Edit " + item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (menuItem.getItemId() == R.id.menu_delete) {
                classList.remove(item);
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }
}
