package broker;

import Data.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DemoAPI extends TradingAPI {
    private final ObjectMapper demoMapper;
    private List<Instrument> mockInstruments;
    private List<Position> mockPositions;
    private List<Order> mockOrders;
    private List<Quote> mockQuotes;
    private Account cashAndMeta;


    public DemoAPI(String domain) {
        super(Broker.DEMO, domain);
        demoMapper = new ObjectMapper();
        demoMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        demoMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mockInstruments = new ArrayList<>();
        mockPositions = new ArrayList<>();
        mockOrders = new ArrayList<>();
        mockQuotes = new ArrayList<>();
        mockInstruments = new ArrayList<>();

        cashAndMeta = new Account(null, null, null, null);

        // Setup default randomised data
        setupInstruments();
        setupPositions();
        setupOrders();
        setupQuotes();
        setupAccountCashAndMeta();
    }

    @Override
    public Result fetchInstruments() {
        ArrayNode ordersArrayNode = demoMapper.createArrayNode();

        for (Instrument inst : mockInstruments) {
            ObjectNode instrumentNode = demoMapper.createObjectNode();
            instrumentNode.put("symbol", inst.symbol);
            instrumentNode.put("name", inst.name);
            instrumentNode.put("class", inst.type);
            instrumentNode.put("tradable", inst.tradable);

            ordersArrayNode.add(instrumentNode);
        }

        return new Result(200, ordersArrayNode.toString());
    }

    @Override
    public Result fetchPositions() {
        ArrayNode ordersArrayNode = demoMapper.createArrayNode();

        for (Position position : mockPositions) {
            ObjectNode positionNode = demoMapper.createObjectNode();
            positionNode.put("symbol", position.symbol);
            positionNode.put("qty", position.quantity);
            positionNode.put("current_price", position.currentPrice);
            positionNode.put("market_value", position.marketValue);
            positionNode.put("avg_entry_price", position.avgEntry);
            positionNode.put("unrealized_pl", position.profitLoss);

            ordersArrayNode.add(positionNode);
        }

        return new Result(200, ordersArrayNode.toString());
    }

    @Override
    public Result fetchOrders() {
        ArrayNode ordersArrayNode = demoMapper.createArrayNode();

        for (Order order : mockOrders) {
            ObjectNode orderNode = demoMapper.createObjectNode();
            orderNode.put("id", order.id);
            orderNode.put("symbol", order.symbol);
            orderNode.put("created_at", order.creationTime);
            orderNode.put("filled_at", order.filledTime);
            orderNode.put("type", order.executionType);
            orderNode.put("qty", order.quantity);
            orderNode.put("side", order.side);
            orderNode.put("status", order.status);
            orderNode.put("filled_avg_price", order.filledValue);

            ordersArrayNode.add(orderNode);
        }

        return new Result(200, ordersArrayNode.toString());
    }

    @Override
    public Result fetchAccountCash() {
        String freeCash = Double.toString(cashAndMeta.freeCash);
        String total = Double.toString(cashAndMeta.totalCash);

        ObjectNode cash = demoMapper.createObjectNode();
        cash.put("cash", freeCash);
        cash.put("equity", total);

        return new Result(200, cash.toString());
    }

    @Override
    public Result fetchAccountMeta() {
        String id = cashAndMeta.accountID;
        String currency = cashAndMeta.currencyCode;

        ObjectNode meta = demoMapper.createObjectNode();
        meta.put("id", id);
        meta.put("currency", currency);

        return new Result(200, meta.toString());
    }

    @Override
    public Result fetchStockData(String[] tickers) {
        List<Integer> indexOfInstruments = new ArrayList<>();
        for (int i = 0; i < tickers.length; i++){
            for (int match = 0; match < mockInstruments.size(); match++) {
                Instrument inst = mockInstruments.get(match);
                if (inst.symbol.equals(tickers[i])) indexOfInstruments.add(match);
            }
        }

        ArrayNode quotes = demoMapper.createArrayNode();

        for (int i = 0; i < indexOfInstruments.size(); i++) {
            ObjectNode quoteNode = demoMapper.createObjectNode();
            Quote quote = mockQuotes.get(indexOfInstruments.get(i));

            quoteNode.put("bp", quote.bidPrice);
            quoteNode.put("ap", quote.askPrice);

            quotes.add(quoteNode);
        }

        return new Result(200, quotes.toString());
    }

    @Override
    public Result placeMarketOrder(String ticker, String quantity, OrderType orderType, boolean isBuy) {
        if (!isBuy) return new Result(501, "Selling is not available in Demo mode");

        LocalDateTime now = LocalDateTime.now();
        Instant today = now.atZone(ZoneId.of("UTC")).toInstant();

        // Create mock data
        Quote quote = mockQuotes.get(getIndexOfInstrument(ticker));
        Order order = createOrder(ticker, quantity, quote.askPrice, today, today);
        Position position = createPosition(ticker, quantity, quote.askPrice, quote.askPrice);

        // Update account cash
        cashAndMeta.freeCash -= Double.valueOf(position.marketValue);

        // Add to backend data lists
        mockPositions.add(position);
        mockOrders.add(order);
        return new Result(200, "Orders and positions have been generated");
    }

    private void setupInstruments() {
        final int INSTRUMENT_COUNT = 20;
        int i = 0;
        while (i < INSTRUMENT_COUNT) {
            Instrument inst = new Instrument();
            inst.symbol = generateRandomString(4);
            inst.name = "Mock name";
            inst.type = "us_equity";
            inst.tradable = true;
            mockInstruments.add(inst);
            i++;
        }
    }

    private void setupPositions() {
        final int POSITION_COUNT = 5;

        int i = 0;
        while (i < POSITION_COUNT) {
            Position pos = new Position();
            pos.symbol = generateRandomString(4);
            pos.quantity = generateRandomNumberAsString(1, 11);
            pos.currentPrice = BigDecimal.valueOf(generateRandomNumber(1d, 200d)).setScale(2, RoundingMode.HALF_EVEN).toString();
            pos.marketValue = Double.toString(Double.parseDouble(pos.quantity) * Double.parseDouble(pos.currentPrice));
            pos.avgEntry = BigDecimal.valueOf(generateRandomNumber(1d, Double.parseDouble(pos.currentPrice)))
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .toString();

            pos.profitLoss = BigDecimal.valueOf(
                    (Double.parseDouble(pos.currentPrice) - Double.parseDouble(pos.avgEntry))
                    * Double.parseDouble(pos.quantity))
                    .setScale(2, RoundingMode.HALF_UP)
                    .toString();

            mockPositions.add(pos);
            i++;
        }
    }

    private void setupOrders() {
        for (Position p : mockPositions) {
            LocalDateTime mockTime1 = LocalDateTime.of(2025, 10, 18, 20, 0);
            LocalDateTime mockTime2 = LocalDateTime.of(2025, 10, 19, 20, 0);
            Instant creation = mockTime1.atZone(ZoneId.of("UTC")).toInstant();
            Instant filled = mockTime2.atZone(ZoneId.of("UTC")).toInstant();
            Order order = createOrder(p.symbol, p.quantity, p.avgEntry, creation, filled);

            mockOrders.add(order);
        }
    }

    private void setupQuotes() {
        for (int i = 0; i < mockInstruments.size(); i++) {
            Quote quote = new Quote();
            BigDecimal buyPrice = new BigDecimal(generateRandomNumber(1d, 200d));
            buyPrice = buyPrice.setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal sellPrice = new BigDecimal(generateRandomNumber(buyPrice.doubleValue(), buyPrice.doubleValue() + 1d));
            sellPrice = sellPrice.setScale(2, RoundingMode.HALF_EVEN);

            quote.askPrice = buyPrice.toString();
            quote.bidPrice = sellPrice.toString();

            mockQuotes.add(quote);
        }
    }

    private void setupAccountCashAndMeta() {
        cashAndMeta.freeCash = generateRandomNumber(10000, 100000);
        double totalInvested = 0;
        for (Position p : mockPositions) {
            totalInvested += Double.valueOf(p.marketValue);
        }

        cashAndMeta.investedCash = totalInvested;
        cashAndMeta.totalCash = cashAndMeta.freeCash + totalInvested;

        cashAndMeta.accountID = generateRandomString(12);
        cashAndMeta.currencyCode = "USD";
    }

    private Order createOrder(String symbol, String qty, String filledValue, Instant creation, Instant filled) {
        Order order = new Order();
        order.id = generateRandomString(26);
        order.symbol = symbol;
        order.quantity = qty;
        order.creationTime = creation.toString();
        order.filledTime = filled.toString();
        order.executionType = OrderType.MARKET.toString();
        order.side = "buy";
        order.filledValue = filledValue;
        order.status = "filled";

        return order;
    }

    private Position createPosition(String symbol, String qty, String price, String avgEntry) {
        Position pos = new Position();
        pos.symbol = symbol;
        pos.quantity = qty;
        pos.currentPrice = price;
        pos.marketValue = Double.toString(Double.valueOf(pos.quantity) * Double.valueOf(pos.currentPrice));
        pos.avgEntry = avgEntry;

        pos.profitLoss = Double.toString((
                Double.valueOf(pos.currentPrice)
                - Double.valueOf(pos.avgEntry))
                * Double.valueOf(pos.quantity));

        return pos;
    }

    private int getIndexOfInstrument(String symbol) {
        int index = 0;
        for (int i = 0; i < mockInstruments.size(); i++) {
            if (mockInstruments.get(i).symbol == symbol) {
                index = i;
                break;
            }
        }

        return index;
    }

    private String generateRandomString(int length) {
        final String characterSelection = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int boundLength = Math.min(characterSelection.length(), length);
        char[] text = new char[boundLength];
        for (int i = 0; i < boundLength; i++) {
            text[i] = characterSelection.charAt(ThreadLocalRandom.current().nextInt((characterSelection.length())));
        }

        return new String(text);
    }

    private double generateRandomNumber(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private int generateRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    private String generateRandomNumberAsString(double min, double max) {
        return Double.toString(generateRandomNumber(min, max));
    }

    private String generateRandomNumberAsString(int min, int max) {
        return Integer.toString(generateRandomNumber(min, max));
    }
}
