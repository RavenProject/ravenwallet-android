//
//  ChartModel.java
//  ChartCore-Slim
//
//  Created by AnAn on 2017/9/8..
//  Copyright © 2018年 An An. All rights reserved.
//*************** ...... SOURCE CODE ...... ***************
//***...................................................***
//*** https://github.com/ChartModel/ChartCore         ***
//*** https://github.com/ChartModel/ChartCore-Slim    ***
//***...................................................***
//*************** ...... SOURCE CODE ...... ***************


/*

 * -------------------------------------------------------------------------------
 *
 *  🌕 🌖 🌗 🌘  ❀❀❀   WARM TIPS!!!   ❀❀❀ 🌑 🌒 🌓 🌔
 *
 * Please contact me on GitHub,if there are any problems encountered in use.
 * GitHub Issues : https://github.com/ChartModel/ChartCore-Slim/issues
 * -------------------------------------------------------------------------------
 * And if you want to contribute for this project, please contact me as well
 * GitHub        : https://github.com/ChartModel
 * StackOverflow : https://stackoverflow.com/users/7842508/codeforu
 * JianShu       : http://www.jianshu.com/u/f1e6753d4254
 * SegmentFault  : https://segmentfault.com/u/huanghunbieguan
 *
 * -------------------------------------------------------------------------------

 */

package com.platform.chart.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;


public class ChartView extends WebView {

    public Float contentWidth;
    public Float contentHeight;

    public ChartView(Context context) {
        super(context);
        sharedConstructor();
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        sharedConstructor();
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor();
    }


    public int convertDipToPixels(float dips) {
        return (int) (dips * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void sharedConstructor() {
        // Do some initialize work.
        this.contentWidth = 320.f;
        this.contentHeight = 350.f;
    }
    public void aa_drawChartWithChartModel(final ChartModel chartModel) {
        this.getSettings().setJavaScriptEnabled(true);
        this.loadUrl("file:///android_asset/charts/ChartView.html");

        this.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                drawChartWithChartModel(chartModel);
            }
        });
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void drawChartWithChartModel(ChartModel chartModel) {
        // 将对象编译成json
        Gson gson = new Gson();
        String optionsJson = gson.toJson(chartModel);
        try {
            JSONObject jsonObject = new JSONObject(gson.toJson(chartModel));
            jsonObject.put("margin",0);
            optionsJson = jsonObject.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Options " + optionsJson);


        HashMap myJson = OptionsConstructor.configureChartOptions(chartModel);
        System.out.println("Options " + optionsJson);


//        this.loadUrl("javascript:loadTheHighChartView('" + optionsJson + "','" + contentWidth + "','" + 155 + "')");
        this.loadUrl("javascript:loadTheHighChartView('" + optionsJson + "','" + 0 + "','" + 0 + "')");
    }


    public void aa_refreshChartWithChartModel(ChartModel chartModel) {
        Gson gson = new Gson();
        String newOptions = gson.toJson(chartModel);
        this.loadUrl("javascript:loadTheHighChartView('" + newOptions + "','" + contentWidth + "','" + contentHeight + "')");
    }

    public void aa_onlyRefreshTheChartDataWithChartModelSeriesArray(SeriesElement[] seriesElementsArr) {
        Gson gson = new Gson();
        String seriesArr = gson.toJson(seriesElementsArr);
        this.loadUrl("javascript:onlyRefreshTheChartDataWithSeries('" + seriesArr + "',')");
    }
}

