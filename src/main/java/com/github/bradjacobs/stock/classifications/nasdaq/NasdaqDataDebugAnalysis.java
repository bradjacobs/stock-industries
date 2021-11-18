package com.github.bradjacobs.stock.classifications.nasdaq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * "Quick and Dirty" class to take a closer look at the sector/industry data from the Nasdaq data
 */
public class NasdaqDataDebugAnalysis
{
    /**
     * Debug Use:
     * Prints any one-off looking sector/industry pair, b/c sometimes the data can be 'off'
     * @param sectorIndustryMap sectorIndustryMap
     */
    public static void sectorMapValidityCheck(Map<String, Map<String, Set<String>>> sectorIndustryMap)
    {
        Map<String, List<SectorIndustryTickerCountRecord>> industryTrackingMap = new HashMap<>();

        // scan over the entire data structure to see if there are any Sector/Industry combinations
        //   that correlate with exactly _ONE_ ticker.  Meaning there 'could' be a data error.
        StringBuilder msgBuilder = new StringBuilder();
        for (Map.Entry<String, Map<String, Set<String>>> sectorEntry : sectorIndustryMap.entrySet())
        {
            String sector = sectorEntry.getKey();
            Map<String, Set<String>> industryMap = sectorEntry.getValue();
            for (Map.Entry<String, Set<String>> industryEntry : industryMap.entrySet())
            {
                String industry = industryEntry.getKey();
                Set<String> tickerSet = industryEntry.getValue();

                List<SectorIndustryTickerCountRecord> industryTrackingList = industryTrackingMap.computeIfAbsent(industry, k -> new ArrayList<>());
                industryTrackingList.add(new SectorIndustryTickerCountRecord(sector, industry, tickerSet.size()));

                if (tickerSet.size() == 1) {
                    msgBuilder.append(String.format("| Ticker: %-5s | Sector: %-21s | Industry: %-39s |\n", tickerSet.iterator().next(), sector, industry));
                }
            }
        }

        if (msgBuilder.length() > 0) {
            System.out.println("NOTE: Nasdaq data has following sector/industry pairs only once...");
            System.out.println(msgBuilder.toString());
            System.out.println();
        }

        // dump info of industries in more than 1 sector.
        for (Map.Entry<String, List<SectorIndustryTickerCountRecord>> entry : industryTrackingMap.entrySet()) {
            String industry = entry.getKey();
            List<SectorIndustryTickerCountRecord> recordList = entry.getValue();
            if (recordList.size() > 1) {
                System.out.println(constructDebugOutputLine(industry, recordList));
            }
        }
    }

    // very quick and dirty
    private static String constructDebugOutputLine(String industry, List<SectorIndustryTickerCountRecord> recordList) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-35s -- ", industry));
        for (SectorIndustryTickerCountRecord record : recordList) {
            sb.append(String.format(" %s(%d) ", record.sector, record.tickerCount));
        }
        return sb.toString();
    }


    private static class SectorIndustryTickerCountRecord {
        private final String sector;
        private final String industry;
        private final int tickerCount;
        public SectorIndustryTickerCountRecord(String sector, String industry, int tickerCount) {
            this.sector = sector;
            this.industry = industry;
            this.tickerCount = tickerCount;
        }
    }


    // REFERENCE ONLY:
    //   as of 06/10/2021, all the Sector/Industry combinations that match
    //   to EXACTLY one ticker is listed below:
    /*
        | Ticker: APG   | Sector: Basic Industries      | Industry: Engineering & Construction              |
        | Ticker: NEWA  | Sector: Basic Industries      | Industry: Miscellaneous                           |
        | Ticker: TTC   | Sector: Capital Goods         | Industry: Tools/Hardware                          |
        | Ticker: WOR   | Sector: Consumer Durables     | Industry: Aerospace                               |
        | Ticker: WHR   | Sector: Consumer Durables     | Industry: Consumer Electronics/Appliances         |
        | Ticker: CMT   | Sector: Consumer Durables     | Industry: Electronic Components                   |
        | Ticker: FERG  | Sector: Consumer Durables     | Industry: Industrial Machinery/Components         |
        | Ticker: DOGZ  | Sector: Consumer Durables     | Industry: Miscellaneous manufacturing industries  |
        | Ticker: CVR   | Sector: Consumer Non-Durables | Industry: Industrial Specialties                  |
        | Ticker: FIGS  | Sector: Consumer Non-Durables | Industry: Medical Specialities                    |
        | Ticker: CLII  | Sector: Consumer Services     | Industry: Automotive Aftermarket                  |
        | Ticker: CHSCM | Sector: Consumer Services     | Industry: Farming/Seeds/Milling                   |
        | Ticker: EBF   | Sector: Consumer Services     | Industry: Office Equipment/Supplies/Services      |
        | Ticker: UTME  | Sector: Energy                | Industry: Consumer Electronics/Appliances         |
        | Ticker: USLM  | Sector: Energy                | Industry: Other Metals and Minerals               |
        | Ticker: HI    | Sector: Miscellaneous         | Industry: Consumer Specialties                    |
        | Ticker: CHRA  | Sector: Public Utilities      | Industry: Medical Specialities                    |
        | Ticker: SGU   | Sector: Public Utilities      | Industry: Oil Refining/Marketing                  |
        | Ticker: SPH   | Sector: Public Utilities      | Industry: Other Specialty Stores                  |
        | Ticker: MICT  | Sector: Technology            | Industry: EDP Peripherals                         |
        | Ticker: ROL   | Sector: Technology            | Industry: Industrial Specialties                  |
        | Ticker: JCOM  | Sector: Technology            | Industry: Telecommunications Equipment            |
        | Ticker: ZTO   | Sector: Transportation        | Industry: Advertising                             |
        | Ticker: GATX  | Sector: Transportation        | Industry: Rental/Leasing Companies                |
     */

