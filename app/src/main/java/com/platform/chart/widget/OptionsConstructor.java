//
//  ChartModel.java
//  ChartCore-Slim
//
//  Created by AnAn on 2018/12/08.
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
 * JianShu       : https://www.jianshu.com/u/f1e6753d4254
 * SegmentFault  : https://segmentfault.com/u/huanghunbieguan
 *
 * -------------------------------------------------------------------------------

 */


package com.platform.chart.widget;

import java.util.HashMap;

public class OptionsConstructor {
    public static HashMap<String, Object> configureChartOptions(ChartModel chartModel) {
        HashMap aaChart = new HashMap<String, Object>();
        aaChart.put("type", chartModel.chartType);//图表类型
        aaChart.put("inverted", chartModel.inverted);//设置是否反转坐标轴，使X轴垂直，Y轴水平。 如果值为 true，则 x 轴默认是 倒置 的。 如果图表中出现条形图系列，则会自动反转
        aaChart.put("backgroundColor", chartModel.backgroundColor);//图表背景色
        aaChart.put("animation", true);//是否开启图表渲染动画
        aaChart.put("pinchType", chartModel.zoomType);//设置手势缩放方向
        aaChart.put("panning", true);//设置手势缩放后是否可平移
        aaChart.put("polar", chartModel.polar);//是否极化图表(开启极坐标模式)
        aaChart.put("marginLeft", chartModel.marginLeft);//图表左边距
        aaChart.put("marginRight", chartModel.marginRight);//图表右边距

        HashMap aaTitle = new HashMap<String, Object>();
        aaTitle.put("text", chartModel.title);//标题文本内容
        HashMap aaTitleStyle = new HashMap<String, Object>();
        aaTitleStyle.put("color", chartModel.titleColor);//标题文字颜色
        aaTitleStyle.put("fontSize", "12px");//标题文字大小
        aaTitle.put("style", aaTitleStyle);

        HashMap aaSubtitle = new HashMap<String, Object>();
        aaSubtitle.put("text", chartModel.subtitle);//富标题文本内容
        HashMap aaSubtitleStyle = new HashMap<String, Object>();
        aaSubtitleStyle.put("color", chartModel.subTitleColor);//副标题文字颜色
        aaSubtitleStyle.put("fontSize", "9px");//副标题文字大小
        aaSubtitle.put("style", aaSubtitleStyle);

        HashMap aaTooltip = new HashMap<String, Object>();
        aaTooltip.put("enabled", chartModel.tooltipEnabled);//是否开启浮动提示框 tooltip
        aaTooltip.put("valueSuffix", chartModel.tooltipValueSuffix);// 浮动提示框数字的单位后缀
        aaTooltip.put("shared", true);//多组 series 数据时,是否共享浮动提示框,默认共享
        aaTooltip.put("crosshairs", chartModel.tooltipCrosshairs);

        HashMap aaSeries = new HashMap<String, Object>();
        aaSeries.put("stacking", chartModel.stacking);//图表堆叠样式类型
        HashMap aaAnimation = new HashMap<String, Object>();
        aaAnimation.put("duration", chartModel.animationDuration);//图表渲染的动画时间
        aaAnimation.put("easing", chartModel.animationType);//图表渲染的动画类型
        aaSeries.put("animation", aaAnimation);

        HashMap aaPlotOptions = new HashMap<String, Object>();
        aaPlotOptions.put("series", aaSeries);

        //数据点标记的相关配置
        aaPlotOptions = configureAAPlotOptionsMarkerStyle(chartModel, aaSeries, aaPlotOptions);
        //配置 aaPlotOptions 的 dataLabels 等相关内容
        aaPlotOptions = configureAAPlotOptionsDataLabels(aaPlotOptions, chartModel);

        HashMap aaLegend = new HashMap<String, Object>();
        aaLegend.put("enabled", chartModel.legendEnabled);//是否显示图表的图例,默认显示
        aaLegend.put("layout", chartModel.legendLayout); //图例数据项的布局。布局类型： "horizontal" 或 "vertical" 即水平布局和垂直布局 默认是：horizontal.
        aaLegend.put("align", chartModel.legendAlign);//设定图例在图表区中的水平对齐方式，合法值有left，center 和 right。
        aaLegend.put("verticalAlign", chartModel.legendVerticalAlign);//设定图例在图表区中的垂直对齐方式，合法值有 top，middle 和 bottom。垂直位置可以通过 y 选项做进一步设定。
        aaLegend.put("borderWidth", 0);
        HashMap aaLegendItemSyle = new HashMap<String, Object>();
        aaLegend.put("color", chartModel.axisColor);//图例的文字颜色,默认图例的文字颜色和X轴文字颜色一样
        aaLegend.put("itemStyle", aaLegendItemSyle);


        HashMap aaOptions = new HashMap<String, Object>();
        aaOptions.put("chart", aaChart);
        aaOptions.put("title", aaTitle);
        aaOptions.put("subtitle", aaSubtitle);
        aaOptions.put("tooltip", aaTooltip);
        aaOptions.put("legend", aaLegend);
        aaOptions.put("plotOptions", aaPlotOptions);
        aaOptions.put("colors", chartModel.colorsTheme);//图表的主体颜色数组
        aaOptions.put("series", chartModel.series);//图表的数据列数组
        aaOptions.put("axisColor", chartModel.axisColor);//图表的 x 轴颜色

        configureAxisContentAndStyle(aaOptions, chartModel);

        return aaOptions;
    }

