package ui;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.AxisMode;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.ErrorStyle;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.CandleStickRenderer;
import io.fair_acc.chartfx.renderer.spi.financial.FinancialTheme;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcv;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcvItem;
import io.fair_acc.dataset.utils.ProcessingProfiler;
import io.fair_acc.sample.financial.dos.DefaultOHLCV;
import io.fair_acc.sample.financial.service.SimpleOhlcvDailyParser;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import utility.OhlcvDailyParser;

import java.io.IOException;

public class CandlestickChart extends XYChart {
    public CandlestickChart(String fileName, String title, String symbol){
        super();
        setTitle(title);
        DefaultNumericAxis xa = new DefaultNumericAxis("Time", "d");
        DefaultNumericAxis ya = new DefaultNumericAxis("Price", "USD");
        ya.setSide(Side.LEFT);

        getAxes().clear();
        getAxes().add(xa);
        getAxes().add(ya);

        FinancialTheme.Classic.applyPseudoClasses(this);
        getPlugins().add(new Zoomer(AxisMode.X));
        getPlugins().add(new EditAxis());
        getPlugins().add(new DataPointTooltip());

        OhlcvDataSet ohlcDataSet = new OhlcvDataSet("test");
        DefaultDataSet indiSet = new DefaultDataSet("average");

        try {
            loadTestData(fileName, symbol, ohlcDataSet, indiSet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupRenderers(ohlcDataSet, indiSet);

    }

    private void setupRenderers(OhlcvDataSet ohlcDataSet, DefaultDataSet indiSet) {
        CandleStickRenderer renderer = new CandleStickRenderer(true);
        renderer.getDatasets().addAll(ohlcDataSet);

        var avgRenderer = new ErrorDataSetRenderer();
        avgRenderer.setDrawMarker(false);
        avgRenderer.setErrorStyle(ErrorStyle.NONE);
        avgRenderer.getDatasets().addAll(indiSet);

        getRenderers().clear();
        getRenderers().addAll(renderer, avgRenderer);
    }

    public void loadTestData(String data, String symbol, OhlcvDataSet dataSet, DefaultDataSet indiSet) throws IOException {
        // what/why is this class
        final long startTime = ProcessingProfiler.getTimeStamp();

        IOhlcv ohlcv = new OhlcvDailyParser().getContinuousOHLCV(data, symbol);
        dataSet.setData(ohlcv);

        DescriptiveStatistics stats = new DescriptiveStatistics(24);

        for (IOhlcvItem ohlcvItem : ohlcv) {
            double timestamp = ohlcvItem.getTimeStamp().getTime() / 1000.0;
            stats.addValue(ohlcvItem.getClose());
            indiSet.add(timestamp, stats.getMean());
        }
        ProcessingProfiler.getTimeDiff(startTime, "adding data into DataSet");
    }

}