/*

For references -- 11/17/2021

Oil & Gas Production                --  Energy(129)  Public Utilities(5)
Business Services                   --  Finance(359)  Miscellaneous(74)
Consumer Electronics/Appliances     --  Consumer Durables(1)  Consumer Non-Durables(10)  Energy(1)
Office Equipment/Supplies/Services  --  Capital Goods(23)  Consumer Durables(2)  Consumer Services(1)  Miscellaneous(3)
EDP Services                        --  Consumer Services(5)  Technology(268)
Automotive Aftermarket              --  Basic Industries(3)  Capital Goods(5)  Consumer Durables(4)
Consumer Specialties                --  Consumer Non-Durables(5)  Consumer Services(2)  Miscellaneous(1)
Medical Specialities                --  Capital Goods(3)  Consumer Non-Durables(1)  Health Care(73)  Public Utilities(1)
Building Products                   --  Capital Goods(10)  Consumer Durables(8)
Recreational Products/Toys          --  Capital Goods(5)  Consumer Non-Durables(22)  Consumer Services(9)
Real Estate                         --  Consumer Services(10)  Finance(32)
Industrial Machinery/Components     --  Capital Goods(138)  Consumer Durables(1)  Energy(1)  Miscellaneous(4)  Technology(21)
Diversified Commercial Services     --  Consumer Services(5)  Finance(2)  Miscellaneous(30)  Technology(34)
Multi-Sector Companies              --  Basic Industries(2)  Capital Goods(3)  Miscellaneous(2)
Rental/Leasing Companies            --  Miscellaneous(6)  Transportation(1)
Pollution Control Equipment         --  Basic Industries(1)  Capital Goods(3)
Specialty Foods                     --  Consumer Non-Durables(24)  Consumer Services(1)
Professional Services               --  Consumer Services(14)  Technology(6)
Oil Refining/Marketing              --  Energy(9)  Public Utilities(1)  Transportation(8)
Other Pharmaceuticals               --  Consumer Services(1)  Health Care(8)
Specialty Chemicals                 --  Basic Industries(7)  Capital Goods(1)  Consumer Durables(87)
Diversified Manufacture             --  Capital Goods(9)  Miscellaneous(20)
Home Furnishings                    --  Basic Industries(2)  Consumer Durables(18)  Miscellaneous(2)
Railroads                           --  Capital Goods(4)  Transportation(5)
Containers/Packaging                --  Basic Industries(4)  Capital Goods(1)  Consumer Durables(5)
Electronic Components               --  Capital Goods(5)  Consumer Durables(2)  Technology(18)
Aerospace                           --  Capital Goods(16)  Transportation(10)
Industrial Specialties              --  Basic Industries(27)  Capital Goods(4)  Consumer Durables(3)  Health Care(29)  Technology(1)
Advertising                         --  Consumer Services(2)  Technology(43)
Engineering & Construction          --  Basic Industries(2)  Capital Goods(12)  Technology(4)
Other Specialty Stores              --  Consumer Services(39)  Public Utilities(1)
Homebuilding                        --  Basic Industries(4)  Capital Goods(15)  Consumer Non-Durables(2)  Consumer Services(4)
Telecommunications Equipment        --  Basic Industries(4)  Capital Goods(22)  Consumer Non-Durables(3)  Consumer Services(25)  Public Utilities(50)
Military/Government/Technical       --  Capital Goods(16)  Consumer Non-Durables(3)  Consumer Services(14)
Other Consumer Services             --  Consumer Services(31)  Miscellaneous(27)  Transportation(4)
Marine Transportation               --  Consumer Services(5)  Transportation(49)
Electrical Products                 --  Capital Goods(16)  Consumer Durables(10)  Technology(11)
Wholesale Distributors              --  Capital Goods(14)  Consumer Durables(1)
Metal Fabrications                  --  Basic Industries(10)  Capital Goods(15)  Consumer Durables(7)  Energy(17)
Department/Specialty Retail Stores  --  Consumer Non-Durables(8)  Consumer Services(21)
Hotels/Resorts                      --  Consumer Services(26)  Transportation(1)
Agricultural Chemicals              --  Basic Industries(11)  Energy(1)
Oilfield Services/Equipment         --  Capital Goods(7)  Energy(37)
Water Supply                        --  Basic Industries(7)  Public Utilities(19)
 */
}
