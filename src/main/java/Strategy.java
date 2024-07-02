public final class Strategy {
    private Strategy(){}

    public static float Arbitrage_Simple(float price1, float price2, float commission){
        float difference = Math.abs(price1 - price2);

        return difference - commission > 0.f ? 1.f : 0.f;
    }

}