    private static HashMap<String, Object> configureAAPlotOptionsMarkerStyle(ChartModel chartModel,
                                                                             HashMap<String, Object> aaSeries,
                                                                             HashMap<String, Object> aaPlotOptions) {
        String chartType = chartModel.chartType;
        //数据点标记相关配置，只有线性图(折线图、曲线图、折线区域填充图、曲线区域填充图,散点图)才有数据点标记
        if (chartType == ChartModel.ChartType.Area
                || chartType == ChartModel.ChartType.AreaSpline
                || chartType == ChartModel.ChartType.Line
                || chartType == ChartModel.ChartType.Spline
                || chartType == ChartModel.ChartType.Scatter) {
            HashMap aaMarker = new HashMap<String, Object>();
            aaMarker.put("radius", chartModel.markerRadius);//曲线连接点半径，默认是4
            aaMarker.put("symbol", chartModel.symbol);//曲线连接点类型："circle", "square", "diamond", "triangle","triangle-down"，默认是"circle"
            //设置曲线连接点风格样式
            if (chartModel.symbolStyle == ChartModel.ChartSymbolStyleType.InnerBlank) {
                aaMarker.put("fillColor", "#FFFFFF");//点的填充色(用来设置折线连接点的填充色)
                aaMarker.put("lineWidth", 2);//外沿线的宽度(用来设置折线连接点的轮廓描边的宽度)
                aaMarker.put("lineColor", "");//外沿线的颜色(用来设置折线连接点的轮廓描边颜色，当值为空字符串时，默认取数据点或数据列的颜色。)
            } else if (chartModel.symbolStyle == ChartModel.ChartSymbolStyleType.BorderBlank) {
                aaMarker.put("lineWidth", 2);//外沿线的宽度(用来设置折线连接点的轮廓描边的宽度)
                aaMarker.put("lineColor", chartModel.backgroundColor);//外沿线的颜色(用来设置折线连接点的轮廓描边颜色，当值为空字符串时，默认取数据点或数据列的颜色。)
            }
            aaSeries.put("marker", aaMarker);
            aaPlotOptions.put("series", aaSeries);
        }
        return aaPlotOptions;
    }


    private static HashMap<String, Object> configureAAPlotOptionsDataLabels(HashMap<String, Object> aaPlotOptions,
                                                                            ChartModel chartModel) {

        String chartType = chartModel.chartType;
        HashMap aaDataLabels = new HashMap<String, Object>();
        aaDataLabels.put("enabled", chartModel.xAxisLabelsEnabled);
        HashMap aaSomeTypeChart = new HashMap<String, Object>();

        if (chartType == ChartModel.ChartType.Column
                || chartType == ChartModel.ChartType.Bar) {
            aaSomeTypeChart.put("borderWidth", 0);
            aaSomeTypeChart.put("borderRadius", chartModel.borderRadius);
            aaSomeTypeChart.put("dataLabels", aaDataLabels);
            if (chartModel.polar == true) {
                aaSomeTypeChart.put("pointPadding", 0);
                aaSomeTypeChart.put("groupPadding", 0.005);
            }
        } else if (chartType == ChartModel.ChartType.Pie) {
            aaSomeTypeChart.put("allowPointSelect", true);
            aaSomeTypeChart.put("cursor", "pointer");
            aaSomeTypeChart.put("showInLegend", chartModel.legendEnabled);
            aaDataLabels.put("format", "{point.name}");
            aaSomeTypeChart.put("dataLabels", aaDataLabels);
        } else {
            aaSomeTypeChart.put("dataLabels", aaDataLabels);
        }
        aaPlotOptions.put(chartType, aaSomeTypeChart);

        return aaPlotOptions;
    }

    private static void configureAxisContentAndStyle(HashMap<String, Object> aaOptions,
                                                     ChartModel chartModel) {

        if (chartModel.chartType != ChartModel.ChartType.Pie
                && chartModel.chartType != ChartModel.ChartType.Pyramid
                && chartModel.chartType != ChartModel.ChartType.Funnel) {
            HashMap aaAxisLabel = new HashMap<String, Object>();
            aaAxisLabel.put("enabled", chartModel.xAxisLabelsEnabled);

            HashMap aaXAxis = new HashMap<String, Object>();
            aaXAxis.put("label", aaAxisLabel);
            aaXAxis.put("reversed", chartModel.xAxisReversed);
            aaXAxis.put("gridLineWidth", chartModel.xAxisGridLineWidth);
            aaXAxis.put("categories", chartModel.categories);
            aaXAxis.put("visible", chartModel.xAxisVisible);

            HashMap aaYAxis = new HashMap<String, Object>();
            aaYAxis.put("label", aaAxisLabel);
            aaYAxis.put("reversed", chartModel.yAxisReversed);
            aaYAxis.put("gridLineWidth", chartModel.yAxisGridLineWidth);
            aaYAxis.put("title", chartModel.yAxisTitle);
            aaYAxis.put("lineWidth", chartModel.yAxisLineWidth);
            aaYAxis.put("visible", chartModel.yAxisVisible);

            aaOptions.put("xAxis", aaXAxis);
            aaOptions.put("yAxis", aaYAxis);
        }
    }
}
