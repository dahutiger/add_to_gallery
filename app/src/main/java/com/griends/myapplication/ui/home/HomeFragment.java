package com.griends.myapplication.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.griends.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class HomeFragment extends Fragment {
    
    private HomeViewModel homeViewModel;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        
        String test = "1dfsw";
        char[] array = test.toCharArray();
        for (int i = 0; i < array.length; i++) {
            Log.d("aaaaa", String.valueOf(array[i]));
        }
        printAllResult(test);
        ArrayList<String> result = permutation(test);
        Log.d("aaaaa", "size = " + result.size());
        for (int i = 0; i < result.size(); i++) {
//            Log.d("aaaaa", result.get(i));
        }
    
        return root;
    }
    
    
    private void printAllResult(String input) {
        if (TextUtils.isEmpty(input)) {
            return;
        }
        List<String> result = new ArrayList<>();
        int length = input.length();
        char[] array = input.toCharArray();

//        String
        
        for (int i = 0; i < array.length; i++) {
        
        }
    }
    
    
    public ArrayList<String> permutation(String str) {
        ArrayList<String> res = new ArrayList();
        if (str == null || str.length() == 0) {
            return res;
        }
        TreeSet<String> set = new TreeSet();
        generate(str.toCharArray(), 0, set);
        res.addAll(set);
        return res;
    }
    
    public void generate(char[] arr, int index, TreeSet<String> res) {
        if (index == arr.length) {
            res.add(new String(arr));
        }
        for (int i = index; i < arr.length; i++) {
            swap(arr, index, i);
            generate(arr, index + 1, res);
            swap(arr, index, i);
        }
    }
    
    public void swap(char[] arr, int i, int j) {
        if (arr == null || arr.length == 0 || i < 0 || j > arr.length - 1)
            return;
        char tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
}