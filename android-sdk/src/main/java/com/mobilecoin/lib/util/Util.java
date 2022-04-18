package com.mobilecoin.lib.util;

import java.util.List;

public class Util {
    public static String listToString(List<String> list) {
        if (list != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(list.get(0));
            for (int i = 1; i < list.size(); i++) {
                sb.append(",").append(list.get(i));
            }
            return sb.toString();
        }
        return "";
    }
}
