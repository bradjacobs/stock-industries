package com.github.bradjacobs.stock.classifications;

public enum Classification
{
    //BICS("bics", ""),  // < - can't seen to find definition hierarchy spec.
    GICS("gics","https://www.msci.com/documents/1296102/11185224/GICS_map+2018.xlsx"),
    ICB("icb","https://content.ftserussell.com/sites/default/files/icb_structure_and_definitions.xlsx"),
    ISIC("isic","http://www.ilo.org/ilostat-files/Documents/ISIC.xlsx"),
    MGECS("mgecs","https://advisor.morningstar.com/Enterprise/VTC/MorningstarGlobalEquityClassStructure2019v3.pdf"),  // aka MorningStar
    NACE("nace","https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=ACT_OTH_CLS_DLD&StrNom=NACE_REV2&StrFormat=CSV&StrLanguageCode=EN&IntKey=&IntLevel=&bExport="),
    NAICS("naics","https://www.census.gov/naics/2017NAICS/2017_NAICS_Descriptions.xlsx"),
    NAPCS("napcs","https://www.census.gov/naics/napcs/structure/2017NAPCSStructure.xlsx"),
    NASDAQ("nasdaq","https://api.nasdaq.com/api/screener/stocks?limit=0&download=true"), // **
    SASB("sasb","https://www.sasb.org/find-your-industry/"), // **
    SIC("sic","https://www.bls.gov/oes/special.requests/oessic87.pdf"),
    TRBC("trbc","https://www.refinitiv.com/content/dam/marketing/en_us/documents/quick-reference-guides/trbc-business-classification-quick-guide.pdf"),  // aka Refinitiv
    ZACKS("zacks","https://www.zacks.com/zrank/sector-industry-classification.php");

    private final String prefix;
    private final String sourceFileLocation;

    Classification(String prefix, String sourceFileLocation)
    {
        this.prefix = prefix;
        this.sourceFileLocation = sourceFileLocation;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getSourceFileLocation()
    {
        return sourceFileLocation;
    }
}
