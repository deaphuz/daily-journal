package com.example.dailyjournal;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EntryListFragment extends Fragment implements BottomNavigationView
        .OnItemSelectedListener{
    private RecyclerView recyclerView;
    private EntryAdapter adapter;
    private EntryStorage entryStorage;

    private String lastCriteria;

    List<Entry> entries;
    private boolean appStateRead = false;
    public static final String KEY_EXTRA_entry_ID = "entrylistfragment.entry_id";

    public EntryListFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!appStateRead)
        {
            lastCriteria = "all";
            entryStorage = EntryStorage.getInstance();
            entryStorage.readState(this.requireContext());
            appStateRead = true;
        }
        Log.i(TAG, "entrylistfragment create");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry_list, container, false);
        recyclerView = view.findViewById(R.id.entry_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

       FloatingActionButton addButton = view.findViewById(R.id.add_entry_floating_button);
        addButton.setOnClickListener(v -> {
            Entry entry = new Entry();
            EntryStorage.getInstance().addEntry(entry);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(KEY_EXTRA_entry_ID, entry.getId());
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(entryStorage != null)
        {
            entryStorage.saveState(this.requireContext());
        }
        updateView();
    }

    private static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    private List<Entry> filterEntryList(EntryStorage entryStorage, String criteria)
    {
        List<Entry> localEntries = entryStorage.getEntries();
        entryStorage.getEntries().removeIf(e -> e.getName().equals(""));

        if(criteria.equals("today"))
        {
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date todayDate = todayCal.getTime();

            return localEntries.stream()
                    .filter(entry -> !entry.isDone() && isSameDay(entry.getDate(), todayDate))
                    .collect(Collectors.toList());
        }
        else if(criteria.equals("done"))
        {
            return localEntries.stream()
                    .filter(Entry::isDone)
                    .collect(Collectors.toList());
        }
        else return entryStorage.getEntries();

    }

    private void updateView() {
        entries = filterEntryList(entryStorage, lastCriteria);
        adapter = new EntryAdapter(entries);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.botmenu_action_mine)
        {
            entries = filterEntryList(entryStorage, "all");
            adapter = new EntryAdapter(entries);
            recyclerView.setAdapter(adapter);
            lastCriteria = "all";
            return true;
        }
        else if (item.getItemId() == R.id.botmenu_action_today)
        {
            entries = filterEntryList(entryStorage, "today");
            adapter = new EntryAdapter(entries);
            recyclerView.setAdapter(adapter);
            lastCriteria = "today";
            return true;
        }
        else if (item.getItemId() == R.id.botmenu_action_done)
        {
            entries = filterEntryList(entryStorage, "done");
            adapter = new EntryAdapter(entries);
            recyclerView.setAdapter(adapter);
            lastCriteria = "done";
            return true;
        }
        else return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class EntryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView nameTextView;
        private Entry entry;

        public EntryHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_entry, parent, false));
            itemView.setOnClickListener(this);

            nameTextView = itemView.findViewById(R.id.entry_item_name);
        }

        public void bind(@NonNull Entry entry) {
            this.entry = entry;
            nameTextView.setText(entry.getName());
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra(KEY_EXTRA_entry_ID, entry.getId());
            startActivity(intent);
        }
    }

    private class EntryAdapter extends RecyclerView.Adapter<EntryHolder> {
        private final List<Entry> entries;

        public EntryAdapter(List<Entry> adapterEntries) {
            this.entries = adapterEntries;
        }

        @NonNull
        @Override
        public EntryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new EntryHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull EntryHolder holder, int position) {
            Entry entry = entries.get(position);
            holder.bind(entry);
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }
    }
}
