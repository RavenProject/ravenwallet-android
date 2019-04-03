//
//  ChartModel.java
//  ChartCore-Slim
//
//  Created by AnAn on 2017/9/5.
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

/**
 * Created by AnAn on 2017/9/5.
 */

public class ChartModel {

    public interface ChartAnimationType {
        String EaseInQuad = "easeInQuad";
        String EaseOutQuad = "easeOutQuad";
        String EaseInOutQuad = "easeInOutQuad";
        String EaseInCubic = "easeInCubic";
        String EaseOutCubic = "easeOutCubic";
        String EaseInOutCubic = "easeInOutCubic";
        String EaseInQuart = "easeInQuart";
        String EaseOutQuart = "easeOutQuart";
        String EaseInOutQuart = "easeInOutQuart";
        String EaseInQuint = "easeInQuint";
        String EaseOutQuint = "easeOutQuint";
        String EaseInOutQuint = "easeInOutQuint";
        String EaseInSine = "easeInSine";
        String EaseOutSine = "easeOutSine";
        String EaseInOutSine = "easeInOutSine";
        String EaseInExpo = "easeInExpo";
        String EaseOutExpo = "easeOutExpo";
        String EaseInOutExpo = "easeInOutExpo";
        String EaseInCirc = "easeInCirc";
        String EaseOutCirc = "easeOutCirc";
        String EaseInOutCirc = "easeInOutCirc";
        String EaseOutBounce = "easeOutBounce";
        String EaseInBack = "easeInBack";
        String EaseOutBack = "easeOutBack";
        String EaseInOutBack = "easeInOutBack";
        String Elastic = "elastic";
        String SwingFromTo = "swingFromTo";
        String SwingFrom = "swingFrom";
        String SwingTo = "swingTo";
        String Bounce = "bounce";
        String BouncePast = "bouncePast";
        String EaseFromTo = "easeFromTo";
        String EaseFrom = "easeFrom";
        String EaseTo = "easeTo";
    }

    public interface ChartType {
        String Column = "column";
        String Bar = "bar";
        String Area = "area";
        String AreaSpline = "areaspline";
        String Line = "line";
        String Spline = "spline";
        String Scatter = "scatter";
        String Pie = "pie";
        String Bubble = "bubble";
        String Pyramid = "pyramid";
        String Funnel = "funnel";
        String Columnrange = "columnrange";
        String Arearange = "arearange";
        String Areasplinerange = "areasplinerange";
        String Boxplot = "boxplot";
        String Waterfall = "waterfall";
    }

    public interface ChartSubtitleAlignType {
        String Left = "left";
        String Center = "center";
        String Right = "right";
    }

    public interface ChartZoomType {
        String X = "x";
        String Y = "y";
        String XY = "xy";
    }

    public interface ChartStackingType {
        String False = "";
        String Normal = "normal";
        String Percent = "percent";
    }

    public interface ChartSymbolType {
        String Circle = "circle";
        String Square = "square";
        String Diamond = "diamond";
        String Triangle = "triangle";
        String Triangle_down = "triangle-down";
    }

    public interface ChartSymbolStyleType {
        String Normal = "normal";
        String InnerBlank = "innerBlank";
        String BorderBlank = "borderBlank";
    }

    public interface chartLegendlLayoutType {
        String Horizontal = "horizontal";
        String Vertical = "vertical";
    }

    public interface ChartLegendAlignType {
        String Left = "left";
        String Center = "center";
        String Right = "right";
    }

    public interface ChartLegendVerticalAlignType {
        String Top = "top";
        String Middle = "middle";
        String Bottom = "bottom";
    }

    public interface LineDashSyleType {
        String Solid = "Solid";
        String ShortDash = "ShortDash";
        String ShortDot = "ShortDot";
        String ShortDashDot = "ShortDashDot";
        String ShortDashDotDot = "ShortDashDotDot";
        String Dot = "Dot";
        String Dash = "Dash";
        String LongDash = "LongDash";
        String DashDot = "DashDot";
        String LongDashDot = "LongDashDot";
        String LongDashDotDot = "LongDashDotDot";
    }


