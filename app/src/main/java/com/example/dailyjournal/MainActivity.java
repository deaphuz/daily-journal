package com.example.dailyjournal;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class MainActivity extends SingleEntryActivity {

    @Override
    protected Fragment createFragment() {
        UUID entryId = (UUID)getIntent().getSerializableExtra(EntryListFragment.KEY_EXTRA_entry_ID);
        return EntryFragment.newInstance(entryId);
    }

}