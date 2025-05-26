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
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.financial.OhlcvDataSet;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcv;
import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcvItem;
import io.fair_acc.dataset.utils.ProcessingProfiler;
import io.fair_acc.sample.financial.dos.DefaultOHLCV;

public class CandlestickChart extends XYChart {
    public CandlestickChart(String title){
        super();
        setTitle(title);
        DefaultNumericAxis xa = new DefaultNumericAxis("Price", "points");
        DefaultNumericAxis ya = new DefaultNumericAxis("Price", "points");

        getAxes().clear();
        getAxes().add(xa);
        getAxes().add(ya);

        FinancialTheme.Blackberry.applyPseudoClasses(this);
        getPlugins().add(new Zoomer(AxisMode.X));
        getPlugins().add(new EditAxis());
        getPlugins().add(new DataPointTooltip());

        OhlcvDataSet ohlcDataSet = new OhlcvDataSet("test");
        CandleStickRenderer renderer = new CandleStickRenderer(true);
        renderer.getDatasets().addAll(ohlcDataSet);

        DefaultDataSet indiSet = new DefaultDataSet("average");
        var avgRenderer = new ErrorDataSetRenderer();
        avgRenderer.setDrawMarker(false);
        avgRenderer.setErrorStyle(ErrorStyle.NONE);
        avgRenderer.getDatasets().addAll(indiSet);

        getRenderers().addAll(renderer, avgRenderer);
        getRenderers().clear();

    }

    void loadTestData(String data, OhlcvDataSet dataSet, DefaultDataSet indiSet) {
        // what/why is this class
        final long startTime = ProcessingProfiler.getTimeStamp();
        // TODO use sample class and find csv file with OHLCV data
    }


}