    public String animationType;         //动画类型
    public Integer animationDuration;     //动画时间
    public String title;                 //标题内容
    public String subtitle;              //副标题内容
    public String chartType;             //图表类型
    public String stacking;              //堆积样式
    public String symbol;                //折线曲线连接点的类型："circle", "square", "diamond", "triangle","triangle-down"，默认是"circle"
    public String symbolStyle;
    public String zoomType;              //缩放类型 AAChartZoomTypeX表示可沿着 x 轴进行手势缩放
    public Boolean pointHollow;           //折线或者曲线的连接点是否为空心的
    public Boolean inverted;              //x 轴是否翻转(垂直)
    public Boolean xAxisReversed;         //x 轴翻转
    public Boolean yAxisReversed;         //y 轴翻转
    public Boolean tooltipEnabled;      //是否显示浮动提示框(默认显示)
    public String tooltipValueSuffix;  //浮动提示框单位后缀
    public Boolean tooltipCrosshairs;     //是否显示准星线(默认显示)
    public Boolean gradientColorEnable;   //是否要为渐变色
    public Boolean polar;                 //是否极化图形(变为雷达图)
    public Float marginLeft;
    public Float marginRight;
    public Boolean dataLabelEnabled;      //是否显示数据
    public Boolean xAxisLabelsEnabled;    //x轴是否显示数据
    public String[] categories;            //x轴是否显示数据
    public Integer xAxisGridLineWidth;    //x轴网格线的宽度
    public Boolean xAxisVisible;        //x 轴是否显示
    public Boolean yAxisVisible;        //y 轴是否显示
    public Boolean yAxisLabelsEnabled;    //y轴是否显示数据
    public String yAxisTitle;            //y轴标题
    public Float yAxisLineWidth;       //y 轴轴线的宽度

    public Integer yAxisGridLineWidth;    //y轴网格线的宽度
    public Object[] colorsTheme;           //图表主题颜色数组
    public Boolean legendEnabled;         //是否显示图例
    public String legendLayout;          //图例数据项的布局。布局类型： "horizontal" 或 "vertical" 即水平布局和垂直布局 默认是：horizontal.
    public String legendAlign;           //设定图例在图表区中的水平对齐方式，合法值有left，center 和 right。
    public String legendVerticalAlign;   //设定图例在图表区中的垂直对齐方式，合法值有 top，middle 和 bottom。垂直位置可以通过 y 选项做进一步设定。
    public String backgroundColor;       //图表背景色
    public Boolean options3dEnable;       //是否3D化图形(仅对条形图,柱状图有效)
    public Integer options3dAlphaInt;
    public Integer options3dBetaInt;
    public Integer options3dDepth;        //3D图形深度
    public Integer borderRadius;          //柱状图长条图头部圆角半径(可用于设置头部的形状,仅对条形图,柱状图有效)
    public Integer markerRadius;          //折线连接点的半径长度
    public SeriesElement[] series;
    public String titleColor;//标题颜色
    public String subTitleColor;//副标题颜色
    public String axisColor;//x 轴和 y 轴文字颜色


    public ChartModel animationType(String animationType) {
        this.animationType = animationType;
        return this;
    }

