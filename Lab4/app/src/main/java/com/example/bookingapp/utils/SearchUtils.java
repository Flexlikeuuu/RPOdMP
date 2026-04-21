package com.example.bookingapp.utils;

import com.example.bookingapp.db.Apartment;
import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    public static boolean fuzzyMatch(String text, String query) {
        if (query == null || query.isEmpty()) return true;
        text = text.toLowerCase();
        query = query.toLowerCase();

        int i = 0, j = 0;
        while (i < text.length() && j < query.length()) {
            if (text.charAt(i) == query.charAt(j)) j++;
            i++;
        }
        return j == query.length();
    }

    public static List<Apartment> filter(List<Apartment> list, String query) {
        List<Apartment> result = new ArrayList<>();
        for (Apartment a : list) {
            if (fuzzyMatch(a.title, query) || fuzzyMatch(a.description, query)) {
                result.add(a);
            }
        }
        return result;
    }
}