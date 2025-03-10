package com.example.dailyjournal;

import androidx.fragment.app.Fragment;

public class EntryListActivity extends SingleEntryActivity {
    @Override
    protected Fragment createFragment() {
        return new EntryListFragment();
    }
}