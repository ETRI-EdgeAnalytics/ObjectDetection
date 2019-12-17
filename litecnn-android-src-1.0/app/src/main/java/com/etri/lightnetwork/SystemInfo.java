package com.etri.lightnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class SystemInfo {

    private static SystemInfo instance;

    private static SharedPreferences pref;

    public static SystemInfo getInstance(Context context) {
        if(instance == null) {
            instance = new SystemInfo(context);
        }
        return instance;
    }



    private SystemInfo(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }



    public void setSelectedModel(int modelIdx)
    {
        pref.edit().putInt("pid_selected_model", modelIdx).commit();
    }


    public int getSelectedModel() {
        return pref.getInt("pid_selected_model", 0);
    }


    public void putPerformanceResult(int modelIdx, int time) {

        ArrayList<Integer> resultList = getPrformanceResult(modelIdx);

        resultList.add(0, time);
        StringBuffer strOutput = new StringBuffer();

        while(resultList.size()>20) {
            resultList.remove(resultList.size()-1);
        }
        //if(resultList.size()>20) {
        //    resultList.remove(19);
        //}

        for(int temp : resultList) {
            strOutput.append(String.format("%d,",temp));
        }

        pref.edit().putString("pid_performance_result_"+modelIdx, strOutput.toString()).commit();
    }

    public ArrayList<Integer> getPrformanceResult(int modelIdx) {
        String result = pref.getString("pid_performance_result_"+modelIdx, "");

        String[] items = result.split(",");
        ArrayList<Integer> resultList = new ArrayList<Integer>();

        for(String item : items) {
            if(item.length()>0) {
                resultList.add(Integer.parseInt(item));
            }

        }

        return resultList;
    }
}