    public ChartModel animationDuration(Integer animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public ChartModel title(String title) {
        this.title = title;
        return this;
    }

    public ChartModel subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public ChartModel chartType(String chartType) {
        this.chartType = chartType;
        return this;
    }

    public ChartModel stacking(String stacking) {
        this.stacking = stacking;
        return this;
    }

    public ChartModel symbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public ChartModel symbolStyle(String symbolStyle) {
        this.symbolStyle = symbolStyle;
        return this;
    }

    public ChartModel zoomType(String zoomType) {
        this.zoomType = zoomType;
        return this;
    }

    public ChartModel pointHollow(Boolean pointHollow) {
        this.pointHollow = pointHollow;
        return this;
    }

    public ChartModel inverted(Boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    public ChartModel xAxisReversed(Boolean xAxisReversed) {
        this.xAxisReversed = xAxisReversed;
        return this;
    }

    public ChartModel yAxisReversed(Boolean yAxisReversed) {
        this.yAxisReversed = yAxisReversed;
        return this;
    }

    public ChartModel tooltipCrosshairs(Boolean tooltipCrosshairs) {
        this.tooltipCrosshairs = tooltipCrosshairs;
        return this;
    }

    public ChartModel gradientColorEnable(Boolean gradientColorEnable) {
        this.gradientColorEnable = gradientColorEnable;
        return this;
    }

    public ChartModel polar(Boolean polar) {
        this.polar = polar;
        return this;
    }

    public ChartModel dataLabelEnabled(Boolean dataLabelEnabled) {
        this.dataLabelEnabled = dataLabelEnabled;
        return this;
    }

    public ChartModel xAxisLabelsEnabled(Boolean xAxisLabelsEnabled) {
        this.xAxisLabelsEnabled = xAxisLabelsEnabled;
        return this;
    }

    public ChartModel categories(String[] categories) {
        this.categories = categories;
        return this;
    }

    public ChartModel xAxisGridLineWidth(Integer xAxisGridLineWidth) {
        this.xAxisGridLineWidth = xAxisGridLineWidth;
        return this;
    }

    public ChartModel yAxisGridLineWidth(Integer yAxisGridLineWidth) {
        this.yAxisGridLineWidth = yAxisGridLineWidth;
        return this;
    }

    public ChartModel yAxisLabelsEnabled(Boolean yAxisLabelsEnabled) {
        this.yAxisLabelsEnabled = yAxisLabelsEnabled;
        return this;
    }

    public ChartModel yAxisTitle(String yAxisTitle) {
        this.yAxisTitle = yAxisTitle;
        return this;
    }

    public ChartModel colorsTheme(Object[] colorsTheme) {
        this.colorsTheme = colorsTheme;
        return this;
    }

    public ChartModel legendEnabled(Boolean legendEnabled) {
        this.legendEnabled = legendEnabled;
        return this;
    }

    public ChartModel legendLayout(String legendLayout) {
        this.legendLayout = legendLayout;

        return this;
    }

    public ChartModel legendAlign(String legendAlign) {
        this.legendAlign = legendAlign;
        return this;
    }

    public ChartModel legendVerticalAlign(String legendVerticalAlign) {
        this.legendVerticalAlign = legendVerticalAlign;
        return this;
    }

    public ChartModel backgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ChartModel options3dEnable(Boolean options3dEnable) {
        this.options3dEnable = options3dEnable;
        return this;
    }

    public ChartModel options3dAlphaInt(Integer options3dAlphaInt) {
        this.options3dAlphaInt = options3dAlphaInt;
        return this;
    }

    public ChartModel options3dBetaInt(Integer options3dBetaInt) {
        this.options3dBetaInt = options3dBetaInt;
        return this;
    }

    public ChartModel options3dDepth(Integer options3dDepth) {
        this.options3dDepth = options3dDepth;
        return this;
    }

    public ChartModel borderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
        return this;
    }

    public ChartModel markerRadius(Integer markerRadius) {
        this.markerRadius = markerRadius;
        return this;
    }

    public ChartModel series(SeriesElement[] series) {
        this.series = series;
        return this;
    }


    // 构造函数(亦即是初始化函数)
    public ChartModel() {

//        this.animationType = ChartAnimationType.EaseInBack;
//        this.animationDuration = 800;//以毫秒为单位
//        this.chartType = ChartType.Column;
//        this.inverted = false;
//        this.stacking = AAChartStackingType.False;
//        //this.symbol = AAChartSymbolType.Square.rawValue//默认的折线连接点类型
//        this.xAxisReversed = false;
//        this.yAxisReversed = false;
//        this.zoomType = AAChartZoomType.X;
//        this.pointHollow = false;//默认折线或者曲线的连接点不为空
//        this.colorsTheme = new String[]{"#b5282a","#e7a701","#50c18d","#fd4800","#f1c6c5"};
//        this.gradientColorEnable = false;
//        this.polar = false;
//        this.dataLabelEnabled = true;
//        this.options3dEnable = false;
//        this.crosshairs = true;
//        this.xAxisLabelsEnabled = true;
//        this.xAxisGridLineWidth = 0;
//        this.yAxisLabelsEnabled = true;
//        this.yAxisGridLineWidth = 1;
//        this.legendEnabled = true;
//        this.legendLayout = AAchartLegendlLayoutType.Horizontal;
//        this.legendAlign = AAChartLegendAlignType.Center;
//        this.legendVerticalAlign = AAChartLegendVerticalAlignType.Bottom;
//        this.borderRadius = 0;//柱状图长条图头部圆角半径(可用于设置头部的形状,仅对条形图,柱状图有效,设置为1000时,柱形图或者条形图头部为楔形)
//        this.markerRadius = 5;//折线连接点的半径长度,设置默认值为0,这样就相当于不显示了

        this.animationType = ChartAnimationType.EaseInBack;
        this.animationDuration = 800;//以毫秒为单位
        this.pointHollow = false;
        this.inverted = false;
        this.stacking = ChartStackingType.False;
        this.xAxisReversed = false;
        this.yAxisReversed = false;
        //this.zoomType = ChartZoomType.XY;
        //this.colorsTheme = new String[]{"#b5282a","#e7a701","#50c18d","#fd4800","#f1c6c5"};
        this.colorsTheme = new String[]{"#CACEDC"};//默认的颜色数组(必须要添加默认数组,否则就会出错)

        this.gradientColorEnable = false;
        this.polar = false;
        this.options3dEnable = false;
        this.xAxisLabelsEnabled = false;
        this.xAxisGridLineWidth = 0;
        this.yAxisLabelsEnabled = false;
        this.yAxisGridLineWidth = 1;
        this.legendEnabled = false;
        this.legendLayout = "horizontal";
        this.legendAlign = "center";
        this.legendVerticalAlign = "bottom";
        this.backgroundColor = "#ffffff";
        this.borderRadius = 0;//柱状图长条图头部圆角半径(可用于设置头部的形状,仅对条形图,柱状图有效,设置为1000时,柱形图或者条形图头部为楔形)
        this.markerRadius = 0;//折线连接点的半径长度,如果值设置为0,这样就相当于不显示了
        this.yAxisVisible = false;
        this.xAxisVisible = false;
        this.tooltipEnabled = false;
        this.tooltipCrosshairs = false;
    }
}
