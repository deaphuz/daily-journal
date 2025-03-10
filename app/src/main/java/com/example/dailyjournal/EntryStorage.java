package com.example.dailyjournal;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntryStorage implements Serializable {
    private static final EntryStorage instance = new EntryStorage();
    private List<Entry> entries;

    private EntryStorage() {
        entries = new ArrayList<Entry>();

     /*   final int entriesCount = 10;
     //   entries = new ArrayList<Entry>();
        for (int i = 1; i <= entriesCount; ++i) {
            Entry entry = new Entry();
            entry.setName("Trening #" + i);
            entry.setDone(i % 3 == 0);
            entries.add(entry);
        }*/
    }

    public void saveState(@NonNull Context context) {
        try {
            FileOutputStream fos = context.openFileOutput("entrystorage_state.txt", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(entries);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readState(@NonNull Context context) {
        try {
            FileInputStream fis = context.openFileInput("entrystorage_state.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            entries = (List<Entry>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static EntryStorage getInstance() {
        return instance;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public Entry getEntry(UUID id) {
        for (Entry entry : entries) {
            if (entry.getId().equals(id))
                return entry;
        }
        return null;
    }
}
