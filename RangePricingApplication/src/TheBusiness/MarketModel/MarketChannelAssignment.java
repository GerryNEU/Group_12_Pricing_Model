/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TheBusiness.MarketModel;

import TheBusiness.SolutionOrders.SolutionOrder;
import java.util.ArrayList;

/**
 *
 * @author kal bugrara
 */
public class MarketChannelAssignment {

    Market market;
    Channel channel;
    int adbudget;
    int targetrevenue;
//int targetrevenue;
    ArrayList<SolutionOffer> solutionofferlist;
    ArrayList<SolutionOrder> solutionorderlist;
    ArrayList<String> admessages; ///Benefits--language we use to speak to customer

    public MarketChannelAssignment(Market m, Channel c) {

        market = m;
        channel = c;
        solutionofferlist = new ArrayList();
        solutionorderlist = new ArrayList();
        admessages = new ArrayList();

    }
    public void addSolutionOffer(SolutionOffer soffer){
        
        solutionofferlist.add(soffer);
        
    }    
    public void setTargetRevenue(int t){
        targetrevenue = t; //initialize to this amout for this M/C combination
    }
    
    public int getTargetRevenue(){
        return targetrevenue;
    }
    
    public void addSolutionOrder(SolutionOrder so){
        
        solutionorderlist.add(so);
        
        
    }
    //specific to the market/channel combination (teenagers, Tiktok). 
    public boolean isTargetRevenueMet(){
        
        if(getTargetRevenue()<getRevenues()) return true;
        else return false;
        
    }
    
    public int getRevenues(){
        int sum = 0;
        for(SolutionOrder so: solutionorderlist){
            sum = sum + so.getSolutionPrice();
            
        }
        return sum;
    }
    
    public boolean matches(Market m, Channel c) {

        if (market == m && channel == c) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isMarketMatch(Market m) {

        if (market == m) {
            return true;
        }
        
        return false;
    }

    public Market getMarket() {
        return market;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isChannelMatch(Channel c) {

        if (channel == c) {
            return true;
        }
        return false;
    }
}
