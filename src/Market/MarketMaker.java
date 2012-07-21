package Market;

public class MarketMaker {
	float[] resources = new float[4]; //4 = cash
	float[] median_prices = new float[3];
	float[] wagers = new float[4];
	float[] target_wagers = new float[]{0.2f,0.2f,0.2f,0.4f};
	float total_value = 0;

	///market maker uses cover's universal portfolio theory 
	
	public void recalc_median_prices(float[] bids, float[] asks) { //bids asks and lasts
		total_value = 0;
		for( int i = 0; i < bids.length; i++) {
			median_prices[i] = (float)Math.exp((Math.log(bids[i])+Math.log(asks[i]))/2.0);
			wagers[i] = median_prices[i] * resources[i];
			total_value += wagers[i];
		}
		wagers[3] = resources[3];
		total_value += resources[3];
	}
	//first, sell at market if doing so at that value would not reduce position below target size
	//then, buy at market if doing so at that value would not exceed target size
	//then, set limit prices where 10 purchase/sale at that price, pricing everything at that price, would, match target allocation.
	

}
