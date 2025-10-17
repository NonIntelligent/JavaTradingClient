package utility;

import io.fair_acc.dataset.spi.financial.api.ohlcv.IOhlcv;
import io.fair_acc.dataset.utils.StreamUtils;
import io.fair_acc.sample.financial.dos.DefaultOHLCV;
import io.fair_acc.sample.financial.dos.OHLCVItem;
import io.fair_acc.sample.financial.service.ConcurrentDateFormatAccess;
import io.fair_acc.sample.financial.service.SimpleOhlcvDailyParser;

import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

// Code copied form ChartFX SimpleOhlcvDailyParser class
public class OhlcvDailyParser {
    private static final ConcurrentDateFormatAccess dateFormatParsing = new ConcurrentDateFormatAccess("MM/dd/yyyy HH:mm");
    private static final ConcurrentDateFormatAccess olderDateFormatParsing = new ConcurrentDateFormatAccess("MM/dd/yyyy HHmm");

    public IOhlcv getContinuousOHLCV(String fileName, String symbol) throws IOException, URISyntaxException {
        String path = OhlcvDailyParser.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        File tempFile = new File(path);
        if (path.endsWith(".jar")) {
            tempFile = tempFile.getParentFile();
        }
        path = tempFile.getAbsolutePath() + File.separator;
        String resource = String.format("%s%s.csv", path, fileName);
        IOhlcv iterableItems;
        try (
                InputStreamReader is = new InputStreamReader(StreamUtils.getInputStream(resource));
                BufferedReader br = new BufferedReader(is);
        ) {
            iterableItems = convertTsRowStream(fileName, symbol, br.lines(), new OhlcvDailyParser.DailyOhlcvItemRowParser());
        }

        return iterableItems;
    }

    private static IOhlcv convertTsRowStream(String title, String symbol, Stream<String> rowStream, OhlcvDailyParser.TradeStationRowParser parser) throws NumberFormatException {
        OhlcvDailyParser.TSConvertSettings ref = new OhlcvDailyParser.TSConvertSettings(true, true);
        Calendar cal = Calendar.getInstance();
        List<OHLCVItem> items = new ArrayList();
        DefaultOHLCV ohlcvOutput = new DefaultOHLCV();
        ohlcvOutput.setTitle(title);
        ohlcvOutput.setName(title);
        ohlcvOutput.setAssetName(title);
        ohlcvOutput.setSymbol(symbol);
        rowStream.forEach((r) -> {
            OHLCVItem ohlcvItem = parser.parse(ref, cal, r);
            if (ohlcvItem != null) {
                items.add(ohlcvItem);
            }
        });
        ohlcvOutput.addOhlcvItems(items);
        return ohlcvOutput;
    }

    private static class TSConvertSettings {
        boolean header;
        boolean useNewStyle;

        public TSConvertSettings(boolean header, boolean useNewStyle) {
            this.header = header;
            this.useNewStyle = useNewStyle;
        }
    }

    private static class DailyOhlcvItemRowParser implements OhlcvDailyParser.TradeStationRowParser {
        private DailyOhlcvItemRowParser() {
        }

        public OHLCVItem parse(OhlcvDailyParser.TSConvertSettings ref, Calendar cal, String r) {
            if (ref.header) {
                ref.header = false;
                return null;
            } else {
                String[] row = r.split(",");

                Date timestamp;
                try {
                    if (ref.useNewStyle) {
                        timestamp = OhlcvDailyParser.dateFormatParsing.parse(row[0] + " " + row[1]);
                    } else {
                        timestamp = OhlcvDailyParser.olderDateFormatParsing.parse(row[0] + " " + row[1]);
                    }
                } catch (ParseException var9) {
                    try {
                        timestamp = OhlcvDailyParser.olderDateFormatParsing.parse(row[0] + " " + row[1]);
                    } catch (ParseException var8) {
                        throw new IllegalArgumentException("Wrong format of daily data row=" + row[0] + " " + row[1]);
                    }

                    ref.useNewStyle = false;
                }

                return new OHLCVItem(timestamp, Double.parseDouble(row[2]), Double.parseDouble(row[3]), Double.parseDouble(row[4]), Double.parseDouble(row[5]), Double.parseDouble(row[6]), Double.parseDouble(row[7]));
            }
        }
    }

    @FunctionalInterface
    private interface TradeStationRowParser {
        OHLCVItem parse(OhlcvDailyParser.TSConvertSettings var1, Calendar var2, String var3);
    }
}
